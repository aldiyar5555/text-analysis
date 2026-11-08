package textanalysis.dp;

import textanalysis.util.Counter;

/**
 * Levenshtein (edit) distance, bottom-up dynamic programming.
 *
 * dp[i][j] = minimum number of insertions, deletions and substitutions
 *            needed to turn a[0..i) into b[0..j)
 *
 * Base cases: dp[i][0] = i  (delete all i characters)
 *             dp[0][j] = j  (insert all j characters)
 * Recurrence:
 *   a[i-1] == b[j-1]  ->  dp[i][j] = dp[i-1][j-1]
 *   otherwise         ->  dp[i][j] = 1 + min(dp[i-1][j]     (delete a[i-1]),
 *                                            dp[i][j-1]     (insert b[j-1]),
 *                                            dp[i-1][j-1])  (substitute)
 *
 * Time O(n*m), space O(n*m).
 */
public final class EditDistance {

    private EditDistance() { }

    public static int[][] table(String a, String b, Counter counter) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                if (ca == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int delete = dp[i - 1][j];
                    int insert = dp[i][j - 1];
                    int substitute = dp[i - 1][j - 1];
                    dp[i][j] = 1 + Math.min(substitute, Math.min(delete, insert));
                }
            }
        }
        if (counter != null) {
            counter.cells += (long) n * m;
            counter.comparisons += (long) n * m;
        }
        return dp;
    }

    public static int distance(String a, String b, Counter counter) {
        return table(a, b, counter)[a.length()][b.length()];
    }

    public static int distance(String a, String b) {
        return distance(a, b, null);
    }
}
