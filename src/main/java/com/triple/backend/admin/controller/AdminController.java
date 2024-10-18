package com.triple.backend.admin.controller;

import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.admin.service.AdminService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/books")
    public ResponseEntity<?> insertBook(@RequestBody AdminBookRequestDto dto) {
        adminService.insertBook(dto);
        return CommonResponse.created("Insert Book Success");
    }

    @GetMapping("/books")
    public ResponseEntity<?> getBookList(@RequestParam(name = "page", defaultValue = "0") int page,
                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        List<AdminBookResponseDto> bookDtoList = adminService.getBookList(page, size);
        return CommonResponse.ok("Get BookList Success", bookDtoList);
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<?> getBookDetail(@PathVariable(name = "bookId") Long bookId) {
        AdminBookResponseDto adminBookResponseDto = adminService.getBookDetail(bookId);
        return CommonResponse.ok("Get Book Success", adminBookResponseDto);
    }

    @PatchMapping("/books/{bookId}")
    public ResponseEntity<?> updateBook(@RequestBody AdminBookUpdateRequestDto dto, @PathVariable Long bookId) {
        adminService.updateBook(bookId, dto);
        return CommonResponse.ok("Update Book Success");
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        adminService.deleteBook(bookId);
        return CommonResponse.ok("Delete Book Success");
    }

}
