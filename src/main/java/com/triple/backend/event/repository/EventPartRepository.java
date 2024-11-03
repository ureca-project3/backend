package com.triple.backend.event.repository;

import com.triple.backend.event.entity.EventPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPartRepository extends JpaRepository<EventPart, Long> {

}
