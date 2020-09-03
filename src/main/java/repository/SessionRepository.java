package repository;

import domain.Session;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public enum SessionRepository {
    INSTANCE;

    private Map<String, Session> sessionMap;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock read = rwl.readLock();
    private final Lock write = rwl.writeLock();

    SessionRepository() {
        sessionMap = new HashMap<>();
    }

    public void register(String sessionKey, int userId, Date dateCreated, Duration validPeriod) {
        write.lock();
        try {
            removeOldSession(userId);
            Session session = new Session(sessionKey, userId, dateCreated, validPeriod);
            sessionMap.put(session.getKey(), session);
        } finally {
            write.unlock();
        }
    }

    public Optional<Session> get(String key) {
        read.lock();
        try {
            return sessionMap.containsKey(key) ? Optional.of(sessionMap.get(key)) : Optional.empty();
        } finally {
            read.unlock();
        }
    }

    private void removeOldSession(int userId) {
        sessionMap.values().parallelStream()
            .filter(session -> session.getUserId() == userId)
            .findFirst()
            .ifPresent(session -> sessionMap.remove(session.getKey()));
    }
}
