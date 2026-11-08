package textanalysis.matching;

import java.util.ArrayList;
import java.util.List;
import textanalysis.util.Counter;

/**
 * Knuth-Morris-Pratt.
 *
 * lps[i] = length of the longest PROPER prefix of pattern[0..i] that is also
 *          a suffix of pattern[0..i].
 *
 * During the search the text pointer i never moves backwards. On a mismatch after
 * j matched characters we already know that text[i-j..i) == pattern[0..j), so we can
 * continue from j = lps[j-1] instead of restarting from 0 -> no character of the text
 * is re-examined after it has been matched.
 *
 * Preprocessing O(m), search O(n) (at most 2n comparisons), total O(n + m).
 */
public final class KMPMatcher implements StringMatcher {

    @Override
    public String name() {
        return "KMP";
    }

    /** Builds the prefix-function (LPS) table in O(m). */
    public static int[] buildLps(String pattern, Counter c) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;      // length of the current longest prefix-suffix
        int i = 1;
        while (i < m) {
            c.preprocessComparisons++;
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];   // fall back, do not advance i
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public static int[] buildLps(String pattern) {
        return buildLps(pattern, new Counter());
    }

    @Override
    public List<Integer> search(String text, String pattern, Counter c) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return result;
        }
        int[] lps = buildLps(pattern, c);
        int j = 0;                          // number of pattern chars currently matched
        for (int i = 0; i < n; i++) {
            char t = text.charAt(i);
            while (true) {
                c.comparisons++;
                if (t == pattern.charAt(j)) {
                    j++;
                    if (j == m) {
                        result.add(i - m + 1);
                        j = lps[j - 1];     // continue: allows overlapping matches
                    }
                    break;
                } else if (j > 0) {
                    j = lps[j - 1];         // shift pattern, keep i
                } else {
                    break;                  // j == 0 and mismatch: advance i
                }
            }
        }
        return result;
    }
}
