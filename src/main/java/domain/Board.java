package domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

public final class Board {

    private int levelId;
    private int maxSize;
    private SortedSet<UserScore> scoreSet;

    public Board(int levelId, int maxSize) {
        this.levelId = levelId;
        this.maxSize = maxSize;

        // P.S. Users with the same score will be sorted by who registered the score first
        scoreSet = new TreeSet<>(Comparator.comparing(UserScore::getScore).reversed()
            .thenComparing(UserScore::getDateCreated)
            .thenComparing(UserScore::getUserId));
    }

    public void add(int userId, int score) {
        Optional<UserScore> optionalUserScore = scoreSet.parallelStream()
            .filter(us -> us.getUserId() == userId)
            .findFirst();

        if (optionalUserScore.isPresent()) {
            UserScore userScore = optionalUserScore.get();
            if (score <= userScore.getScore()) {
                return;
            }
            scoreSet.remove(userScore);
        }

        UserScore userScore = new UserScore(userId, score);
        scoreSet.add(userScore);

        if (scoreSet.size() > maxSize) {
            scoreSet.remove(scoreSet.last()); //remove lowest score
        }
    }

    public List<UserScore> getBoard() {
        return new ArrayList<>(scoreSet);
    }

    public UserScore getMaxUserScore() {
        return scoreSet.first();
    }

    public UserScore getMinUserScore() {
        return scoreSet.last();
    }

    @Override
    public String toString() {
        return "levelId: " + levelId + "; " + scoreSet;
    }
}
