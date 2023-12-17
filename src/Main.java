import utils.Item;

import java.util.*;
import utils.*;

public class Main {
    static String textToParse = "asad21$";

    public static void main(String[] args) {
        Grammar.parse_file("grammar");
        Grammar.transitions.put("augmented_grammar_start_var", List.of(Grammar.start));
        List<State> states = states();
        for(int i = 0; i < textToParse.length(); ++i){
            System.out.println(textToParse.charAt(i));
        }
    }

    private static List<State> states() {
        List<State> states = new ArrayList<>();
        State initialState = new State();

        for(var key: Grammar.transitions.keySet()) {
            for(String s: Grammar.transitions.get(key)){
                if(!s.isEmpty()) {
                    initialState.items.add(new Item(key, s));
                }
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
                boolean changeFlag;
                do {
                    changeFlag = false;
                    HashSet<Item> temp = new HashSet<>();
                    for (Item item : nextState.items) {
                        if (item.dotPosition < item.to.length() && Grammar.nonterminals.contains(item.to.charAt(item.dotPosition))) {
                            Grammar.transitions.get(String.valueOf(item.to.charAt(item.dotPosition))).forEach(rule->{
                                if(!rule.isEmpty()){
                                    temp.add(new Item(item.to.substring(item.dotPosition, item.dotPosition + 1), rule));
                                }
                            });
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
                }
            }
        }
        return states;
    }


    private static Map<String, Set<String>> first;

    static class FunctionState{
        public Set<String> res;
        public String symbol;
        public Integer currTransaction;
    }

    private static Set<String> first(String symbol){
        if(Grammar.terminals.contains(symbol.charAt(0))){
            return Set.of(symbol);
        }

        if (first.containsKey(symbol)) {
            return first.get(symbol);
        }

        Stack<FunctionState> functionStates = new Stack<>();

        while(true) {
            int i = 0;
            Set<String> res = new HashSet<>();
            startFunc: while(i < Grammar.transitions.get(symbol).size()){
                String transition = Grammar.transitions.get(symbol).get(i);
                if(transition.isEmpty()){
                    res.add("");
                }
                for(int j = 0; j < transition.length(); ++j){
                    if(Grammar.terminals.contains(transition.charAt(j))){
                        res.add(transition.substring(j, j+1));
                        break;
                    }
                    if(Grammar.nonterminals.contains(transition.charAt(j))){
                        if(first.containsKey(transition.substring(j, j+1))){
                            res.addAll(first.get(transition.substring(j, j+1)));
                            if(!first.get(transition.substring(j, j+1)).contains("")){
                                break;
                            }
                        }else{
                            FunctionState func = new FunctionState();
                            func.res = res;
                            func.currTransaction = i;
                            func.symbol = symbol;
                            functionStates.push(func);
                            res = new HashSet<>();
                            i = 0;
                            symbol = transition.substring(j, j+1);
                            continue startFunc;
                        }
                    }
                }
                // Add to first
                // restore func state if present in stack
                // continue to startFunc tag

            }
        }
    }
}