package textanalysis.matching;

import java.util.List;
import textanalysis.util.Counter;

/**
 * Common interface for exact string-matching algorithms.
 *
 * Conventions (identical for every implementation):
 *  - returns ALL starting indices (0-based, ascending), overlapping occurrences included;
 *  - empty pattern  -> empty list (we treat an empty pattern as "nothing to search for");
 *  - pattern longer than text -> empty list.
 */
public interface StringMatcher {

    String name();

    List<Integer> search(String text, String pattern, Counter counter);

    default List<Integer> search(String text, String pattern) {
        return search(text, pattern, new Counter());
    }
}
