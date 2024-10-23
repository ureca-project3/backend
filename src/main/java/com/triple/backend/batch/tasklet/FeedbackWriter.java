package com.triple.backend.batch.tasklet;

import com.triple.backend.feedback.entity.Feedback;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FeedbackWriter implements ItemWriter<Feedback> {

    @Override
    public void write(Chunk<? extends Feedback> chunk) throws Exception {

    }
}
