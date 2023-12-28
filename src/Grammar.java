import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grammar {
    public static Set<Character> terminals = new HashSet<>();
    public static Set<Character> nonterminals = new HashSet<>();

    public static String start = null;

    public static Map<String, List<String>> transitions = new HashMap<>();

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
                nonterminals.add(left.charAt(0));
                for (Character c : right.toCharArray()) {
                    if ('A' <= c && c <= 'Z') {
                        nonterminals.add(c);
                    } else {
                        terminals.add(c);
                    }
                }

                transitions.compute(left, (k, v) -> {
                    if(v==null) {
                        List<String> t = new ArrayList<>();
                        t.add(right);
                        return t;
                    } else {
                        v.add(right);
                        return v;
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
