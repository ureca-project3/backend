package com.triple.backend.recbook.repository;

import com.triple.backend.book.entity.Book;
import com.triple.backend.recbook.entity.RecBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecBookRepository extends JpaRepository<RecBook, Long> {

    @Query(value = "SELECT rc.book_id FROM rec_book rc WHERE child_id = :childId ORDER BY RAND() LIMIT 12", nativeQuery = true)
    List<Long> findRandomBooksByChildId(@Param("childId") Long childId);

    @Query(value = "select rb from RecBook rb where rb.child.childId = :childId and rb.book.bookId = :bookId")
    Optional<RecBook> findByChildIdAndBookId(Long childId, Long bookId);
}
