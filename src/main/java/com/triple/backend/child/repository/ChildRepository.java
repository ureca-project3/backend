package com.triple.backend.child.repository;

import com.triple.backend.child.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, Long> {
}
