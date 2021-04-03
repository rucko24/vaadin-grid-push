package com.example.application.task;

import com.example.application.broadcaster.Broadcaster;
import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.service.ReactiveBookService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Log4j2
@SpringComponent
public class RefreshReactiveDataTask {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    private ReactiveBookService reactiveBookService;

    @Autowired
    public RefreshReactiveDataTask(final ReactiveBookService reactiveBookService) {
        this.reactiveBookService = reactiveBookService;
    }

    public void initUpdateGrid(final String message) {
        log.info(message);
        Broadcaster.broadcastMessage(message);
        this.scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                Broadcaster.broadcastForGridReactiveBooks(updatedBooks());
            } catch (RuntimeException e) {
                log.error("Error en runnable Refresh data task");
            }
        }, 0,2, TimeUnit.SECONDS);

    }

    private List<Book> updatedBooks() {
        final var list = new CopyOnWriteArrayList<Book>();
        final CountDownLatch latch = new CountDownLatch(1);
        reactiveBookService
                .findAll()
                .delayElements(Duration.ofMillis(100))
                .doOnComplete(latch::countDown)
                .subscribe(list::add);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("book list size " + list.size());
        return list;
    }



    public void stopUpdateGrid(final String message) {
        if(Objects.nonNull(scheduledFuture)) {
            Broadcaster.broadcastMessage(message);
            log.info(message);
            scheduledFuture.cancel(true);
        }
    }

}
