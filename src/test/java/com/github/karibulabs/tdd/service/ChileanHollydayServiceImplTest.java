package com.github.karibulabs.tdd.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karibulabs.tdd.RetryThresholdException;
import com.github.karibulabs.tdd.domain.ChileanHollyday;
import com.github.karibulabs.tdd.repository.ChileanHollydayRepository;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChileanHollydayServiceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChileanHollydayServiceImplTest.class);

    static {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private static final String TEST_URL = "/feriados";
    private static final String FAKE_HOLLYDAY_API_RESOURCE_URL = "http://localhost:8888" + TEST_URL;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888));

    @InjectMocks
    @Spy
    private ChileanHollydayServiceImpl chileanHollydaysService;

    @Mock
    private ChileanHollydayRepository chileanHollydayRepository;

    @Mock
    private List<ChileanHollyday> chileanHollydayList;

    @Mock
    private ChileanHollyday chileanHollyday;

    private HollydayHelper hollydayHelper = new HollydayHelper();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isHollydayTrueTest() {

        hollydayHelper.setResourceURL(chileanHollydaysService, FAKE_HOLLYDAY_API_RESOURCE_URL);
        hollydayHelper.setConnectionTimeout(chileanHollydaysService, 10000);
        hollydayHelper.setSocketTimeout(chileanHollydaysService, 10000);
        hollydayHelper.setMaxRetries(chileanHollydaysService, 1);

        wireMockRule.stubFor(get(urlEqualTo(TEST_URL)).willReturn(aResponse().withBody(hollydayHelper.jsonBody())));

        doReturn(chileanHollyday).when(chileanHollydayRepository).findByDate(any(Date.class));

        Date someDate = new Date();

        assertTrue("El valor DEBE SER feriado", chileanHollydaysService.isAHoliday(someDate));

        verify(chileanHollydaysService).isAHoliday(someDate);
    }

    @Test
    public void isHollydayFalseTest() {

        hollydayHelper.setResourceURL(chileanHollydaysService, FAKE_HOLLYDAY_API_RESOURCE_URL);
        hollydayHelper.setConnectionTimeout(chileanHollydaysService, 10000);
        hollydayHelper.setSocketTimeout(chileanHollydaysService, 10000);
        hollydayHelper.setMaxRetries(chileanHollydaysService, 3);
        hollydayHelper.setDelay(chileanHollydaysService, 1000);

        wireMockRule.stubFor(get(urlEqualTo(TEST_URL)).willReturn(aResponse().withBody(hollydayHelper.jsonBody())));

        doReturn(null).when(chileanHollydayRepository).findByDate(any(Date.class));

        Date someDate = new Date();

        assertTrue("El valor NO DEBE SER feriado", !chileanHollydaysService.isAHoliday(someDate));

        verify(chileanHollydaysService).isAHoliday(someDate);
    }

    @Test
    public void isHollydayWithRetriesTest() {

        hollydayHelper.setResourceURL(chileanHollydaysService, FAKE_HOLLYDAY_API_RESOURCE_URL);
        hollydayHelper.setConnectionTimeout(chileanHollydaysService, 3000);
        hollydayHelper.setSocketTimeout(chileanHollydaysService, 3000);
        hollydayHelper.setMaxRetries(chileanHollydaysService, 3);
        hollydayHelper.setDelay(chileanHollydaysService, 5000);

        wireMockRule.stubFor(get(urlEqualTo(TEST_URL)).willReturn(aResponse().withBody(hollydayHelper.jsonBody()).withFixedDelay(5000)));

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                hollydayHelper.setConnectionTimeout(chileanHollydaysService, 6000);
                hollydayHelper.setSocketTimeout(chileanHollydaysService, 6000);
            } catch (InterruptedException e) {
                LOGGER.error("Error al cambiar timeout", e);
            }
        }).start();

        doReturn(chileanHollyday).when(chileanHollydayRepository).findByDate(any(Date.class));

        Date someDate = new Date();

        assertTrue("El valor DEBE SER feriado", chileanHollydaysService.isAHoliday(someDate));

        verify(chileanHollydaysService).isAHoliday(someDate);
    }

    @Test(expected = RetryThresholdException.class)
    public void isHollydayWithTimeoutErrorTest() {

        hollydayHelper.setResourceURL(chileanHollydaysService, FAKE_HOLLYDAY_API_RESOURCE_URL);
        hollydayHelper.setConnectionTimeout(chileanHollydaysService, 1000);
        hollydayHelper.setSocketTimeout(chileanHollydaysService, 1000);
        hollydayHelper.setMaxRetries(chileanHollydaysService, 1);
        hollydayHelper.setDelay(chileanHollydaysService, 5000);

        wireMockRule.stubFor(get(urlEqualTo(TEST_URL)).willReturn(aResponse().withBody(hollydayHelper.jsonBody()).withFixedDelay(5000)));

        doReturn(chileanHollyday).when(chileanHollydayRepository).findByDate(any(Date.class));

        Date someDate = new Date();

        chileanHollydaysService.isAHoliday(someDate);
    }


}
