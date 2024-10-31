package com.triple.backend.recbook.repository;

import com.triple.backend.recbook.entity.RecBookLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecBookLikeRepository extends JpaRepository<RecBookLike, Long> {

    @Query(value = "select rbl.recBookLikeId from RecBookLike rbl " +
            "where rbl.recBook.recBookId = :recBookId and rbl.child.childId = :childId")
    Long findByRecBookIdAndChildId(Long recBookId, Long childId);
}
