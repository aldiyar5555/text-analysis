package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import textanalysis.util.Counter;

/**
 * Naive (brute-force) matching: try every shift s = 0..n-m and compare
 * the pattern left to right until a mismatch or a full match.
 *
 * Preprocessing: none. Search: best O(n) (first char mismatches), worst O((n-m+1)*m).
 */
public final class NaiveMatcher implements StringMatcher {

    @Override
    public String name() {
        return "Naive";
    }

    @Override
    public List<Integer> search(String text, String pattern, Counter c) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return result;
        }
        for (int s = 0; s <= n - m; s++) {
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
            }
        }
        return result;
    }
}
