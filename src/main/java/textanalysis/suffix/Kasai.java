package textanalysis.suffix;

import textanalysis.util.Counter;

/**
 * Kasai's algorithm: LCP array from a string and its suffix array in O(n).
 *
 * lcp[i] = length of the longest common prefix of the suffixes sa[i-1] and sa[i]
 *          (neighbours in sorted order); lcp[0] = 0 by convention.
 *
 * Key lemma: if the suffix starting at i has LCP h with its predecessor in the
 * suffix array, then the suffix starting at i+1 has LCP >= h-1 with ITS predecessor.
 * So we process suffixes in text order (i = 0,1,2,...) and never reset h to 0,
 * only decrease it by 1 per step. h increases at most n times in total and
 * decreases at most n times -> O(n) character comparisons overall.
 *
 * Time O(n), auxiliary space O(n) (rank array; the lcp output is also O(n)).
 */
public final class Kasai {

    private Kasai() { }

    public static int[] lcp(String s, int[] sa, Counter c) {
        int n = s.length();
        if (sa.length != n) {
            throw new IllegalArgumentException("suffix array length " + sa.length
                    + " does not match string length " + n);
        }
        int[] lcp = new int[n];
        if (n == 0) {
            return lcp;
        }
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;          // inverse permutation of the suffix array
        }
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {       // first suffix in sorted order has no predecessor
                h = 0;
                continue;
            }
            int j = sa[rank[i] - 1];  // suffix that precedes suffix i in sorted order
            while (i + h < n && j + h < n) {
                c.comparisons++;
                if (s.charAt(i + h) != s.charAt(j + h)) {
                    break;
                }
                h++;
            }
            lcp[rank[i]] = h;
            if (h > 0) {
                h--;                  // lemma: next suffix keeps at least h-1
            }
        }
        return lcp;
    }

    public static int[] lcp(String s, int[] sa) {
        return lcp(s, sa, new Counter());
    }

    /** Brute-force LCP for verification in tests: O(n^2) worst case. */
    public static int[] lcpBruteForce(String s, int[] sa) {
        int n = s.length();
        int[] lcp = new int[n];
        for (int i = 1; i < n; i++) {
            int a = sa[i - 1];
            int b = sa[i];
            int k = 0;
            while (a + k < n && b + k < n && s.charAt(a + k) == s.charAt(b + k)) {
                k++;
            }
            lcp[i] = k;
        }
        return lcp;
    }
}
