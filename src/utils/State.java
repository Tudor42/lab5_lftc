package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class State {
    public Set<Item> items = new HashSet<>();

    public Map<String, State> transitions = new HashMap<>();

    @Override
    public String toString() {
        return "State{" +
                "items=" + items + '}';
    }
}
