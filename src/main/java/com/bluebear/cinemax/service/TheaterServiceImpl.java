package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.TheaterRepository;
import com.bluebear.cinemax.entity.Theater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TheaterServiceImpl implements TheaterService {
    @Autowired
    private TheaterRepository theaterRepository;

    public Theater getTheaterById(Integer id){
        return theaterRepository.findById(id).orElse(null) ;
    }
}

