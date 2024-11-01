package com.triple.backend.book.service.impl;

import com.triple.backend.book.dto.BookRankingResponseDto;
import com.triple.backend.book.entity.BookTraits;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.chatgpt.dto.BookAnalysisRequestDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.test.repository.TraitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.triple.backend.book.dto.BookDetailResponseDto;
import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.book.service.BookService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {


	private final ChatGptService chatGptService;
	private final BookRepository bookRepository;
	private final TraitRepository traitRepository;
	private final BookTraitsRepository bookTraitsRepository;

	/**
	 *	도서 정보 상세 조회
	 * 	- TODO: 회원인증필요
	 */
	@Override
	public BookDetailResponseDto getBookDetail(Long bookId) {
		Book book = bookRepository.findById(bookId)
			.orElseThrow(() -> new IllegalArgumentException("도서 정보를 찾을 수 없습니다."));

		return new BookDetailResponseDto(book);
	}

	/**
	 * 도서 검색
	 */
	@Override
	public Page<BookResponseDto> getBookSearch(String keyword, Pageable pageable) {
		Page<Book> books = bookRepository.searchBookByKeyword(keyword, pageable);

		return books.map(BookResponseDto::new);
	}

	@Override
	public List<BookRankingResponseDto> getTopLikedBooks() {
		LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
		List<Book> books = bookRepository.findTop10BooksByLikesInLastThreeMonths(threeMonthsAgo);
		return books.stream().map(BookRankingResponseDto::new).collect(Collectors.toList());
	}

	@Override
	public List<BookRankingResponseDto> getBookList(Pageable pageable) {
		Page<Book> books = bookRepository.findAllOrderByCreatedAtDesc(pageable);
		return books.stream().map(BookRankingResponseDto::new).collect(Collectors.toList());
	}

	// 책 전체 MBTI 검사
// 책 전체 MBTI 검사
	@Override
	@Transactional(readOnly = false)
	public Map<String, Object> analyzeMbti(Book book) {
		BookAnalysisRequestDto request = BookAnalysisRequestDto.builder()
				.content(book.getSummary())
				.analysisType("MBTI")
				.build();
		Map<String, Object> result = chatGptService.analyzeMbti(request);
		if (result == null) {
			throw new RuntimeException("MBTI 분석 중 오류가 발생했습니다.");
		}

		// 분석 결과를 book_traits 테이블에 저장
		Map<String, Integer> mbtiScores = (Map<String, Integer>) result.get("mbtiScores");
		System.out.println("MBTI Scores: " + mbtiScores);

		// Trait 엔티티에서 해당하는 trait 조회
		com.triple.backend.test.entity.Trait eiTrait = traitRepository.findByTraitName("에너지방향");
		System.out.println("EI Trait: " + eiTrait);
		com.triple.backend.test.entity.Trait snTrait = traitRepository.findByTraitName("인식기능");
		System.out.println("SN Trait: " + snTrait);
		com.triple.backend.test.entity.Trait tfTrait = traitRepository.findByTraitName("판단기능");
		System.out.println("TF Trait: " + tfTrait);
		com.triple.backend.test.entity.Trait jpTrait = traitRepository.findByTraitName("생활양식");
		System.out.println("JP Trait: " + jpTrait);

		BookTraits eiBookTraits = BookTraits.builder()
				.book(book)
				.trait(eiTrait)
				.traitScore(mbtiScores.get("EI"))
				.build();
		BookTraits snBookTraits = BookTraits.builder()
				.book(book)
				.trait(snTrait)
				.traitScore(mbtiScores.get("SN"))
				.build();
		BookTraits tfBookTraits = BookTraits.builder()
				.book(book)
				.trait(tfTrait)
				.traitScore(mbtiScores.get("TF"))
				.build();
		BookTraits jpBookTraits = BookTraits.builder()
				.book(book)
				.trait(jpTrait)
				.traitScore(mbtiScores.get("JP"))
				.build();

		System.out.println("Saving BookTraits...");
		bookTraitsRepository.saveAll(List.of(eiBookTraits, snBookTraits, tfBookTraits, jpBookTraits));
		System.out.println("BookTraits saved.");

		return result;
	}
}
