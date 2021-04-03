package com.example.application.reactivedatabase.repository;

import com.example.application.reactivedatabase.model.Book;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 *
 */
@Repository
public interface BookReactiveRepository extends ReactiveMongoRepository<Book,String> {
    Mono<Book> findByTitle(final String title);
    Mono<Book> findByAuthor(final String author);
}
