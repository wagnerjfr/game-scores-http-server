package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SessionValidatorTest {

    private SessionValidator sessionValidator = SessionValidator.INSTANCE;

    @Test
    @DisplayName("Test: Session created and check the response")
    void testSessionJustCreated() {
        String userId = "1";
        Optional<String> optionalSession = SessionController.INSTANCE.login(userId);

        if (optionalSession.isPresent()) {
            String sessionKey = optionalSession.get();
            OptionalInt optionalUserId = sessionValidator.check(sessionKey);

            if (optionalUserId.isPresent()) {
                assertEquals(userId, String.valueOf(optionalUserId.getAsInt()));
            } else {
                fail("UserId expected");
            }
        } else {
            fail("Session key expected");
        }
    }

    @Test
    @DisplayName("Test: Multiple sessions but just last one is valid")
    void testMultipleSessionForSameUserJustLastOneIsValid() {
        final int size = 5;
        String userId = "1";

        List<Optional<String>> resultList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Optional<String> optionalSession = SessionController.INSTANCE.login(userId);
            resultList.add(optionalSession);
        }

        for (Optional<String> optional : resultList) {
            if (optional.isPresent()) {
                String sessionKey = optional.get();
                OptionalInt optionalUserId = sessionValidator.check(sessionKey);

                if ((resultList.indexOf(optional) == size - 1) && optionalUserId.isPresent()) { // latest key
                    assertEquals(userId, String.valueOf(optionalUserId.getAsInt()));
                } else {
                    assertEquals(OptionalInt.empty(), optionalUserId);
                }
            }
        }
    }

}
