package textanalysis.util;

/**
 * Mutable operation counter shared by the algorithms.
 * Counting costs one increment per operation, so it does not change
 * the asymptotic behaviour of the algorithms.
 */
public final class Counter {
    /** Character comparisons performed during the main (search / fill) phase. */
    public long comparisons;
    /** Character comparisons performed during preprocessing (e.g. KMP LPS table). */
    public long preprocessComparisons;
    /** Number of DP cells filled (LCS / Edit Distance). */
    public long cells;
    /** Rabin-Karp: windows whose hash equals the pattern hash. */
    public long hashHits;
    /** Rabin-Karp: hash hits that turned out NOT to be real matches (collisions). */
    public long spuriousHits;

    public void reset() {
        comparisons = preprocessComparisons = cells = hashHits = spuriousHits = 0;
    }

    public long totalComparisons() {
        return comparisons + preprocessComparisons;
    }
}
