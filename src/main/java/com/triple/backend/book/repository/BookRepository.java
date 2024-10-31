package com.triple.backend.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.triple.backend.book.entity.Book;

import java.time.LocalDateTime;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

	// 도서 검색
	@Query(value = """
			SELECT b
			  FROM Book b
			 WHERE
			 	LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
			 	LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
			 	LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
   				LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
			ORDER BY b.title, b.summary, b.author, b.publisher
		""", countQuery = "SELECT count(b) FROM Book b")
	Page<Book> searchBookByKeyword(@Param(value = "keyword") String keyword, Pageable pageable);

	@Query("SELECT b FROM Book b JOIN Feedback f ON b.bookId = f.book.bookId " +
			"WHERE f.likeStatus = true AND f.createdAt >= :startDate " +
			"GROUP BY b.bookId ORDER BY COUNT(f) DESC LIMIT 10")
	List<Book> findTop10BooksByLikesInLastThreeMonths(@Param("startDate") LocalDateTime startDate);

	@Query("select b from Book b order by b.createdAt desc")
	Page<Book> findAllOrderByCreatedAtDesc(Pageable pageable);
}
