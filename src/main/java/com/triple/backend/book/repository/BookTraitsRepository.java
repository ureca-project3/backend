package com.triple.backend.book.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.triple.backend.book.entity.Book;
import com.triple.backend.book.entity.BookTraits;

public interface BookTraitsRepository extends JpaRepository<BookTraits,Long> {

	@Query(value = """
    		SELECT bt.book
    		  FROM BookTraits bt
    		 WHERE bt.traitScore BETWEEN :minScore AND :maxScore
    		 ORDER BY bt.book.publishedAt DESC
	""")
	List<Book> findBooksByTraitScoreBetween(@Param("minScore") int minScore, @Param("maxScore") int maxScore, Pageable pageable);

	// 성향정보 저장
	@Query("SELECT bt.trait.traitName, COUNT(bt) AS count FROM BookTraits bt GROUP BY bt.trait.traitName ORDER BY count DESC")
	List<Object[]> findMbtiTypeStats();

	// 모든 BookTraits 데이터 삭제
	void deleteAll();
}
