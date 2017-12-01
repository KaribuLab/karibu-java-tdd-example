package com.github.karibulabs.tdd.service;

import com.github.karibulabs.tdd.domain.ChileanHollyday;

import java.util.Date;
import java.util.List;

public interface ChileanHollydaysService {
    boolean isAHoliday(Date date);

    List<ChileanHollyday> pullHollydays();
}
