package repository;

import domain.Board;
import domain.UserScore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum ScoreRepository {
    INSTANCE;

    public static final int MAX_SIZE = 15;

    private Map<Integer, Board> scoreMap;

    public enum ResultType {
        MIN, MAX
    }

    ScoreRepository() {
        scoreMap = new HashMap<>();
    }

    public void register(int levelId, int userId, int score) {
        if (!scoreMap.containsKey(levelId)) {
            scoreMap.put(levelId, new Board(levelId, MAX_SIZE));
        }
        Board board = scoreMap.get(levelId);
        board.add(userId, score);
    }

    public List<UserScore> getScores(int levelId) {
        List<UserScore> resultList = Collections.emptyList();

        if (scoreMap.containsKey(levelId)) {
            resultList = scoreMap.get(levelId).getBoard();
        }

        return resultList;
    }

    public Optional<UserScore> getUserScore(int levelId, ResultType type) {
        Optional<UserScore> optionalResult = Optional.empty();

        if (scoreMap.containsKey(levelId)) {
            if (type.equals(ResultType.MIN)) {
                optionalResult = Optional.of(scoreMap.get(levelId).getMinUserScore());
            } else { // MAX
                optionalResult = Optional.of(scoreMap.get(levelId).getMaxUserScore());
            }
        }

        return optionalResult;
    }

    public void deleteLevelId(int levelId) {
        scoreMap.remove(levelId);
    }
}
