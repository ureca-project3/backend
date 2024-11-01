package com.triple.backend.history;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.impl.MbtiHistoryServiceImpl;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.entity.*;
import com.triple.backend.test.repository.TestAnswerRepository;
import com.triple.backend.test.repository.TestParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MbtiHistoryServiceImplTest {

    @InjectMocks
    private MbtiHistoryServiceImpl mbtiHistoryService;

    @Mock
    private MbtiHistoryRepository mbtiHistoryRepository;
    @Mock
    private ChildTraitsRepository childTraitsRepository;
    @Mock
    private TestAnswerRepository testAnswerRepository;
    @Mock
    private TestParticipationRepository testParticipationRepository;
    @Mock
    private ChildRepository childRepository;

    @Captor
    private ArgumentCaptor<MbtiHistory> mbtiHistoryCaptor;
    @Captor
    private ArgumentCaptor<List<MbtiHistory>> mbtiHistoryListCaptor;

    @Nested
    @DisplayName("deleteMyChildTraitHistory 메소드 테스트")
    class DeleteMyChildTraitHistoryTest {

        private Child child;
        private MbtiHistory mbtiHistory;

        @BeforeEach
        void setUp() {
            child = Child.builder()
                    .childId(1L)
                    .build();

            mbtiHistory = MbtiHistory.builder()
                    .historyId(1L)
                    .child(child)
                    .currentMbti("INFP")
                    .reason("010")
                    .isDeleted(false)
                    .build();
        }

        @Test
        @DisplayName("마지막 히스토리 삭제 시 기본 INFP 히스토리 생성")
        void deleteLastHistoryTest() {
            // given
            given(mbtiHistoryRepository.count()).willReturn(1L);
            given(childRepository.findById(anyLong())).willReturn(Optional.of(child));
            given(mbtiHistoryRepository.findById(anyLong())).willReturn(Optional.of(mbtiHistory));
            given(mbtiHistoryRepository.save(any(MbtiHistory.class))).willReturn(mbtiHistory);

            // when
            MbtiHistoryDeletedResponseDto result = mbtiHistoryService.deleteMyChildTraitHistory(1L, 1L);

            // then
            assertThat(result.getIsDeleted()).isTrue();
            verify(mbtiHistoryRepository, times(2)).save(mbtiHistoryCaptor.capture());

            List<MbtiHistory> capturedHistories = mbtiHistoryCaptor.getAllValues();
            assertThat(capturedHistories).hasSize(2);

            MbtiHistory newHistory = capturedHistories.get(0);
            assertThat(newHistory)
                    .extracting("currentMbti", "reason", "isDeleted")
                    .containsExactly("INFP", "010", false);

            MbtiHistory deletedHistory = capturedHistories.get(1);
            assertThat(deletedHistory.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("논리적 히스토리 삭제 성공")
        void deleteNormalHistoryTest() {
            // given
            given(mbtiHistoryRepository.count()).willReturn(2L);
            given(childRepository.findById(anyLong())).willReturn(Optional.of(child));
            given(mbtiHistoryRepository.findById(anyLong())).willReturn(Optional.of(mbtiHistory));
            given(mbtiHistoryRepository.save(any(MbtiHistory.class))).willReturn(mbtiHistory);

            // when
            MbtiHistoryDeletedResponseDto result = mbtiHistoryService.deleteMyChildTraitHistory(1L, 1L);

            // then
            assertThat(result.getIsDeleted()).isTrue();
            verify(mbtiHistoryRepository, times(1)).save(mbtiHistoryCaptor.capture());

            MbtiHistory capturedHistory = mbtiHistoryCaptor.getValue();
            assertThat(capturedHistory.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 자녀 ID로 요청시 예외 발생")
        void deleteWithInvalidChildIdTest() {
            given(childRepository.findById(anyLong())).willReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> mbtiHistoryService.deleteMyChildTraitHistory(1L, 999L));

            assertThat(exception.getMessage()).contains("자녀");
            verify(mbtiHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 히스토리 ID로 요청시 예외 발생")
        void deleteWithInvalidHistoryIdTest() {
            given(childRepository.findById(anyLong())).willReturn(Optional.of(child));
            given(mbtiHistoryRepository.findById(anyLong())).willReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> mbtiHistoryService.deleteMyChildTraitHistory(999L, 1L));

            assertThat(exception.getMessage()).contains("자녀 성향 진단 결과");
            verify(mbtiHistoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cleanUpOldRecords 메소드 테스트")
    class CleanUpOldRecordsTest {

        private MbtiHistory oldMbtiHistory;
        private MbtiHistory recentMbtiHistory;
        private MbtiHistory day30History;
        private TestParticipation testParticipation;
        private ChildTraits childTraits;
        private TestAnswer testAnswer;

        @BeforeEach
        void setUp() {
            // 31일 지난 거
            LocalDateTime oldDate = LocalDateTime.now().minusDays(31);

            // 15일 지난 거
            LocalDateTime recentDate = LocalDateTime.now().minusDays(15);

            // 30일 지난 거
            LocalDateTime day30 = LocalDateTime.now().minusDays(30);

            oldMbtiHistory = MbtiHistory.builder()
                    .historyId(1L)
                    .reason("020")
                    .isDeleted(true)
                    .reasonId(1L)
                    .build();
            ReflectionTestUtils.setField(oldMbtiHistory, "modifiedAt", oldDate);

            recentMbtiHistory = MbtiHistory.builder()
                    .historyId(2L)
                    .reason("020")
                    .isDeleted(true)
                    .reasonId(2L)
                    .build();
            ReflectionTestUtils.setField(recentMbtiHistory, "modifiedAt", recentDate);

            day30History = MbtiHistory.builder()
                    .historyId(3L)
                    .reason("020")
                    .isDeleted(true)
                    .reasonId(3L)
                    .build();
            ReflectionTestUtils.setField(day30History, "modifiedAt", day30);

            Child child = Child.builder()
                    .childId(1L)
                    .build();

            com.triple.backend.test.entity.Test test = new com.triple.backend.test.entity.Test();
            Trait trait = createTestTrait(test);
            TestQuestion testQuestion = createTestQuestion(test, trait);

            childTraits = createChildTraits(oldMbtiHistory, trait);
            testParticipation = createTestParticipation(test, child);
            testAnswer = createTestAnswer(testParticipation, testQuestion);
        }

        @Test
        @DisplayName("30일 이상된 히스토리 연관 데이터 물리적 삭제")
        void cleanUpOldRecordsSuccessTest() {
            // given
            List<MbtiHistory> allHistoryList = Arrays.asList(oldMbtiHistory, recentMbtiHistory);
            List<TestParticipation> testParticipationList = Collections.singletonList(testParticipation);
            List<ChildTraits> childTraitsList = Collections.singletonList(childTraits);
            List<TestAnswer> testAnswerList = Collections.singletonList(testAnswer);

            given(mbtiHistoryRepository.findByReasonAndIsDeleted("020", true))
                    .willReturn(allHistoryList);
            given(childTraitsRepository.findByMbtiHistoryIn(anyList()))
                    .willReturn(childTraitsList);
            given(testParticipationRepository.findAllById(anyList()))
                    .willReturn(testParticipationList);
            given(testAnswerRepository.findByTestAnswerIdTestParticipationIn(anyList()))
                    .willReturn(testAnswerList);

            // when
            mbtiHistoryService.cleanUpOldRecords();

            // then
            verify(childTraitsRepository).deleteAllInBatch(eq(childTraitsList));
            verify(testAnswerRepository).deleteAllInBatch(eq(testAnswerList));
            verify(testParticipationRepository).deleteAllInBatch(eq(testParticipationList));
            verify(mbtiHistoryRepository).deleteAllInBatch(mbtiHistoryListCaptor.capture());

            List<MbtiHistory> deletedHistories = mbtiHistoryListCaptor.getValue();
            assertThat(deletedHistories)
                    .hasSize(1)
                    .contains(oldMbtiHistory)
                    .doesNotContain(recentMbtiHistory);
        }

        @Test
        @DisplayName("정확히 30일된 히스토리와 연관된 모든 데이터 물리적 삭제")
        void cleanUpExactly30DaysOldRecordTest() {
            // given
            List<MbtiHistory> borderlineHistoryList = Collections.singletonList(day30History);
            List<TestParticipation> testParticipationList = Collections.singletonList(testParticipation);
            List<ChildTraits> childTraitsList = Collections.singletonList(childTraits);
            List<TestAnswer> testAnswerList = Collections.singletonList(testAnswer);

            given(mbtiHistoryRepository.findByReasonAndIsDeleted("020", true))
                    .willReturn(borderlineHistoryList);
            given(childTraitsRepository.findByMbtiHistoryIn(anyList()))
                    .willReturn(childTraitsList);
            given(testParticipationRepository.findAllById(anyList()))
                    .willReturn(testParticipationList);
            given(testAnswerRepository.findByTestAnswerIdTestParticipationIn(anyList()))
                    .willReturn(testAnswerList);

            // when
            mbtiHistoryService.cleanUpOldRecords();

            // then
            // 각 엔티티의 삭제 검증
            verify(childTraitsRepository).deleteAllInBatch(Collections.singletonList(childTraits));
            verify(testAnswerRepository).deleteAllInBatch(Collections.singletonList(testAnswer));
            verify(testParticipationRepository).deleteAllInBatch(Collections.singletonList(testParticipation));
            verify(mbtiHistoryRepository).deleteAllInBatch(mbtiHistoryListCaptor.capture());

            // 삭제된 MbtiHistory 검증
            List<MbtiHistory> deletedHistories = mbtiHistoryListCaptor.getValue();
            assertThat(deletedHistories)
                    .hasSize(1)
                    .contains(day30History);
        }

        @Test
        @DisplayName("삭제할 히스토리가 없는 경우")
        void cleanUpWithNoOldRecordsTest() {
            // given
            given(mbtiHistoryRepository.findByReasonAndIsDeleted("020", true))
                    .willReturn(Collections.emptyList());

            // when
            mbtiHistoryService.cleanUpOldRecords();

            // then
            verify(childTraitsRepository, never()).deleteAllInBatch(anyList());
            verify(testAnswerRepository, never()).deleteAllInBatch(anyList());
            verify(testParticipationRepository, never()).deleteAllInBatch(anyList());
            verify(mbtiHistoryRepository, never()).deleteAllInBatch(anyList());
        }

        // 생성 분리하기~
        private Trait createTestTrait(com.triple.backend.test.entity.Test test) {
            Trait trait = new Trait();
            ReflectionTestUtils.setField(trait, "traitId", 1L);
            ReflectionTestUtils.setField(trait, "traitName", "에너지방향");
            ReflectionTestUtils.setField(trait, "traitDescription", "에너지방향 설명");
            ReflectionTestUtils.setField(trait, "maxScore", 100);
            ReflectionTestUtils.setField(trait, "minScore", 0);
            ReflectionTestUtils.setField(trait, "test", test);
            return trait;
        }

        private TestQuestion createTestQuestion(com.triple.backend.test.entity.Test test, Trait trait) {
            return TestQuestion.builder()
                    .test(test)
                    .trait(trait)
                    .questionText("Sample Question")
                    .build();
        }

        private ChildTraits createChildTraits(MbtiHistory mbtiHistory, Trait trait) {
            return ChildTraits.builder()
                    .mbtiHistory(mbtiHistory)
                    .trait(trait)
                    .traitScore(10)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        private TestParticipation createTestParticipation(com.triple.backend.test.entity.Test test, Child child) {
            return TestParticipation.builder()
                    .test(test)
                    .child(child)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        private TestAnswer createTestAnswer(TestParticipation testParticipation, TestQuestion testQuestion) {
            TestAnswerId testAnswerId = new TestAnswerId(testParticipation, testQuestion);
            return new TestAnswer(testAnswerId, 3);
        }
    }
}