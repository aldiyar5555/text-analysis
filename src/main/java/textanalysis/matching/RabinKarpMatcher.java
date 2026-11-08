package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import textanalysis.util.Counter;

/**
 * Rabin-Karp with a polynomial rolling hash:
 *   h(s[0..m)) = (s[0]*B^(m-1) + s[1]*B^(m-2) + ... + s[m-1]) mod Q
 *
 * Rolling the window one position to the right costs O(1):
 *   h' = ((h - s[i]*B^(m-1)) * B + s[i+m]) mod Q
 *
 * Collisions: equal hashes do not guarantee equal strings, so on every hash hit
 * the window is VERIFIED character by character. A hit that fails verification
 * is a spurious hit (collision) and is counted, but never reported.
 * This makes the algorithm always correct (Las Vegas style); collisions only cost time.
 *
 * Preprocessing O(m). Search: expected O(n + m), worst O(n*m) (every window collides,
 * or many real matches, e.g. text = "aaaa...", pattern = "aa").
 */
public final class RabinKarpMatcher implements StringMatcher {

    public static final long DEFAULT_BASE = 256;
    public static final long DEFAULT_MOD = 1_000_000_007L;   // large prime

    private final long base;
    private final long mod;

    public RabinKarpMatcher() {
        this(DEFAULT_BASE, DEFAULT_MOD);
    }

    /** A small modulus can be passed to force collisions (used in tests). */
    public RabinKarpMatcher(long base, long mod) {
        this.base = base;
        this.mod = mod;
    }

    @Override
    public String name() {
        return "Rabin-Karp";
    }

    @Override
    public List<Integer> search(String text, String pattern, Counter c) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return result;
        }

        // highPow = B^(m-1) mod Q  -- weight of the leading character
        long highPow = 1;
        for (int i = 1; i < m; i++) {
            highPow = (highPow * base) % mod;
        }

        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
        }

        for (int s = 0; s <= n - m; s++) {
            if (windowHash == patternHash) {
                c.hashHits++;
                int j = 0;
                while (j < m) {
                    c.comparisons++;
                    if (text.charAt(s + j) != pattern.charAt(j)) {
                        break;
                    }
                    j++;
                }
                if (j == m) {
                    result.add(s);
                } else {
                    c.spuriousHits++;       // hash collision detected and rejected
                }
            }
            if (s < n - m) {
                windowHash = (windowHash - (text.charAt(s) * highPow) % mod + mod) % mod;
                windowHash = (windowHash * base + text.charAt(s + m)) % mod;
            }
        }
        return result;
    }
}
