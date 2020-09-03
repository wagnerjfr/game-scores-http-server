package controller;

import domain.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
        Optional<String> optionalLogin = sessionController.login(userId);

        String sessionKey = "";
        if (optionalLogin.isPresent()) {
            sessionKey = optionalLogin.get();
            assertEquals(10, sessionKey.length());
        } else {
            fail("Session key expected.");
        }

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
}
