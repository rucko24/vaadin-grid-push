package com.com.example.application.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class MemoryConsumtion {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private String getTotalMemory() {
        return String.format("Total memory: %dMB ",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    }

    public void showMemory(final UI ui, final Label label) {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ui.access(() -> {
               label.setText(getTotalMemory());
               System.gc();
            });
        },0,1, TimeUnit.SECONDS);
    }

}
