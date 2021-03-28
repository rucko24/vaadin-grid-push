package com.com.example.application.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class Hour {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    public static final String PATTERN_TIME = "hh:mm:ss a";

    public String getHour() {
        return DateTimeFormatter.ofPattern( PATTERN_TIME )
                .withLocale(Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }

    public void initHour(final UI ui, final Label labelHour) {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ui.access(() -> {
                labelHour.setText("Current server time: ".concat(this.getHour()));
            });
        },0,1, TimeUnit.SECONDS);

    }

}
