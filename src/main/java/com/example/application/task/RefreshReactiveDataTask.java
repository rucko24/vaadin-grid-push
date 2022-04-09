package com.example.application.task;

import com.example.application.broadcaster.Broadcaster;
import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.service.ReactiveBookService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Log4j2
@SpringComponent
@RequiredArgsConstructor
public class RefreshReactiveDataTask {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    private final ReactiveBookService reactiveBookService;

    public void initUpdateGrid(final String message) {
        log.info(message);
        Broadcaster.broadcastMessage(message);
        this.scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                /**
                 * Esto invoca a mongo con los libros actualizados
                 */
                Broadcaster.broadcastForGridReactiveBooks(updatedBooks());
            } catch (RuntimeException e) {
                log.error("Error en runnable Refresh data task");
            }
        }, 0,2, TimeUnit.SECONDS);

    }

    private List<Book> updatedBooks() {
        final List<Book> bookList = reactiveBookService.findAllForUpdatedBooks();
        log.info("book list size " + bookList.size());
        return bookList;
    }

    public void stopUpdateGrid(final String message) {
        if(Objects.nonNull(scheduledFuture)) {
            Broadcaster.broadcastMessage(message);
            log.info(message);
            scheduledFuture.cancel(true);
        }
    }

}
