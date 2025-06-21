package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.repository.cashier.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestConrtoller {

    @Autowired
    private SeatRepository seatRepository;

    @GetMapping("/")
    public Long test() {
        return seatRepository.seatLeftInSchedule(1, 1, 1);
    }
}
