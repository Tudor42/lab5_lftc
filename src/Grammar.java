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
                parts[0] = parts[0].strip();
                parts[1] = parts[1].strip();

                if(start == null) {
                    start = parts[0];
                }
                nonterminals.add(parts[0].charAt(0));
                for (Character c : parts[1].toCharArray()) {
                    if ('A' <= c && c <= 'Z') {
                        nonterminals.add(c);
                    } else {
                        terminals.add(c);
                    }
                }

                transitions.compute(parts[0], (k, v) -> {
                    if(v==null) {
                        List<String> t = new ArrayList<>();
                        t.add(parts[1]);
                        return t;
                    } else {
                        v.add(parts[1]);
                        return v;
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
