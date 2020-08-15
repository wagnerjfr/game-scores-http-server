package controller;

import domain.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.HttpStatusCode;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SessionControllerTest {

    private SessionController sessionController = SessionController.INSTANCE;

    @Test
    @DisplayName("Test: Create a session with a valid user id")
    void testGenerateSessionValidUserId() throws InterruptedException {
        int userId = 1;
        Status status = sessionController.login(String.valueOf(userId));

        String sessionKey = status.getMessage();
        assertEquals(10, sessionKey.length());
        assertEquals(HttpStatusCode.OK, status.getCode());

        Thread.sleep(Duration.ofSeconds(2).toMillis());

        final Date currentDate = new Date();
        final Date expiredDate = Date.from(currentDate.toInstant().plus(SessionController.VALID_PERIOD));

        Optional<Session> optionalSession = sessionController.getSession(sessionKey);
        if (optionalSession.isPresent()) {
            Session session = optionalSession.get();
            assertEquals(sessionKey, session.getKey());
            assertEquals(userId, session.getUserId());
            assertTrue(session.getDateExpiry().after(currentDate));
            assertTrue(session.getDateExpiry().before(expiredDate));
        } else {
            fail("Session expected");
        }
    }

    @Test
    @DisplayName("Test: Try to create sessions with invalid user ids")
    void testGenerateSessionInvalidUserId() {
        for (String invalidId : Arrays.asList("abc", "", null)) {
            Status status = sessionController.login(invalidId);

            String sessionKey = status.getMessage();
            assertEquals("", sessionKey);
            assertEquals(HttpStatusCode.BAD_REQUEST, status.getCode());
        }
    }
}
