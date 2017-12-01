package com.github.karibulabs.tdd.service;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

public class HollydayHelper {

    private static final String JSON_RESPONSE_FILE = "feriados.json";

    private File file;

    public HollydayHelper() {
        try {
            this.file = new ClassPathResource(JSON_RESPONSE_FILE).getFile();
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener archivo con días feriados",e);
        }
    }

    public void setResourceURL(ChileanHollydayService chileanHollydayService, String resourceURL){
        ReflectionTestUtils.setField(chileanHollydayService,"hollydayApiResourceURL",resourceURL);
    }

    public void setConnectionTimeout(ChileanHollydayService chileanHollydayService, long connectionTimeout){
        ReflectionTestUtils.setField(chileanHollydayService,"connectionTimeout",connectionTimeout);
    }

    public void setSocketTimeout(ChileanHollydayService chileanHollydayService, long socketTimeout){
        ReflectionTestUtils.setField(chileanHollydayService,"socketTimeout",socketTimeout);
    }

    public void setMaxRetries(ChileanHollydayService chileanHollydayService, int maxRetries){
        ReflectionTestUtils.setField(chileanHollydayService,"maxRetries",maxRetries);
    }

    public void setDelay(ChileanHollydayService chileanHollydayService, long delay){
        ReflectionTestUtils.setField(chileanHollydayService,"delay",delay);
    }

    public String jsonBody() {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer archivo con feriados",e);
        }
    }

}
