package textanalysis.bench;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
 * Runs all experiments and writes CSV files + a Markdown summary into results/.
 *
 * Methodology:
 *  - all inputs generated BEFORE timing with a fixed seed (42) and alphabet {a,b,c,d};
 *  - a global JIT warm-up phase runs every algorithm many times before ANY measurement,
 *    then WARMUP more untimed runs precede each configuration (steady-state timing);
 *  - RUNS = 5 timed runs, System.nanoTime() around the algorithm call only;
 *  - the average of the 5 runs is reported (plus min and max);
 *  - operation counts come from one extra, untimed, instrumented run
 *    (the counters are deterministic, so one run is enough);
 *  - printing and file writing happen only after all timing is done.
 */
public final class Benchmark {

    private static final int RUNS = 5;
    private static final int WARMUP = 10;
    private static final int PATTERN_LENGTH = 16;

    private static final int[] DP_SIZES = {100, 500, 1_000};
    private static final int[] TEXT_SIZES = {10_000, 50_000, 100_000};
    private static final int[] KASAI_SIZES = {1_000, 5_000, 10_000};

    /** Sink so that the JIT cannot eliminate the measured calls as dead code. */
    private static long sink = 0;

    @FunctionalInterface
    private interface Task {
        long run();
    }

    private record Timing(double avgMs, double minMs, double maxMs) { }

    private static Timing time(Task task) {
        for (int i = 0; i < WARMUP; i++) {
            sink += task.run();
        }
        double total = 0;
        double min = Double.MAX_VALUE;
        double max = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            long r = task.run();
            long elapsed = System.nanoTime() - start;
            sink += r;
            double ms = elapsed / 1_000_000.0;
            total += ms;
            min = Math.min(min, ms);
            max = Math.max(max, ms);
        }
        return new Timing(total / RUNS, min, max);
    }

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);

        warmUpJit();
        List<String> dpRows = runDp();
        List<String> matchRows = runMatching();
        List<String> kasaiRows = runKasai();

        // ---- output (outside of any timed region) ----
        write(outDir.resolve("dp.csv"),
                "algorithm,n,avg_ms,min_ms,max_ms,cells,result", dpRows);
        write(outDir.resolve("matching.csv"),
                "scenario,algorithm,text_length,pattern_length,avg_ms,min_ms,max_ms,"
                        + "search_comparisons,preprocess_comparisons,matches,hash_hits,spurious_hits",
                matchRows);
        write(outDir.resolve("kasai.csv"),
                "n,avg_ms,min_ms,max_ms,char_comparisons,max_lcp", kasaiRows);
        writeMarkdown(outDir.resolve("results.md"), dpRows, matchRows, kasaiRows);

        System.out.println("Java " + System.getProperty("java.version") + ", "
                + System.getProperty("os.name") + ", " + Runtime.getRuntime().availableProcessors() + " CPUs");
        System.out.println(Files.readString(outDir.resolve("results.md")));
        System.out.println("(sink=" + sink + ")");
    }

    /** Untimed: executes every algorithm repeatedly so the JIT compiles it before measuring. */
    private static void warmUpJit() {
        RandomStrings rnd = new RandomStrings(1, RandomStrings.ALPHABET);
        String a = rnd.next(500);
        String b = rnd.next(500);
        String text = rnd.next(50_000);
        String pattern = text.substring(100, 100 + PATTERN_LENGTH);
        String worst = "a".repeat(50_000);
        String worstPattern = "a".repeat(PATTERN_LENGTH - 1) + "b";
        String k = rnd.next(5_000);
        int[] sa = SuffixArray.build(k);
        StringMatcher[] matchers = {new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()};
        for (int it = 0; it < 40; it++) {
            sink += LCS.length(a, b, null) + EditDistance.distance(a, b, null);
            for (StringMatcher m : matchers) {
                sink += m.search(text, pattern, new Counter()).size();
                sink += m.search(worst, worstPattern, new Counter()).size();
            }
            sink += Kasai.lcp(k, sa, new Counter())[1];
        }
    }

    // ------------------------------------------------------------- DP
    private static List<String> runDp() {
        List<String> rows = new ArrayList<>();
        RandomStrings rnd = new RandomStrings();
        for (int n : DP_SIZES) {
            String a = rnd.next(n);           // generated outside timing
            String b = rnd.next(n);

            Timing tl = time(() -> LCS.length(a, b, null));
            Counter cl = new Counter();
            int lcs = LCS.length(a, b, cl);
            rows.add(String.format(Locale.US, "LCS,%d,%.4f,%.4f,%.4f,%d,%d",
                    n, tl.avgMs, tl.minMs, tl.maxMs, cl.cells, lcs));

            Timing te = time(() -> EditDistance.distance(a, b, null));
            Counter ce = new Counter();
            int ed = EditDistance.distance(a, b, ce);
            rows.add(String.format(Locale.US, "EditDistance,%d,%.4f,%.4f,%.4f,%d,%d",
                    n, te.avgMs, te.minMs, te.maxMs, ce.cells, ed));
        }
        return rows;
    }

    // ------------------------------------------------------- matching
    private static List<String> runMatching() {
        List<String> rows = new ArrayList<>();
        StringMatcher[] matchers = {new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()};
        NaiveMatcher reference = new NaiveMatcher();
        RandomStrings rnd = new RandomStrings();

        for (int n : TEXT_SIZES) {
            String text = rnd.next(n);
            // present: a substring copied from the middle of the text
            String present = text.substring(n / 2, n / 2 + PATTERN_LENGTH);
            // absent: random pattern, regenerated until it does not occur (checked untimed)
            String absent;
            do {
                absent = rnd.next(PATTERN_LENGTH);
            } while (!reference.search(text, absent).isEmpty());

            measure(rows, "present", text, present, matchers);
            measure(rows, "absent", text, absent, matchers);

            // extra: adversarial input that triggers the naive worst case
            String worstText = "a".repeat(n);
            String worstPattern = "a".repeat(PATTERN_LENGTH - 1) + "b";
            measure(rows, "worst_case", worstText, worstPattern, matchers);
        }
        return rows;
    }

    private static void measure(List<String> rows, String scenario, String text, String pattern,
                                StringMatcher[] matchers) {
        List<Integer> expected = null;
        for (StringMatcher m : matchers) {
            Timing t = time(() -> m.search(text, pattern, new Counter()).size());
            Counter c = new Counter();
            List<Integer> found = m.search(text, pattern, c);
            if (expected == null) {
                expected = found;
            } else if (!expected.equals(found)) {
                throw new IllegalStateException(m.name() + " disagrees on scenario " + scenario);
            }
            rows.add(String.format(Locale.US, "%s,%s,%d,%d,%.4f,%.4f,%.4f,%d,%d,%d,%d,%d",
                    scenario, m.name(), text.length(), pattern.length(), t.avgMs, t.minMs, t.maxMs,
                    c.comparisons, c.preprocessComparisons, found.size(), c.hashHits, c.spuriousHits));
        }
    }

    // ---------------------------------------------------------- Kasai
    private static List<String> runKasai() {
        List<String> rows = new ArrayList<>();
        RandomStrings rnd = new RandomStrings();
        for (int n : KASAI_SIZES) {
            String s = rnd.next(n);
            int[] sa = SuffixArray.build(s);   // suffix array is INPUT, not timed
            Timing t = time(() -> Kasai.lcp(s, sa, new Counter())[n - 1]);
            Counter c = new Counter();
            int[] lcp = Kasai.lcp(s, sa, c);
            int maxLcp = 0;
            for (int v : lcp) {
                maxLcp = Math.max(maxLcp, v);
            }
            rows.add(String.format(Locale.US, "%d,%.4f,%.4f,%.4f,%d,%d",
                    n, t.avgMs, t.minMs, t.maxMs, c.comparisons, maxLcp));
        }
        return rows;
    }

    // -------------------------------------------------------- output
    private static void write(Path file, String header, List<String> rows) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println(header);
            rows.forEach(w::println);
        }
    }

    private static void writeMarkdown(Path file, List<String> dp, List<String> match, List<String> kasai)
            throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println("### Dynamic programming (average of " + RUNS + " runs)");
            w.println();
            w.println("| Algorithm | n = m | Avg time (ms) | Min (ms) | Max (ms) | DP cells | Result |");
            w.println("|---|---:|---:|---:|---:|---:|---:|");
            for (String r : dp) {
                w.println("| " + String.join(" | ", r.split(",")) + " |");
            }
            w.println();
            w.println("### String matching (pattern length " + PATTERN_LENGTH + ", average of " + RUNS + " runs)");
            w.println();
            w.println("| Scenario | Algorithm | Text length | Avg time (ms) | Search comparisons "
                    + "| Preprocess comparisons | Matches | Hash hits | Spurious hits |");
            w.println("|---|---|---:|---:|---:|---:|---:|---:|---:|");
            for (String r : match) {
                String[] f = r.split(",");
                w.printf("| %s | %s | %s | %s | %s | %s | %s | %s | %s |%n",
                        f[0], f[1], f[2], f[4], f[7], f[8], f[9], f[10], f[11]);
            }
            w.println();
            w.println("### Kasai LCP (suffix array given as input, average of " + RUNS + " runs)");
            w.println();
            w.println("| n | Avg time (ms) | Min (ms) | Max (ms) | Char comparisons | Max LCP |");
            w.println("|---:|---:|---:|---:|---:|---:|");
            for (String r : kasai) {
                w.println("| " + String.join(" | ", r.split(",")) + " |");
            }
        }
    }
}
