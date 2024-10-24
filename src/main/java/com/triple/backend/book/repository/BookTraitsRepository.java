package com.triple.backend.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.triple.backend.book.entity.BookTraits;

public interface BookTraitsRepository extends JpaRepository<BookTraits,Long> {
}
