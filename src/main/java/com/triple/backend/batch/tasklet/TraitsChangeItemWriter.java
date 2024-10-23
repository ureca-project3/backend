package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.TraitsChangeDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.TraitsChange;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.TraitsChangeRepository;
import com.triple.backend.test.entity.Trait;
import com.triple.backend.test.repository.TraitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TraitsChangeItemWriter implements ItemWriter<List<TraitsChangeDto>> {

    private final ChildTraitsRepository childTraitsRepository;
    private final TraitsChangeRepository traitsChangeRepository;
    private final ChildRepository childRepository;
    private final TraitRepository traitRepository;

    @Override
    public void write(Chunk<? extends List<TraitsChangeDto>> chunk) throws Exception {
        for (List<TraitsChangeDto> list : chunk) {
            for (TraitsChangeDto dto : list) {
                Optional<TraitsChange> optionalTraitsChange = traitsChangeRepository
                        .findByChildIdAndTraitId(dto.getChildId(), dto.getTraitId());
                if (dto.isBeyondFive()) {
                    /*
                    문제 1 : 이전 FeedbackProcessor에서 조회했던 ChildTraits를 다시 조회한다. Processor와 Writer간 데이터를 공유할 방법 없을까?
                    문제 2 : 배치 처리는 에러가 나도 기록만 해 두고, 다음 처리로 넘어가도록 구현하는 것이 일반적이다. 그러나 현재 구현은 repository에 해당 아이템이 없다면 exception을 터뜨리고 있다. 어떻게 해야 할까?
                     */
                    ChildTraits childTrait = childTraitsRepository
                            .findByChildIdAndTraitId(dto.getChildId(), dto.getTraitId())
                            .orElseThrow(() -> new IllegalArgumentException("여기 에러났어요!"));
                    childTrait.updateScore((int) Math.floor(dto.getChangeAmount()));
                    childTraitsRepository.save(childTrait);

                    // TraitsChange 초기화
                    if (optionalTraitsChange.isPresent()) {
                        TraitsChange traitsChange = optionalTraitsChange.get();
                        traitsChange.resetChangeAmount();
                        /*
                        문제 3 : 현재 한 개씩 데이터를 집어넣고 있다. 한꺼번에 집어넣으려면 Jdbc를 써야 한다. (Jpa는 GenerationType.Identity를 걸면 무조건 1개씩만 데이터를 집어넣기 때문에 사용 불가)
                         */
                        traitsChangeRepository.save(traitsChange);
                    }
                } else {
                    if (optionalTraitsChange.isEmpty()) {
                        Child child = childRepository.findById(dto.getChildId()).orElseThrow(() -> new IllegalArgumentException("여기 에러났어요!"));
                        Trait trait = traitRepository.findById(dto.getTraitId()).orElseThrow(() -> new IllegalArgumentException("여기 에러났어요!"));
                        TraitsChange traitsChange = new TraitsChange(child, trait, dto.getChangeAmount());
                        traitsChangeRepository.save(traitsChange);
                    } else {
                        TraitsChange traitsChange = optionalTraitsChange.get();
                        traitsChange.updateChangeAmount(dto.getChangeAmount());
                        traitsChangeRepository.save(traitsChange);
                    }
                }
            }
        }
    }
}
