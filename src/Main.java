import utils.Item;

import java.security.Key;
import java.util.*;

import utils.*;

public class Main {
    static String textToParse = "i*i+i$";
    private static final Map<String, Set<String>> follow = new HashMap<>();

    public static void main(String[] args) {
        Grammar.parse_file("grammar");
        Grammar.transitions.put("augmented_grammar_start_var", List.of(Grammar.start + "$"));
        Grammar.terminals.add('$');
        calcFollow();

        List<State> states = states();
        List<Map<String, Action>> actionTable = new ArrayList<>();
        List<Map<String, Integer>> goTo = new ArrayList<>();
        for (State s : states) {
            Map<String, Action> newLine = new HashMap<>();
            Map<String, Integer> goToLine = new HashMap<>();
            for (Item item : s.items) {
                if (item.to.equals(Grammar.start + "$") && item.nonTerminal.equals("augmented_grammar_start_var")) {
                    Action action = new Action();
                    action.to = item.to;
                    action.from = item.nonTerminal;
                    action.actionType = Action.ActionType.ACCEPT;
                    if (newLine.put("$", action) != null) {
                        throw new RuntimeException("Conflict");
                    }
                }
                if (item.dotPosition == item.to.length()) { // reduce
                    for (String followItem : follow.get(item.nonTerminal)) {
                        Action action = new Action();
                        action.to = item.to;
                        action.from = item.nonTerminal;
                        action.actionType = Action.ActionType.REDUCE;
                        if (newLine.put(followItem, action) != null) {
                            throw new RuntimeException("Reduce/reduce conflict");
                        }
                    }
                }
            }
            for (String use : s.transitions.keySet()) {
                if (Grammar.terminals.contains(use.charAt(0))) {
                    Action action = new Action();
                    action.index = states.indexOf(s.transitions.get(use));
                    action.actionType = Action.ActionType.SHIFT;
                    if (newLine.put(use, action) != null) {
                        throw new RuntimeException("Reduce/shift conflict");
                    }
                } else {
                    goToLine.put(use, states.indexOf(s.transitions.get(use)));
                }
            }
            actionTable.add(newLine);
            goTo.add(goToLine);
        }

        parse(actionTable, goTo);
    }

    private static void parse(List<Map<String, Action>> actionTable, List<Map<String, Integer>> goTo) {
        if (textToParse.length() == 1) {
            System.out.println("Correct text.");
            return;
        }

        int currStateIndex = 0;
        Stack<String> stack = new Stack<>();
        stack.push("0");

        while (true) {
            String currentChar = String.valueOf(textToParse.charAt(0));
            Action currAction = actionTable.get(currStateIndex).get(currentChar);

            if (currAction == null) {
                System.out.println("Can't parse the text.");
                break;
            }

            if (currAction.actionType == Action.ActionType.SHIFT) {
                stack.push(currentChar);
                textToParse = textToParse.substring(1);
                currStateIndex = currAction.index;
                stack.push(String.valueOf(currStateIndex));
            }

            if (currAction.actionType == Action.ActionType.REDUCE) {
                int toRemove = currAction.to.length() * 2;
                while (toRemove != 0) {
                    stack.pop();
                    toRemove--;
                }

                int goToIndex = Integer.parseInt(String.valueOf(stack.peek()));
                stack.push(currAction.from);
                currStateIndex = goTo.get(goToIndex).get(String.valueOf(currAction.from.charAt(0)));
                stack.push(String.valueOf(currStateIndex));
            }

            if (currAction.actionType == Action.ActionType.ACCEPT) {
                System.out.println("Correct text.");
                break;
            }
        }
    }

    private static List<State> states() {
        List<State> states = new ArrayList<>();
        State initialState = new State();

        for (var key : Grammar.transitions.keySet()) {
            for (String s : Grammar.transitions.get(key)) {
                if (!s.isEmpty()) {
                    initialState.items.add(new Item(key, s));
                }
            }
        }

        states.add(initialState);

        for (int i = 0; i < states.size(); ++i) {
            Set<String> stringWithDot = new HashSet<>();
            for (Item item : states.get(i).items) {
                boolean startVarCondition = item.nonTerminal.equals("augmented_grammar_start_var") && item.dotPosition < item.to.length() - 1;
                boolean restOfVarsCondition = !item.nonTerminal.equals("augmented_grammar_start_var") && item.dotPosition < item.to.length();
                if (startVarCondition || restOfVarsCondition) {
                    stringWithDot.add(item.to.substring(item.dotPosition, item.dotPosition + 1));
                }
            }
            for (String s : stringWithDot) {
                HashSet<Item> nextStateItems = new HashSet<>();
                for (Item item : states.get(i).items) {
                    if (item.dotPosition < item.to.length() && item.to.substring(item.dotPosition, item.dotPosition + 1).equals(s)) {
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
                            Grammar.transitions.get(String.valueOf(item.to.charAt(item.dotPosition))).forEach(rule -> {
                                if (!rule.isEmpty()) {
                                    temp.add(new Item(item.to.substring(item.dotPosition, item.dotPosition + 1), rule));
                                }
                            });
                        }
                    }
                    if (!nextState.items.containsAll(temp)) {
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

    private static final Map<String, Set<String>> first = new HashMap<>();

    static class FunctionState {
        public Set<String> res;
        public String symbol;
        public Integer currTransition;
    }

    private static Set<String> first(String symbol) {
        if (Grammar.terminals.contains(symbol.charAt(0))) {
            return new HashSet<>(Set.of(symbol));
        }

        if (first.containsKey(symbol)) {
            return new HashSet<>(first.get(symbol));
        }

        Stack<FunctionState> functionStates = new Stack<>();
        int i = 0;
        Set<String> res = new HashSet<>();
        while (true) {
            startFunc:
            while (i < Grammar.transitions.get(symbol).size()) {
                String transition = Grammar.transitions.get(symbol).get(i);
                if (transition.isEmpty()) {
                    res.add("");
                }
                for (int j = 0; j < transition.length(); ++j) {
                    if (Grammar.terminals.contains(transition.charAt(j))) {
                        res.add(transition.substring(j, j + 1));
                        break;
                    }
                    if (Grammar.nonterminals.contains(transition.charAt(j))) {
                        if (first.containsKey(transition.substring(j, j + 1))) {
                            res.addAll(first.get(transition.substring(j, j + 1)));
                            if (!first.get(transition.substring(j, j + 1)).contains("")) {
                                break;
                            }
                        } else {
                            FunctionState func = new FunctionState();
                            func.res = res;
                            func.currTransition = i;
                            func.symbol = symbol;
                            functionStates.push(func);
                            res = new HashSet<>();
                            i = 0;
                            symbol = transition.substring(j, j + 1);
                            continue startFunc;
                        }
                    }
                }
                i++;
            }
            first.put(symbol, new HashSet<>(res));
            if (functionStates.isEmpty()) {
                return res;
            }
            FunctionState f = functionStates.pop();
            res = f.res;
            i = f.currTransition;
            symbol = f.symbol;
        }
    }

    private static void calcFollow() {
        Map<String, Set<String>> keepAccount = new HashMap<>();
        for (Character c : Grammar.nonterminals) {
            follow.put(String.valueOf(c), new HashSet<>());
            keepAccount.put(String.valueOf(c), new HashSet<>());
        }
        follow.get(Grammar.start).add("$");

        while (true) {
            boolean changed = false;
            for (Character nonterminal : Grammar.nonterminals) {
                for (var rule : Grammar.transitions.entrySet()) {
                    for (String rh : rule.getValue()) {
                        if (!rh.contains(String.valueOf(nonterminal))) {
                            continue;
                        }
                        List<Integer> indexes = new ArrayList<>();
                        for (int i = 0; i < rh.length(); ++i) {
                            if (rh.charAt(i) == nonterminal)
                                indexes.add(i);
                        }
                        for (Integer index : indexes) {
                            if (index == rh.length() - 1) {
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                changed = follow.get(String.valueOf(nonterminal)).addAll(tmp);
                                keepAccount.get(String.valueOf(nonterminal)).add(rule.getKey());
                                continue;
                            }
                            String symbol = rh.substring(index + 1, index + 2);
                            if (Grammar.terminals.contains(symbol.charAt(0))) {
                                changed = follow.get(String.valueOf(nonterminal)).add(symbol);
                            }
                            Set<String> first = first(symbol);
                            if (first.contains("")) {
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                changed = follow.get(String.valueOf(nonterminal)).addAll(tmp);
                                first.remove("");
                            }
                            first.removeAll(follow.get(String.valueOf(nonterminal)));
                            if (!first.isEmpty()) {
                                follow.get(String.valueOf(nonterminal)).addAll(first);
                                changed = true;
                            }
                        }

                        if (changed) {
                            for (String nonTerminal : keepAccount.keySet()) {
                                if (keepAccount.get(nonTerminal).contains(String.valueOf(nonterminal))) {
                                    Set<String> tmp = new HashSet<>(follow.get(String.valueOf(nonterminal)));
                                    follow.get(nonTerminal).addAll(tmp);
                                }
                            }
                        }

                    }
                }
            }
            if (!changed) {
                break;
            }
        }
    }
}