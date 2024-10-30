package com.triple.backend.event.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class EventAnswer {

    @EmbeddedId
    EventAnswerId eventAnswerId;
    private String answerText;

    public EventAnswer(EventAnswerId eventAnswerId, String answerText) {
        this.eventAnswerId = eventAnswerId;
        this.answerText = answerText;
    }

}
