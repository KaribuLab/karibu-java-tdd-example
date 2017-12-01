package com.github.karibulabs.tdd.service;

import com.github.karibulabs.tdd.RetryThresholdException;
import com.github.karibulabs.tdd.domain.ChileanHollyday;
import com.github.karibulabs.tdd.repository.ChileanHollydayRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class ChileanHollydayServiceImpl implements ChileanHollydayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChileanHollydayServiceImpl.class);

    @Value("${hollyday.api.resource.url}")
    private String hollydayApiResourceURL;

    @Value("${hollyday.api.resource.connectionTimeout}")
    private long connectionTimeout;

    @Value("${hollyday.api.resource.socketTimeout}")
    private long socketTimeout;

    @Value("${hollyday.api.resource.delay}")
    private long delay;

    @Value("${hollyday.api.resource.maxRetries}")
    private int maxRetries;

    @Autowired
    ChileanHollydayRepository chileanHollydayRepository;

    @Override
    public boolean isAHoliday(Date date) {
        pullHollydays();
        return chileanHollydayRepository.findByDate(date) != null;
    }

    private ChileanHollyday[] getHollydaysFromAPI() {

        for (int retry = 1; retry <= maxRetries; retry++) {
            Unirest.setTimeouts(connectionTimeout, socketTimeout);
            try {
                LOGGER.debug("Timeout actual: [socketTimeout='{}',delay='{}']",socketTimeout,delay);
                Thread.sleep(delay);
                HttpResponse<ChileanHollyday[]> response =
                        Unirest.get(hollydayApiResourceURL)
                                .asObject(ChileanHollyday[].class);

                return response.getBody();
            } catch (UnirestException | InterruptedException e) {
                LOGGER.warn("Error al invocar el servicio. Reintentos: [retry='{}',maxRetries='{}']", retry, maxRetries, e);
            }
        }

        throw new RetryThresholdException("Se han agotado los reintentos para acceder al API de feriados.");
    }

    @Override
    public List<ChileanHollyday> pullHollydays() {
        chileanHollydayRepository.deleteAll();

        ChileanHollyday[] chileanHollydays = getHollydaysFromAPI();

        Arrays.stream(chileanHollydays).forEach(chileanHollyday -> {
            chileanHollydayRepository.save(chileanHollyday);
        });

        return Arrays.asList(chileanHollydays);
    }
}
