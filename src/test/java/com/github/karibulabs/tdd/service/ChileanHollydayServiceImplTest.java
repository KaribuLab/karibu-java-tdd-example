package com.github.karibulabs.tdd.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.karibulabs.tdd.domain.ChileanHollyday;
import com.github.karibulabs.tdd.repository.ChileanHollydayRepository;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.FileUtils;
import org.h2.util.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChileanHollydayServiceImplTest {

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
    private static final String JSON_RESPONSE_FILE = "feriados.json";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888));

    @InjectMocks
    @Spy
    private ChileanHollydaysServiceImpl chileanHollydaysService;

    @Mock
    private ChileanHollydayRepository chileanHollydayRepository;

    @Mock
    private List<ChileanHollyday> chileanHollydayList;

    @Mock
    private  ChileanHollyday chileanHollyday;

    @Before
    public void initMocks(){
        ReflectionTestUtils.setField(chileanHollydaysService,"hollydayApiResourceURL",FAKE_HOLLYDAY_API_RESOURCE_URL);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isHollydayTrueTest() throws IOException {

        File file = new ClassPathResource(JSON_RESPONSE_FILE).getFile();
        String body = FileUtils.readFileToString(file);


        wireMockRule.stubFor(get(urlEqualTo(TEST_URL)).willReturn(aResponse().withBody(body)));

        doReturn(chileanHollyday).when(chileanHollydayRepository).findByDate(any(Date.class));

        Date someDate = new Date();

        assertTrue("El valor debe ser feriado",chileanHollydaysService.isAHoliday(someDate));

        verify(chileanHollydaysService).isAHoliday(someDate);
    }



}
