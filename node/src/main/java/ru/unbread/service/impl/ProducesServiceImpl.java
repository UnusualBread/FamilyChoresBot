package ru.unbread.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.unbread.service.ProducerService;

import static ru.unbread.model.RabbitQueue.ANSWER_MESSAGE;

@Service
public class ProducesServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    public ProducesServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }
}
