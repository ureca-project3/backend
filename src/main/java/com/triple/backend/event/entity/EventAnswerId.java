package com.triple.backend.event.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EventAnswerId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_question_id")
    private EventQuestion eventQuestionId;

    public EventAnswerId(Member memberId, EventQuestion eventQuestionId) {
        this.memberId = memberId;
        this.eventQuestionId = eventQuestionId;
    }

}
