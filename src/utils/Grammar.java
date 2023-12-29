package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grammar {
    public static Set<String> terminals = new HashSet<>();
    public static Set<String> nonterminals = new HashSet<>();

    public static String start = null;

    public static Map<String, List<TokenSequence>> transitions = new HashMap<>();

    public static class TokenSequence extends ArrayList<String>{}

    public static void parse_file(String filename) {
        nonterminals.clear();
        transitions.clear();
        terminals.clear();
        try (BufferedReader bufferedReader
                     = new BufferedReader(new FileReader("resources/" + filename))) {
            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                String[] parts = tmp.split("->");
                String left, right;
                if(parts.length == 2) {
                    left = parts[0].strip();
                    right = parts[1].strip();
                }else {
                    left = parts[0].strip();
                    right = "";
                }

                if(start == null) {
                    start = left;
                }
                nonterminals.add(left);

                transitions.compute(left, (k, v) -> {
                    TokenSequence tt = new TokenSequence();
                    tt.addAll(Arrays.asList(right.split(" +")));

                    if(v==null) {
                        List<TokenSequence> t = new ArrayList<>();
                        t.add(tt);
                        return t;
                    } else {
                        v.add(tt);
                        return v;
                    }
                });
            }
            transitions.values().forEach(e->e.forEach(i->{
                Set<String> tmpCol = new HashSet<>(i);
                tmpCol.removeAll(nonterminals);
                terminals.addAll(tmpCol);
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
