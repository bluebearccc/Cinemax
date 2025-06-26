package com.bluebear.cinemax.service;

import com.bluebear.cinemax.repository.DetailSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailSeatService {

    @Autowired
    private DetailSeatRepository detailSeatRepository;

    public List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId) {
        return detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId);
    }
}