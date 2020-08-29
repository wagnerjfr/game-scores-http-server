package controller;

import domain.UserScore;
import repository.ScoreRepository;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public enum ScoreController {
    INSTANCE;

    public boolean addScore(String sessionKey, int levelId, int score) {
        OptionalInt optionalUserId = SessionValidator.INSTANCE.check(sessionKey);

        if (optionalUserId.isPresent()) {
            // Process request
            ScoreRepository.INSTANCE.register(levelId, optionalUserId.getAsInt(), score);
            return true;
        } else {
            return false;
        }
    }

    public String getScores(int levelId) {
        List<UserScore> userScoreList = ScoreRepository.INSTANCE.getScores(levelId);

        return userScoreList.stream()
            .map(UserScore::toString)
            .collect(Collectors.joining(","));
    }
}
