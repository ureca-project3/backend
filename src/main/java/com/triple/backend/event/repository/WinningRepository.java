package com.triple.backend.event.repository;

import com.triple.backend.event.dto.WinnerResponseDto;
import com.triple.backend.event.entity.Winning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WinningRepository extends JpaRepository<Winning, Long> {

    @Query("SELECT new com.triple.backend.event.dto.WinnerResponseDto(m.name, m.phone) " +
            "FROM Winning w " +
            "JOIN EventPart ep ON w.eventPart.eventPartId = ep.eventPartId " +
            "JOIN Member m ON ep.member.memberId = m.memberId " +
            "WHERE ep.event.eventId = :eventId")
    List<WinnerResponseDto> findWinningDataByEventId(@Param("eventId") Long eventId);
}
