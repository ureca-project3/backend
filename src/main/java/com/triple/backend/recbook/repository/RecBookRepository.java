package com.triple.backend.recbook.repository;

import com.triple.backend.recbook.entity.RecBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecBookRepository extends JpaRepository<RecBook, Long> {
}
