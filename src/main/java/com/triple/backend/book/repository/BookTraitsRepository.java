package com.triple.backend.book.repository;

import com.triple.backend.book.entity.BookTraits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookTraitsRepository extends JpaRepository<BookTraits,Long> {

    @Query("select bt from BookTraits bt where bt.book.bookId = :bookId")
    Optional<BookTraits> findByBookId(Long bookId);
}
