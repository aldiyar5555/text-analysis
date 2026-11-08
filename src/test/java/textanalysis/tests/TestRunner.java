package textanalysis.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LCS;
import textanalysis.matching.KMPMatcher;
import textanalysis.matching.NaiveMatcher;
import textanalysis.matching.RabinKarpMatcher;
import textanalysis.matching.StringMatcher;
import textanalysis.suffix.Kasai;
import textanalysis.suffix.SuffixArray;
import textanalysis.util.Counter;
import textanalysis.util.RandomStrings;

/**
 * Self-contained test suite (no external dependencies, plain JDK).
 * Exit code 0 = all tests passed, 1 = at least one failure.
 */
public final class TestRunner {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    private static final StringMatcher[] MATCHERS = {
        new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()
    };

    public static void main(String[] args) {
        section("LCS");
        testLcs();
        section("Edit Distance");
        testEditDistance();
        section("String matching (Naive / KMP / Rabin-Karp)");
        testMatching();
        section("Kasai LCP");
        testKasai();

        System.out.println();
        System.out.printf("RESULT: %d passed, %d failed%n", passed, failed);
        for (String f : failures) {
            System.out.println("  FAILED: " + f);
        }
        System.exit(failed == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ LCS
    private static void testLcs() {
        check("identical strings", LCS.length("abcdef", "abcdef"), 6);
        check("completely different strings", LCS.length("abc", "xyz"), 0);
        check("first string empty", LCS.length("", "abc"), 0);
        check("second string empty", LCS.length("abc", ""), 0);
        check("both empty", LCS.length("", ""), 0);
        check("classic CLRS example ABCBDAB/BDCABA", LCS.length("ABCBDAB", "BDCABA"), 4);
        check("AGGTAB/GXTXAYB", LCS.length("AGGTAB", "GXTXAYB"), 4);
        check("reconstructed LCS AGGTAB/GXTXAYB", LCS.subsequence("AGGTAB", "GXTXAYB"), "GTAB");
        // property: reconstructed string is a subsequence of both and has the right length
        RandomStrings rnd = new RandomStrings(7, "abc");
        boolean ok = true;
        for (int t = 0; t < 200; t++) {
            String a = rnd.next(rnd.nextInt(30));
            String b = rnd.next(rnd.nextInt(30));
            String s = LCS.subsequence(a, b);
            ok &= s.length() == LCS.length(a, b) && isSubsequence(s, a) && isSubsequence(s, b)
                    && LCS.length(a, b) == LCS.length(b, a);
        }
        check("200 random pairs: valid, symmetric reconstruction", ok, true);
    }

    // -------------------------------------------------------- Edit Distance
    private static void testEditDistance() {
        check("identical strings", EditDistance.distance("algorithm", "algorithm"), 0);
        check("first string empty (= insertions)", EditDistance.distance("", "abcd"), 4);
        check("second string empty (= deletions)", EditDistance.distance("abcd", ""), 4);
        check("both empty", EditDistance.distance("", ""), 0);
        check("one substitution cat->cut", EditDistance.distance("cat", "cut"), 1);
        check("one insertion cat->cats", EditDistance.distance("cat", "cats"), 1);
        check("one deletion cats->cat", EditDistance.distance("cats", "cat"), 1);
        check("mixed kitten->sitting (2 sub + 1 ins)", EditDistance.distance("kitten", "sitting"), 3);
        check("mixed intention->execution", EditDistance.distance("intention", "execution"), 5);
        check("sunday->saturday", EditDistance.distance("sunday", "saturday"), 3);
        RandomStrings rnd = new RandomStrings(11, "abc");
        boolean ok = true;
        for (int t = 0; t < 200; t++) {
            String a = rnd.next(rnd.nextInt(25));
            String b = rnd.next(rnd.nextInt(25));
            int d = EditDistance.distance(a, b);
            // symmetry and standard bounds: |n-m| <= d <= max(n,m)
            ok &= d == EditDistance.distance(b, a)
                    && d >= Math.abs(a.length() - b.length())
                    && d <= Math.max(a.length(), b.length());
        }
        check("200 random pairs: symmetry and bounds", ok, true);
    }

    // ------------------------------------------------------ String matching
    private static void testMatching() {
        matchCase("pattern at the beginning", "hello world", "hello", List.of(0));
        matchCase("pattern in the middle", "hello world", "lo w", List.of(3));
        matchCase("pattern at the end", "hello world", "world", List.of(6));
        matchCase("multiple occurrences", "abracadabra", "abra", List.of(0, 7));
        matchCase("overlapping occurrences", "aaaaa", "aa", List.of(0, 1, 2, 3));
        matchCase("periodic pattern", "abababab", "abab", List.of(0, 2, 4));
        matchCase("pattern absent", "hello world", "xyz", List.of());
        matchCase("pattern longer than text", "abc", "abcd", List.of());
        matchCase("empty pattern (convention: no matches)", "abc", "", List.of());
        matchCase("empty text", "", "a", List.of());
        matchCase("pattern equals text", "pattern", "pattern", List.of(0));
        matchCase("single character", "banana", "a", List.of(1, 3, 5));

        check("KMP LPS of 'aabaaab'", Arrays.toString(KMPMatcher.buildLps("aabaaab")),
                Arrays.toString(new int[] {0, 1, 0, 1, 2, 2, 3}));

        // Rabin-Karp with a tiny modulus -> many collisions, but results must stay correct
        RabinKarpMatcher weak = new RabinKarpMatcher(256, 7);
        String collText = new RandomStrings(99, "abcd").next(300);
        Counter c = new Counter();
        List<Integer> r = weak.search(collText, "abcd", c);
        check("Rabin-Karp mod=7: correct despite collisions", r, new NaiveMatcher().search(collText, "abcd"));
        check("Rabin-Karp mod=7: collisions were detected and rejected", c.spuriousHits > 0, true);

        // all three algorithms agree on many random inputs
        RandomStrings rnd = new RandomStrings(3, "ab");
        boolean agree = true;
        for (int t = 0; t < 500; t++) {
            String text = rnd.next(rnd.nextInt(60));
            String pat = rnd.next(1 + rnd.nextInt(5));
            List<Integer> expected = MATCHERS[0].search(text, pat);
            for (StringMatcher m : MATCHERS) {
                agree &= m.search(text, pat).equals(expected);
            }
            agree &= weak.search(text, pat).equals(expected);
        }
        check("500 random cases: Naive == KMP == Rabin-Karp", agree, true);
    }

    private static void matchCase(String name, String text, String pattern, List<Integer> expected) {
        List<List<Integer>> results = new ArrayList<>();
        for (StringMatcher m : MATCHERS) {
            List<Integer> res = m.search(text, pattern);
            results.add(res);
            check(name + " [" + m.name() + "]", res, expected);
        }
        boolean same = results.get(0).equals(results.get(1)) && results.get(1).equals(results.get(2));
        check(name + " [all three return the same list]", same, true);
    }

    // ---------------------------------------------------------------- Kasai
    private static void testKasai() {
        // banana: suffixes sorted
        //   5 a
        //   3 ana
        //   1 anana
        //   0 banana
        //   4 na
        //   2 nana
        String s = "banana";
        int[] expectedSa = {5, 3, 1, 0, 4, 2};
        int[] expectedLcp = {0, 1, 3, 0, 0, 2};
        check("banana: suffix array (naive construction)",
                Arrays.toString(SuffixArray.buildNaive(s)), Arrays.toString(expectedSa));
        check("banana: suffix array (prefix doubling)",
                Arrays.toString(SuffixArray.build(s)), Arrays.toString(expectedSa));
        check("banana: LCP from manually verified SA",
                Arrays.toString(Kasai.lcp(s, expectedSa)), Arrays.toString(expectedLcp));

        // mississippi: SA = [10,7,4,1,0,9,8,6,3,5,2], LCP = [0,1,1,4,0,0,1,0,2,1,3]
        String t = "mississippi";
        int[] saM = {10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2};
        int[] lcpM = {0, 1, 1, 4, 0, 0, 1, 0, 2, 1, 3};
        check("mississippi: suffix array", Arrays.toString(SuffixArray.build(t)), Arrays.toString(saM));
        check("mississippi: LCP", Arrays.toString(Kasai.lcp(t, saM)), Arrays.toString(lcpM));

        check("single character", Arrays.toString(Kasai.lcp("a", new int[] {0})), "[0]");
        check("empty string", Kasai.lcp("", new int[0]).length, 0);
        check("aaaa: LCP", Arrays.toString(Kasai.lcp("aaaa", SuffixArray.build("aaaa"))), "[0, 1, 2, 3]");

        RandomStrings rnd = new RandomStrings(5, "abc");
        boolean ok = true;
        for (int k = 0; k < 100; k++) {
            String x = rnd.next(1 + rnd.nextInt(80));
            int[] sa = SuffixArray.build(x);
            ok &= Arrays.equals(sa, SuffixArray.buildNaive(x));
            ok &= Arrays.equals(Kasai.lcp(x, sa), Kasai.lcpBruteForce(x, sa));
        }
        check("100 random strings: Kasai == brute-force LCP", ok, true);

        boolean threw = false;
        try {
            Kasai.lcp("abc", new int[] {0, 1});
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("mismatched SA length is rejected", threw, true);
    }

    // -------------------------------------------------------------- helpers
    private static boolean isSubsequence(String s, String of) {
        int i = 0;
        for (int j = 0; j < of.length() && i < s.length(); j++) {
            if (s.charAt(i) == of.charAt(j)) {
                i++;
            }
        }
        return i == s.length();
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private static void check(String name, Object actual, Object expected) {
        if (expected.equals(actual)) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            String msg = name + " -> expected " + expected + ", got " + actual;
            failures.add(msg);
            System.out.println("  [FAIL] " + msg);
        }
    }
}
