package textanalysis.dp;

import textanalysis.util.Counter;

/**
 * Longest Common Subsequence (bottom-up dynamic programming).
 *
 * dp[i][j] = length of the LCS of a[0..i) and b[0..j)
 *
 * Base cases: dp[0][j] = dp[i][0] = 0  (LCS with an empty prefix is empty)
 * Recurrence:
 *   a[i-1] == b[j-1]  ->  dp[i][j] = dp[i-1][j-1] + 1
 *   otherwise         ->  dp[i][j] = max(dp[i-1][j], dp[i][j-1])
 *
 * Time O(n*m), space O(n*m) for the full table (needed for reconstruction).
 */
public final class LCS {

    private LCS() { }

    /** Fills and returns the full (n+1) x (m+1) DP table. */
    public static int[][] table(String a, String b, Counter counter) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];   // row 0 and column 0 stay 0 (base cases)
        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                if (ca == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        if (counter != null) {
            counter.cells += (long) n * m;
            counter.comparisons += (long) n * m;
        }
        return dp;
    }

    /** Length of the longest common subsequence. */
    public static int length(String a, String b, Counter counter) {
        return table(a, b, counter)[a.length()][b.length()];
    }

    public static int length(String a, String b) {
        return length(a, b, null);
    }

    /** Reconstructs one LCS by walking the table back from dp[n][m]. O(n + m) extra time. */
    public static String subsequence(String a, String b) {
        int[][] dp = table(a, b, null);
        StringBuilder sb = new StringBuilder();
        int i = a.length();
        int j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return sb.reverse().toString();
    }
}
