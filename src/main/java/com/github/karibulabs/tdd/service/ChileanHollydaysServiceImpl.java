package com.github.karibulabs.tdd.service;

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
public class ChileanHollydaysServiceImpl implements ChileanHollydaysService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChileanHollydaysServiceImpl.class);

    @Value("${hollyday.api.resource.url}")
    private String hollydayApiResourceURL;

    @Autowired
    ChileanHollydayRepository chileanHollydayRepository;

    @Override
    public boolean isAHoliday(Date date) {
        pullHollydays();
        return chileanHollydayRepository.findByDate(date) != null;
    }

    @Override
    public List<ChileanHollyday> pullHollydays() {
        try {
            chileanHollydayRepository.deleteAll();
            HttpResponse<ChileanHollyday[]> response =
                    Unirest.get(hollydayApiResourceURL)
                            .asObject(ChileanHollyday[].class);
            ChileanHollyday [] chileanHollydays = response.getBody();
            Arrays.stream(chileanHollydays).forEach(chileanHollyday -> {
                chileanHollydayRepository.save(chileanHollyday);
            });
            return Arrays.asList(chileanHollydays);
        } catch (UnirestException e) {
            LOGGER.error("Error al invocar el servicio",e);
            throw new RuntimeException("Error al invocar el servicio", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<ArrayList<T>> getGenericListType(Class<T> generic) {
        return (Class<ArrayList<T>>) new ArrayList<T>().getClass();
    }

}
