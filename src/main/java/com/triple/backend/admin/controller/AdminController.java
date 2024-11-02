package com.triple.backend.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.admin.service.AdminService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/books")
    public ResponseEntity<?> insertBook(@RequestBody AdminBookRequestDto dto) throws JsonProcessingException {
        adminService.insertBook(dto);
        return CommonResponse.created("Insert Book Success");
    }

    @GetMapping("/books")
    public ResponseEntity<?> getBookList(@PageableDefault(page = 0, size = 10,
            sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        List<AdminBookResponseDto> bookDtoList = adminService.getBookList(pageable);
        return CommonResponse.ok("Get BookList Success", bookDtoList);
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<?> getBookDetail(@PathVariable(name = "bookId") Long bookId) {
        AdminBookResponseDto adminBookResponseDto = adminService.getBookDetail(bookId);
        return CommonResponse.ok("Get Book Success", adminBookResponseDto);
    }

    @PatchMapping("/books/{bookId}")
    public ResponseEntity<?> updateBook(@RequestBody AdminBookUpdateRequestDto dto,
                                        @PathVariable(name = "bookId") Long bookId) {
        adminService.updateBook(bookId, dto);
        return CommonResponse.ok("Update Book Success");
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        adminService.deleteBook(bookId);
        return CommonResponse.ok("Delete Book Success");
    }

}
