package repository;

import controller.SessionValidator;
import domain.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.SessionKeyGenerator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SessionRepositoryTest {

    private static final Duration VALID_PERIOD = Duration.ofSeconds(5);

    private SessionRepository sessionRepository = SessionRepository.INSTANCE;
    private SessionValidator sessionValidator = SessionValidator.INSTANCE;
    private SessionKeyGenerator sessionKeyGenerator = SessionKeyGenerator.INSTANCE;

    @Test
    @DisplayName("Test: Register user session")
    void testRegisterSession() {
        int userId = 1;
        String sessionKey = sessionKeyGenerator.getKey();
        Date dateCreated = new Date();
        Date dateExpiry = Date.from(dateCreated.toInstant().plus(VALID_PERIOD));

        sessionRepository.register(sessionKey, userId, dateCreated, VALID_PERIOD);

        Optional<Session> optionalSession = sessionRepository.get(sessionKey);
        if (optionalSession.isPresent()) {
            Session session = optionalSession.get();
            assertEquals(sessionKey, session.getKey());
            assertEquals(userId, session.getUserId());
            assertEquals(dateCreated, session.getDateCreated());
            assertEquals(dateExpiry, session.getDateExpiry());
        } else {
            fail("Session expected");
        }
    }

    @Test
    @DisplayName("Test: Register user session and check whether time is valid")
    void testSessionRegister() throws InterruptedException {
        int userId = 1;
        String sessionKey = sessionKeyGenerator.getKey();

        sessionRepository.register(sessionKey, userId, new Date(), VALID_PERIOD);

        Thread.sleep(Duration.ofSeconds(3).toMillis());
        OptionalInt optionalUserId = sessionValidator.check(sessionKey);
        if (optionalUserId.isPresent()) {
            assertEquals(userId, optionalUserId.getAsInt());
        } else {
            fail("UserId expected");
        }

        Thread.sleep(Duration.ofSeconds(3).toMillis());
        optionalUserId = sessionValidator.check(sessionKey);
        assertEquals(OptionalInt.empty(), optionalUserId);
    }

    @Test
    @DisplayName("Test: Multiple sessions registered for the same user")
    void testMultipleSessionRegisterForSameUser() {
        int numberOfSessionKeys = 5;

        List<String> sessionKeys = new ArrayList<>(numberOfSessionKeys);

        for (int i = 0; i < numberOfSessionKeys; i++) {
            String sessionKey = sessionKeyGenerator.getKey();
            sessionRepository.register(sessionKey, 1, new Date(), VALID_PERIOD);
            sessionKeys.add(sessionKey);
        }

        for (String sessionKey : sessionKeys) {
            boolean isPresent = sessionRepository.get(sessionKey).isPresent();

            if (sessionKeys.indexOf(sessionKey) == numberOfSessionKeys - 1) { // latest session key
                assertTrue(isPresent);
            } else {
                assertFalse(isPresent);
            }
        }
    }

    @Test
    @DisplayName("Test: Multiple sessions registered for different users")
    void testMultipleSessionRegisterForDifferentUsers() {
        int numberOfUsers = 5;

        List<String> sessionKeys = new ArrayList<>(numberOfUsers);

        for (int userId = 1; userId <= numberOfUsers; userId++) {
            String sessionKey = sessionKeyGenerator.getKey();
            sessionRepository.register(sessionKey, userId, new Date(), VALID_PERIOD);
            sessionKeys.add(sessionKey);
        }

        for (String sessionKey : sessionKeys) {
            assertTrue(sessionRepository.get(sessionKey).isPresent());
        }
    }
}
