package com.github.karibulabs.tdd.shell;

import com.github.karibulabs.tdd.service.ChileanHollydaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@ShellComponent
public class ChileanHollydayShell {

    @Autowired
    ChileanHollydaysService chileanHollydaysService;

    @ShellMethod("Valida sin un día es feriado")
    public String isHollyday(@ShellOption(value = "-date", help = "Fecha en formato YYYYMMDD") String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date = simpleDateFormat.parse(dateString);
            if (chileanHollydaysService.isAHoliday(date)) {
                return "La fecha ingresada ES un día feriado";
            } else {
                return "La fecha ingresada NO ES un día feriado";
            }
        } catch (ParseException e) {
            return "Error en el formato de la fecha";
        }
    }

}
