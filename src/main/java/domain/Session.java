package domain;

import java.time.Duration;
import java.util.Date;

public final class Session {

    private String key;
    private int userId;
    private Date dateCreated;
    private Date dateExpiry;
    private Duration validPeriod;

    public Session(String key, int userId, Date dateCreated, Duration validPeriod) {
        this.key = key;
        this.userId = userId;
        this.validPeriod = validPeriod;
        this.dateCreated = dateCreated;
        this.dateExpiry = getValidUpToDateHour();
    }

    public String getKey() {
        return key;
    }

    public int getUserId() {
        return userId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateExpiry() {
        return dateExpiry;
    }

    private Date getValidUpToDateHour() {
        return Date.from(dateCreated.toInstant().plus(validPeriod));
    }

}
