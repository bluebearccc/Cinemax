package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.theater.TheaterService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TheaterFunction {

    @Autowired
    private TheaterService theaterService;

    @Tool(description = "Get all theaters")
    public List<TheaterDTO> getAllTheaters() {
        return theaterService.getAllTheaters().getContent();
    }

    @Tool(description = "Get theater by name")
    public TheaterDTO getTheaterByName(String name) {
        return theaterService.getTheaterByName(name);
    }
}
