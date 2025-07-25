package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.PromotionDTO;
import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import com.bluebear.cinemax.service.PromotionService;
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
    @Autowired
    private PromotionService promotionService;

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Get the genres")
    public List<GenreDTO> getGenres() {
        return genreService.getAllGenres();
    }

    @Tool(description = "get the promotions currently available")
    public List<Promotion> getPromotions() {
        return promotionService.searchVouchers("", Promotion_Status.Available.name());
    }

}
