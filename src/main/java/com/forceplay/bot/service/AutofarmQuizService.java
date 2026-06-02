package com.forceplay.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forceplay.bot.config.TelegramBotProperties;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AutofarmQuizService {

    private final ObjectMapper objectMapper;
    private final TelegramBotProperties botProperties;

    public QuizQuestion randomQuestion() {
        List<QuizQuestion> questions = loadQuestions();
        if (questions.isEmpty()) {
            throw new ForcePlayException("Autofarm quiz file is empty");
        }
        return questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
    }

    private List<QuizQuestion> loadQuestions() {
        Path quizFile = Path.of(botProperties.getAutofarmQuizFilePath()).toAbsolutePath().normalize();
        try {
            if (!Files.exists(quizFile)) {
                throw new ForcePlayException("Autofarm quiz file not found: " + quizFile);
            }
            QuizQuestion[] questions = objectMapper.readValue(quizFile.toFile(), QuizQuestion[].class);
            return questions == null ? List.of() : List.of(questions);
        } catch (IOException exception) {
            throw new ForcePlayException("Failed to read autofarm quiz file: " + quizFile);
        }
    }

    public record QuizQuestion(
            String question,
            List<String> answers,
            int correctAnswerIndex
    ) {
    }
}
