package textanalysis.suffix;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Suffix array construction helpers. Not part of the graded algorithm
 * (the assignment allows a simple construction); used to PRODUCE the input
 * for Kasai's algorithm. Never timed in the experiments.
 */
public final class SuffixArray {

    private SuffixArray() { }

    /** Simplest possible construction: sort suffixes as strings. O(n^2 log n). For tests. */
    public static int[] buildNaive(String s) {
        int n = s.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) {
            idx[i] = i;
        }
        Arrays.sort(idx, (x, y) -> s.substring(x).compareTo(s.substring(y)));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = idx[i];
        }
        return sa;
    }

    /**
     * Prefix doubling: sort suffixes by their first 2^k characters using the ranks of
     * the first 2^(k-1) characters. O(n log^2 n). Used for the larger benchmark strings.
     */
    public static int[] build(String s) {
        int n = s.length();
        if (n == 0) {
            return new int[0];
        }
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] tmp = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = s.charAt(i);
        }
        for (int k = 1; ; k <<= 1) {
            final int step = k;
            final int[] r = rank;
            Comparator<Integer> cmp = (x, y) -> {
                if (r[x] != r[y]) {
                    return Integer.compare(r[x], r[y]);
                }
                int rx = x + step < n ? r[x + step] : -1;
                int ry = y + step < n ? r[y + step] : -1;
                return Integer.compare(rx, ry);
            };
            Arrays.sort(sa, cmp);
            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp.compare(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            System.arraycopy(tmp, 0, rank, 0, n);
            if (rank[sa[n - 1]] == n - 1 || step >= n) {
                break;   // all ranks distinct -> fully sorted
            }
        }
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = sa[i];
        }
        return result;
    }
}
