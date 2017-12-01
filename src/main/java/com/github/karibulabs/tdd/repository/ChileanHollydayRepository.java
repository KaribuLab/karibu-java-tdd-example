package com.github.karibulabs.tdd.repository;

import com.github.karibulabs.tdd.domain.ChileanHollyday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface ChileanHollydayRepository extends JpaRepository<ChileanHollyday,Long> {

    ChileanHollyday findByDate(Date date);
}
