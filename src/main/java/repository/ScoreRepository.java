package repository;

import domain.Board;
import domain.UserScore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public enum ScoreRepository {
    INSTANCE;

    public static final int MAX_SIZE = 15;

    private Map<Integer, Board> scoreMap;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock read = rwl.readLock();
    private final Lock write = rwl.writeLock();

    public enum ResultType {
        MIN, MAX
    }

    ScoreRepository() {
        scoreMap = new HashMap<>();
    }

    public void register(int levelId, int userId, int score) {
        if (!containsLevelId(levelId)) {
            createBoard(levelId);
        }
        addScoreToBoard(levelId, userId, score);
    }

    private boolean containsLevelId(int levelId) {
        read.lock();
        try {
            return scoreMap.containsKey(levelId);
        } finally {
            read.unlock();
        }
    }

    private void createBoard(int levelId) {
        write.lock();
        try {
            scoreMap.put(levelId, new Board(levelId, MAX_SIZE));
        } finally {
            write.unlock();
        }
    }

    private void addScoreToBoard(int levelId, int userId, int score) {
        write.lock();
        try {
            Board board = getBoard(levelId);
            board.add(userId, score);
        } finally {
            write.unlock();
        }
    }

    private Board getBoard(int levelId) {
        read.lock();
        try {
            return scoreMap.get(levelId);
        } finally {
            read.unlock();
        }
    }

    public List<UserScore> getScores(int levelId) {
        List<UserScore> resultList = Collections.emptyList();

        read.lock();
        try {
            if (containsLevelId(levelId)) {
                resultList = scoreMap.get(levelId).getBoard();
            }
        } finally {
            read.unlock();
        }

        return resultList;
    }

    public Optional<UserScore> getUserScore(int levelId, ResultType type) {
        Optional<UserScore> optionalResult = Optional.empty();

        if (containsLevelId(levelId)) {
            Board board = getBoard(levelId);
            UserScore userScore = type.equals(ResultType.MIN) ? board.getMinUserScore() : board.getMaxUserScore();
            optionalResult = Optional.of(userScore);
        }

        return optionalResult;
    }

    public void deleteLevelId(int levelId) {
        scoreMap.remove(levelId);
    }
}
