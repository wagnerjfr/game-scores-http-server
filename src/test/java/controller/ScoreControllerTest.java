package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.HttpStatusCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ScoreControllerTest {

    private ScoreController scoreController = ScoreController.INSTANCE;
    private SessionController sessionController = SessionController.INSTANCE;

    private final static String _URI = "/%s/score?sessionkey=%s";
    private final static String REQUEST_BODY = "score=%s";

    private final static String LEVEL_ID = "2";

    private final static int NUM_WRITERS = 1_000;
    private final static int NUM_READERS = 500;
    private final static int NUM_ITERATIONS = 10;

    @Nested
    class HighScoreTest {

        private String levelId;

        @Test
        @DisplayName("Test: Add one high score and check string response format")
        void testHighScoreStringResponseOneUser() {
            levelId = "100";
            final String stringResponse = "1=1000";

            Optional<String> optionalSession = sessionController.login(String.valueOf(1));
            if (optionalSession.isPresent()) {
                String sessionKey = optionalSession.get();
                String uri = String.format(_URI, levelId, sessionKey);
                scoreController.addScore(uri, String.format(REQUEST_BODY, 1000));
                assertEquals(stringResponse, scoreController.getScores(levelId).getMessage());
            } else {
                fail("Session key expected.");
            }
        }

        @Test
        @DisplayName("Test: Add multiple high scores and check string response format")
        void testHighScoreStringResponseMultipleUsers() {
            levelId = "200";
            List<Integer> userIdList = Arrays.asList(4, 3, 2, 1);

            IntFunction<Integer> multiply = (x) -> x * 100;

            // 4=400,3=300,2=200,1=100
            final String stringResponse = userIdList.stream()
                .map(i -> String.format("%d=%d", i, multiply.apply(i)))
                .collect(Collectors.joining(","));

            for (int userId : userIdList) {
                Optional<String> optionalSession = sessionController.login(String.valueOf(userId));
                if (optionalSession.isPresent()) {
                    String sessionKey = optionalSession.get();
                    String uri = String.format(_URI, levelId, sessionKey);
                    scoreController.addScore(uri, String.format(REQUEST_BODY, multiply.apply(userId)));
                } else {
                    fail("Session key expected.");
                }
            }
            assertEquals(stringResponse, scoreController.getScores(levelId).getMessage());
        }

        @Test
        @DisplayName("Test: Try to get high scores with not existing level id")
        void testHighScoreStringResponseNotExistingLevel() {
            List<String> noLevelIdList = Arrays.asList("300", "10000");

            for (String noLevelId : noLevelIdList) {
                Status status = scoreController.getScores(noLevelId);
                assertEquals(HttpStatusCode.OK, status.getCode());
                assertEquals("", status.getMessage());
            }
        }

        @Test
        @DisplayName("Test: Try to get high scores with invalid level id")
        void testHighScoreStringResponseInvalidLevel() {
            List<String> invalidList = Arrays.asList("", "abc", null);

            for (String invalidId : invalidList) {
                Status status = scoreController.getScores(invalidId);
                assertEquals(HttpStatusCode.BAD_REQUEST, status.getCode());
                assertEquals("", status.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Test: Try to add a score with invalid session key")
    void testAddScoreWithoutSessionKey() {
        String sessionKey = "123InvalidKey";
        String uri = String.format(_URI, LEVEL_ID, sessionKey);

        Status status = scoreController.addScore(uri, String.format(REQUEST_BODY, 0));
        assertEquals(HttpStatusCode.UNAUTHORIZED, status.getCode());
    }

    @Test
    @DisplayName("Test: Multiple writers and readers accessing the scores concurrently")
    void testWriterReaderParallel() throws InterruptedException, ExecutionException {

        List<Worker> workerList = new ArrayList<>(NUM_WRITERS + NUM_READERS);

        for (int id = 1; id <= NUM_WRITERS; id++) {
            workerList.add(new Writer(String.valueOf(id), NUM_ITERATIONS));
        }

        for (int id = 1; id <= NUM_READERS; id++) {
            workerList.add(new Reader());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_WRITERS + NUM_READERS);

        List<Future<List<Boolean>>> list = executorService.invokeAll(workerList);
        for (Future<List<Boolean>> future : list) {
            List<Boolean> result = future.get();
            assertTrue(result.parallelStream().allMatch(r -> r.equals(true)));
        }

        executorService.shutdown();
    }

    private interface Sleepable {
        int MIN_SLEEP = 10;
        int MAX_SLEEP = 50;

        default void waitForSomeSeconds() throws InterruptedException {
            Thread.sleep(ThreadLocalRandom.current().nextInt(MIN_SLEEP, MAX_SLEEP));
        }
    }

    private abstract class Worker implements Sleepable, Callable<List<Boolean>> { }

    private class Reader extends Worker {

        @Override
        public List<Boolean> call() throws Exception {
            List<Boolean> checkList = new ArrayList<>();

            for (int i = 0; i < NUM_ITERATIONS; i++) {
                waitForSomeSeconds();
                checkList.add(doCheck());
            }

            return checkList;
        }

        // Reads the high score list, parses it, confirms whether the list is sorted
        private boolean doCheck() {
            boolean bCheck = true;
            String list = scoreController.getScores(LEVEL_ID).getMessage();

            if (!list.isEmpty()) {
                String[] scores = list.split(",");

                List<Integer> resultList = Arrays.stream(scores)
                    .map(s -> s.substring(s.indexOf("=") + 1))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

                List<Integer> listToCompare = new ArrayList<>(resultList);
                listToCompare.sort(Comparator.reverseOrder());

                bCheck = resultList.equals(listToCompare);
            }

            return bCheck;
        }
    }

    private class Writer extends Worker {

        private String sessionKey;
        private int numberOfScores;
        private String uri;

        Writer(String userId, int numberOfScores) {
            Optional<String> optionalSession = sessionController.login(userId);
            if (optionalSession.isPresent()) {
                this.sessionKey = optionalSession.get();
                this.numberOfScores = numberOfScores;
                this.uri = String.format(_URI, LEVEL_ID, sessionKey);
            }
        }

        @Override
        public List<Boolean> call() throws InterruptedException {
            List<Boolean> checkList = new ArrayList<>();

            for (int i = 0; i < numberOfScores; i++) {
                waitForSomeSeconds();
                int score = ThreadLocalRandom.current().nextInt(1000, 2001);
                Status status = scoreController.addScore(uri, String.format(REQUEST_BODY, score));
                checkList.add(status.getCode() == HttpStatusCode.CREATED);
            }

            return checkList;
        }
    }
}
