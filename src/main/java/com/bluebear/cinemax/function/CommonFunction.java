package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.service.genre.GenreService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommonFunction {

    @Autowired
    private GenreService genreService;

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Get the genres")
    public List<GenreDTO> getGenres() {
        return genreService.getAllGenres();
    }



}
