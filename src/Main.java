import utils.Item;

import java.util.*;
import utils.*;

public class Main {
    static String textToParse = "asad21$";

    public static void main(String[] args) {
        Grammar.parse_file("grammar");
        Grammar.transitions.put("augmented_grammar_start_var", List.of(Grammar.start));
        List<State> states = new ArrayList<>();

        // CREATE states

        State initialState = new State();

        for(var key: Grammar.transitions.keySet()) {
            for(String s: Grammar.transitions.get(key)){
                initialState.items.add(new Item(key, s));
            }
        }

        states.add(initialState);

        for(int i = 0; i < states.size(); ++i){
            Set<String> stringWithDot = new HashSet<>();
            for(Item item: states.get(i).items){
                if(item.dotPosition < item.to.length()) {
                    stringWithDot.add(item.to.substring(item.dotPosition, item.dotPosition + 1));
                }
            }
            for(String s: stringWithDot){
                HashSet<Item> nextStateItems = new HashSet<>();
                for(Item item: states.get(i).items){
                    if(item.dotPosition < item.to.length() && item.to.substring(item.dotPosition, item.dotPosition + 1).equals(s)) {
                        Item newItem = new Item(item.nonTerminal, item.to);
                        newItem.dotPosition = item.dotPosition + 1;
                        nextStateItems.add(newItem);
                    }
                }
                State nextState = new State();
                nextState.items = nextStateItems;
                boolean changeFlag = false;
                do {
                    changeFlag = false;
                    HashSet<Item> temp = new HashSet<>();
                    for (Item item : nextState.items) {
                        if (item.dotPosition < item.to.length() && Grammar.nonterminals.contains(item.to.charAt(item.dotPosition))) {
                            Grammar.transitions.get(String.valueOf(item.to.charAt(item.dotPosition))).forEach(rule->
                                temp.add(new Item(item.to.substring(item.dotPosition, item.dotPosition + 1), rule)));
                        }
                    }
                    if(!nextState.items.containsAll(temp)){
                        nextState.items.addAll(temp);
                        changeFlag = true;
                    }
                } while (changeFlag);

                boolean exists = false;
                for (State state : states) {
                    if (state.items.containsAll(nextState.items) && nextState.items.containsAll(state.items)) {
                        exists = true;
                        states.get(i).transitions.put(s, state);
                    }
                }
                if (!exists) {
                    states.add(nextState);
                    states.get(i).transitions.put(s, nextState);
                    System.out.println(nextState);
                }
            }
        }
    }
}