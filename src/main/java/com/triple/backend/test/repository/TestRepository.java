package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {

}
