package controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SessionValidatorTest {

    private SessionValidator sessionValidator = SessionValidator.INSTANCE;

    @Test
    @DisplayName("Test: Session created and check the response")
    void testSessionJustCreated() {
        String userId = "1";
        Status status = SessionController.INSTANCE.login(userId);
        assertEquals(HttpStatusCode.OK, status.getCode());

        String sessionKey = status.getMessage();
        OptionalInt optionalUserId = sessionValidator.check(sessionKey);

        if (optionalUserId.isPresent()) {
            assertEquals(userId, String.valueOf(optionalUserId.getAsInt()));
        } else {
            fail("UserId expected");
        }
    }

    @Test
    @DisplayName("Test: Multiple sessions but just last one is valid")
    void testMultipleSessionForSameUserJustLastOneIsValid() {
        final int size = 5;
        String userId = "1";

        List<Status> statusList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Status status = SessionController.INSTANCE.login(userId);
            assertEquals(HttpStatusCode.OK, status.getCode());
            statusList.add(status);
        }

        for (Status status : statusList) {
            String sessionKey = status.getMessage();
            OptionalInt optionalUserId = sessionValidator.check(sessionKey);

            if ((statusList.indexOf(status) == size - 1) && optionalUserId.isPresent()) { // latest key
                assertEquals(userId, String.valueOf(optionalUserId.getAsInt()));
            } else {
                assertEquals(OptionalInt.empty(), optionalUserId);
            }
        }
    }

}
