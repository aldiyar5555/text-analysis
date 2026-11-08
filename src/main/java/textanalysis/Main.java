package textanalysis;

import java.util.Arrays;
import textanalysis.dp.EditDistance;
import textanalysis.dp.LCS;
import textanalysis.matching.KMPMatcher;
import textanalysis.matching.NaiveMatcher;
import textanalysis.matching.RabinKarpMatcher;
import textanalysis.matching.StringMatcher;
import textanalysis.suffix.Kasai;
import textanalysis.suffix.SuffixArray;

/** Small demo of every algorithm. Usage: java -cp out textanalysis.Main */
public final class Main {

    public static void main(String[] args) {
        String a = "AGGTAB";
        String b = "GXTXAYB";
        System.out.println("LCS(" + a + ", " + b + ") = " + LCS.length(a, b)
                + "  -> \"" + LCS.subsequence(a, b) + "\"");
        System.out.println("EditDistance(kitten, sitting) = " + EditDistance.distance("kitten", "sitting"));

        String text = "abracadabra";
        String pattern = "abra";
        for (StringMatcher m : new StringMatcher[] {new NaiveMatcher(), new KMPMatcher(), new RabinKarpMatcher()}) {
            System.out.println(m.name() + " search(\"" + text + "\", \"" + pattern + "\") = "
                    + m.search(text, pattern));
        }

        String s = "banana";
        int[] sa = SuffixArray.build(s);
        System.out.println("SA(banana)  = " + Arrays.toString(sa));
        System.out.println("LCP(banana) = " + Arrays.toString(Kasai.lcp(s, sa)));
    }
}
