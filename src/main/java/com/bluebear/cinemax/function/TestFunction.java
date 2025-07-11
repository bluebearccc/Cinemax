package com.bluebear.cinemax.function;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestFunction {

    @Tool(description = "Find movie schedule at a specific theater")
    public String findSchedule(String movie, String theater) {
        return "Oppenheimer is playing at Beta Cineplex at 7:00 PM and 9:30 PM.";
    }

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
