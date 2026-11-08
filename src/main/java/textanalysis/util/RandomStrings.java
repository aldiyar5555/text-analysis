package textanalysis.util;

import java.util.Random;

/** Deterministic random string generation (fixed seed, fixed alphabet). */
public final class RandomStrings {
    /** The alphabet used by all experiments (documented in README). */
    public static final String ALPHABET = "abcd";
    public static final long SEED = 42L;

    private final Random random;
    private final String alphabet;

    public RandomStrings(long seed, String alphabet) {
        this.random = new Random(seed);
        this.alphabet = alphabet;
    }

    public RandomStrings() {
        this(SEED, ALPHABET);
    }

    public String next(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
