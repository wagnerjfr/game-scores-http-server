package controller;

import domain.UserScore;
import repository.ScoreRepository;
import util.HttpStatusCode;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public enum ScoreController {
    INSTANCE;

    private static final int PARAM_VALUE_IDX = 1;
    private static final String EQUAL_DELIMITER = "=";

    public Status addScore(String uri, String requestBody) {
        int levelId;
        int score;
        String sessionKey;

        // Parse request
        try {
            levelId = Integer.parseInt(uri.split("/")[PARAM_VALUE_IDX]);
            score = Integer.parseInt(requestBody.split(EQUAL_DELIMITER)[PARAM_VALUE_IDX]);

            int index = uri.indexOf(EQUAL_DELIMITER);
            sessionKey = uri.substring(index + 1);

        } catch (NumberFormatException e) {
            return new Status(HttpStatusCode.BAD_REQUEST, String.format("Bad request: %s", e.getMessage()));
        }

        int code;
        OptionalInt optionalUserId = SessionValidator.INSTANCE.check(sessionKey);

        if (optionalUserId.isPresent()) {
            // Process request
            ScoreRepository.INSTANCE.register(levelId, optionalUserId.getAsInt(), score);
            code = HttpStatusCode.CREATED;
        } else {
            code = HttpStatusCode.UNAUTHORIZED;
        }

        return new Status(code, "");
    }

    public Status getScores(String urlLevelId) {
        int levelId;
        String result;

        try {
            levelId = Integer.parseInt(urlLevelId);
        } catch (NumberFormatException e) {
            return new Status(HttpStatusCode.BAD_REQUEST, "");
        }

        List<UserScore> userScoreList = ScoreRepository.INSTANCE.getScores(levelId);

        result = userScoreList.stream()
            .map(UserScore::toString)
            .collect(Collectors.joining(","));

        return new Status(HttpStatusCode.OK, result);
    }
}
