package com.example.application.reactivedatabase.service;

import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.repository.BookReactiveRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

@Service
@Log4j2
public class ReactiveBookService {

    private BookReactiveRepository bookReactiveRepository;

    @Autowired
    public ReactiveBookService(final BookReactiveRepository bookReactiveRepository) {
        this.bookReactiveRepository = bookReactiveRepository;
    }

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
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Book> bookList = new CopyOnWriteArrayList<>();
        bookReactiveRepository.findAll()
                .doOnComplete(latch::countDown)
                .subscribe(bookList::add);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Flux.just(bookList);
    }

    public List<Book> findAllForUpdatedBooks() {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Book> bookList = new CopyOnWriteArrayList<>();
        bookReactiveRepository.findAll()
                .doOnComplete(latch::countDown)
                .subscribe(bookList::add);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
