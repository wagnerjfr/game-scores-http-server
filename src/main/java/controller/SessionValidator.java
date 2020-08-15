package controller;

import domain.Session;
import repository.SessionRepository;

import java.util.Date;
import java.util.Optional;
import java.util.OptionalInt;

public enum SessionValidator {
    INSTANCE;

    public OptionalInt check(String sessionKey) {
        Optional<Session> optionalSessionKey = SessionRepository.INSTANCE.get(sessionKey);

        OptionalInt userId = OptionalInt.empty();
        if (optionalSessionKey.isPresent()) {

            Session session = optionalSessionKey.get();
            Date currentDate = new Date();

            if (session.getKey().equals(sessionKey) && currentDate.before(session.getDateExpiry())) {
                userId = OptionalInt.of(session.getUserId());
            }
        }
        return userId;

    }
}
