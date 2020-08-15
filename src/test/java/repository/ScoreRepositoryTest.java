package repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ScoreRepository.ResultType;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreRepositoryTest {

    private final static int SCORE = 1000;

    private ScoreRepository scoreRepository = ScoreRepository.INSTANCE;

    @Nested
    class ScoreBoardTest {

        private static final int LEVEL_ID = 1;

        private final static int SCORE_MIN = SCORE;
        private final static int SCORE_MAX = SCORE * 2;

        @BeforeEach
        void beforeEach() {
            scoreRepository.deleteLevelId(LEVEL_ID);
        }

        @Test
        @DisplayName("Test: Adding same scores in the same level to the same user")
        void testAddEqualScoresSameUser() {
            for (int i = 1; i < 10; i++) {
                scoreRepository.register(LEVEL_ID, 1, SCORE);
            }
            assertEquals(1, scoreRepository.getScores(LEVEL_ID).size());
        }

        @Test
        @DisplayName("Test: Adding different scores in the same level to the same user")
        void testAddDifferentScoresSameUser() {
            for (int i = 1; i < 10; i++) {
                scoreRepository.register(LEVEL_ID,1, SCORE + (i * 10));
            }
            assertEquals(1, scoreRepository.getScores(LEVEL_ID).size());
        }

        @Test
        @DisplayName("Test: Adding the same scores in the same level to different users")
        void testAddEqualScoresDifferentUsers() {
            final int numUsers = 10;
            for (int i = 1; i <= numUsers; i++) {
                scoreRepository.register(LEVEL_ID, i, SCORE);
            }
            assertEquals(numUsers, scoreRepository.getScores(LEVEL_ID).size());
        }

        @Test
        @DisplayName("Test: Adding different scores in the same level to different users")
        void testAddDifferentScores() {
            final int userIdScoreMin = 9;
            final int userIdScoreMax = 10;

            // register intermediary scores
            final int numUsers = 8;
            for (int i = 1; i <= numUsers; i++) {
                scoreRepository.register(LEVEL_ID, i, ThreadLocalRandom.current().nextInt(SCORE_MIN + 1, SCORE_MAX));
            }

            // register min score
            scoreRepository.register(LEVEL_ID,userIdScoreMin, SCORE_MIN);
            // register max score
            scoreRepository.register(LEVEL_ID,userIdScoreMax, SCORE_MAX);

            scoreRepository.getUserScore(LEVEL_ID, ResultType.MIN)
                .ifPresent(userScore -> {
                    assertEquals(userIdScoreMin, userScore.getUserId());
                    assertEquals(SCORE_MIN, userScore.getScore());
                });

            scoreRepository.getUserScore(LEVEL_ID, ResultType.MAX)
                .ifPresent(userScore -> {
                    assertEquals(userIdScoreMax, userScore.getUserId());
                    assertEquals(SCORE_MAX, userScore.getScore());
                });
        }

        @Test
        @DisplayName("Test: Adding more scores than the limit that is stored")
        void testAddMoreUserScoresThanLimit() {
            final int numUsers = 20;
            for (int i = 1; i <= numUsers; i++) {
                scoreRepository.register(LEVEL_ID, i, ThreadLocalRandom.current().nextInt(SCORE_MIN, SCORE_MAX));
            }
            assertEquals(ScoreRepository.MAX_SIZE, scoreRepository.getScores(LEVEL_ID).size());

        }
    }
}
