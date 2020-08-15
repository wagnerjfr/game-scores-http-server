package util;

import java.util.concurrent.ThreadLocalRandom;

public enum SessionKeyGenerator {
    INSTANCE;

    public String getKey() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < targetStringLength; i++) {
            int n = ThreadLocalRandom.current().nextInt(leftLimit, rightLimit + 1);
            char c = (char) n;
            if (ThreadLocalRandom.current().nextBoolean()) {
                c = Character.toUpperCase(c);
            }
            key.append(c);
        }

        return key.toString();
    }
}
