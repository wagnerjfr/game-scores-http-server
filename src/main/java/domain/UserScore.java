package domain;

import java.util.Date;
import java.util.Objects;

public final class UserScore {

    private int userId;
    private int score;
    private Date dateCreated;

    UserScore(int userId, int score) {
        this.userId = userId;
        this.score = score;
        this.dateCreated = new Date();
    }

    public int getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof UserScore)) {
            return false;
        }

        UserScore userScore = (UserScore) obj;
        return userId == userScore.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public String toString() {
        return userId + "=" + score;
    }
}
