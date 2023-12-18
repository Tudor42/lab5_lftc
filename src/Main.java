import utils.Item;

import java.util.*;
import java.util.regex.Pattern;

import utils.*;

public class Main {
    static String textToParse = "asad21$";

    public static void main(String[] args) {
        Grammar.parse_file("grammar");
        Grammar.transitions.put("augmented_grammar_start_var", List.of(Grammar.start));
        first("R").forEach(System.out::println);
        calcFollow();
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


    private static Map<String, Set<String>> first = new HashMap<>();

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
            return new HashSet<>(first.get(symbol));
        }

        Stack<FunctionState> functionStates = new Stack<>();
        int i = 0;
        Set<String> res = new HashSet<>();
        while(true) {
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
                i++;
            }
            first.put(symbol, new HashSet<>(res));
            if(functionStates.isEmpty()){
                return res;
            }
            FunctionState f = functionStates.pop();
            res = f.res;
            i = f.currTransaction;
            symbol = f.symbol;
        }
    }

    private static Map<String, Set<String>> follow = new HashMap<>();

    private static void calcFollow(){
        for(Character c: Grammar.nonterminals){
            follow.put(String.valueOf(c), new HashSet<>());
        }
        follow.get(Grammar.start).add("$");

        while (true) {
            boolean changed = false;
            for(Character nonterminal: Grammar.nonterminals){
                for(var rule: Grammar.transitions.entrySet()){
                    for(String rh: rule.getValue()){
                        if(!rh.contains(String.valueOf(nonterminal))){
                            continue;
                        }
                        List<Integer> indexes = new ArrayList<>();
                        for(int i = 0; i < rh.length(); ++i){
                            if(rh.charAt(i) == nonterminal)
                                indexes.add(i);
                        }
                        for(Integer index: indexes){
                            if(index == rh.length() - 1){
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                tmp.removeAll(follow.get(String.valueOf(nonterminal)));
                                if(!tmp.isEmpty()){
                                    follow.get(String.valueOf(nonterminal)).addAll(tmp);
                                    changed = true;
                                }
                                continue;
                            }
                            String symbol = rh.substring(index + 1, index + 2);
                            if(Grammar.terminals.contains(symbol.charAt(0))){
                                changed = follow.get(String.valueOf(nonterminal)).add(symbol);
                            }
                            Set<String> first = first(symbol);
                            if(first.contains("")){
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                tmp.removeAll(follow.get(String.valueOf(nonterminal)));
                                if(!tmp.isEmpty()){
                                    follow.get(String.valueOf(nonterminal)).addAll(tmp);
                                    changed = true;
                                }
                                first.remove("");
                            }
                            first.removeAll(follow.get(String.valueOf(nonterminal)));
                            if(!first.isEmpty()){
                                follow.get(String.valueOf(nonterminal)).addAll(first);
                                changed = true;
                            }
                        }

                    }
                }
            }
            if(!changed){
                break;
            }
        }
    }
}