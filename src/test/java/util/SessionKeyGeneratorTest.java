package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionKeyGeneratorTest {

    @Test
    @DisplayName("Test: 10M unique session keys")
    void testUniqueKeyGenerator() {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < 10_000_000; i++) {
            String key = SessionKeyGenerator.INSTANCE.getKey();
            int count = 1;
            if (map.containsKey(key)) {
                count = map.get(key) + 1;
            }
            map.put(key, count);
        }

        long equals = map.values().parallelStream()
            .filter(count -> count > 1)
            .count();

        assertEquals(0, equals);
    }
}
