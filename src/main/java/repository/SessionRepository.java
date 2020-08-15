package repository;

import domain.Session;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum SessionRepository {
    INSTANCE;

    private Map<String, Session> sessionMap;

    SessionRepository() {
        sessionMap = new HashMap<>();
    }

    public void register(String sessionKey, int userId, Date dateCreated, Duration validPeriod) {
        removeOldSession(userId);
        Session session = new Session(sessionKey, userId, dateCreated, validPeriod);
        sessionMap.put(session.getKey(), session);
    }

    public Optional<Session> get(String key) {
        return sessionMap.containsKey(key) ? Optional.of(sessionMap.get(key)) : Optional.empty();
    }

    private void removeOldSession(int userId) {
        sessionMap.values().parallelStream()
            .filter(session -> session.getUserId() == userId)
            .findFirst()
            .ifPresent(session -> sessionMap.remove(session.getKey()));
    }
}
