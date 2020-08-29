package controller;

import domain.Session;
import repository.SessionRepository;
import util.SessionKeyGenerator;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

public enum SessionController {
    INSTANCE;

    public static final Duration VALID_PERIOD = Duration.ofMinutes(10);

    public Optional<String> login(String urlUserId) {
        String result;
        try {
            int userId = Integer.parseInt(urlUserId);

            String sessionKey = SessionKeyGenerator.INSTANCE.getKey();

            SessionRepository.INSTANCE.register(sessionKey, userId, new Date(), VALID_PERIOD);

            result = sessionKey;

        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public Optional<Session> getSession(String sessionKey) {
        return SessionRepository.INSTANCE.get(sessionKey);
    }
}
