package com.triple.backend.admin.service;

import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    void insertBook(AdminBookRequestDto dto);

    List<AdminBookResponseDto> getBookList(Pageable pageable);

    AdminBookResponseDto getBookDetail(Long bookId);

    void updateBook(Long bookId, AdminBookUpdateRequestDto dto);

    void deleteBook(Long bookId);
}
