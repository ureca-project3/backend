package com.triple.backend.book.repository;

import com.triple.backend.book.entity.BookTraits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookTraitsRepository extends JpaRepository<BookTraits,Long> {

    @Query(value = "select bt from BookTraits bt where bt.book.bookId = :bookId")
    List<BookTraits> findAllByBookId(Long bookId);
}
