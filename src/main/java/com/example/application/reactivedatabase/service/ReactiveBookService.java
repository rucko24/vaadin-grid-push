package com.example.application.reactivedatabase.service;

import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.repository.BookReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@Component
@Log4j2
public class ReactiveBookService {

    private final BookReactiveRepository bookReactiveRepository;

    public Mono<Mono<Book>> findById(final String id) {
        return Mono.justOrEmpty(bookReactiveRepository.findById(id));
    }

    public Mono<Book> findByTitle(final String title) {
        return bookReactiveRepository
                .findByTitle(title)
                .map(e -> Book.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .author(e.getAuthor())
                            .build());
    }

    public Mono<Book> findByAuthor(final String author) {
        return bookReactiveRepository
                .findByAuthor(author)
                .map(e -> Book.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .author(e.getAuthor())
                            .build());
    }

    public Flux<List<Book>> findAll() {
        final List<Book> bookList = new CopyOnWriteArrayList<>();
        return bookReactiveRepository.findAll()
                .flatMap((Book book) -> {
                    bookList.add(book);
                    return Flux.just(bookList);
                });
    }

    /**
     * Add countDownLatch with subscription
     *
     * @return List<Book>
     */
    public List<Book> findAllForUpdatedBooks() {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Book> bookList = new CopyOnWriteArrayList<>();
        bookReactiveRepository.findAll()
                .doOnComplete(latch::countDown)
                .doOnError(Flux::error)
                .subscribe(bookList::add);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Error findAllForUpdatedBooks() {}", e);
        }
        return bookList;
    }

    public void saveAll(final Flux<Book> bookFlux) {
        this.bookReactiveRepository.saveAll(bookFlux).subscribe();
    }

    public void deleteAll() {
        this.bookReactiveRepository.deleteAll().subscribe();
    }

}
