import exceptions.InvalidTokenException;
import tokens.TokenType;
import utils.Item;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.*;

public class Main {
    static List<String> tokens = new ArrayList<>();
    private static final Map<String, Set<String>> follow = new HashMap<>();

    public static void main(String[] args) throws InvalidTokenException, IOException {
        Grammar.parse_file("grammar");
        Grammar.TokenSequence tokenSequence = new Grammar.TokenSequence();
        tokenSequence.addAll(Arrays.asList(Grammar.start, "$"));
        Grammar.transitions.put("augmented_grammar_start_var", List.of(tokenSequence));
        Grammar.terminals.add("$");
        calcFirst();
        calcFollow();

        List<State> states = states();
        List<Map<String, Action>> actionTable = new ArrayList<>();
        List<Map<String, Integer>> goTo = new ArrayList<>();
        for (State s : states) {
            Map<String, Action> newLine = new HashMap<>();
            Map<String, Integer> goToLine = new HashMap<>();
            for (Item item : s.items) {
                if (item.nonTerminal.equals("augmented_grammar_start_var") && item.getTo().size() == tokenSequence.size()
                        && item.getTo().get(0).equals(tokenSequence.get(0)) && item.getTo().get(1).equals(tokenSequence.get(1))) {
                    Action action = new Action();
                    action.to = item.getTo();
                    action.from = item.nonTerminal;
                    action.actionType = Action.ActionType.ACCEPT;
                    if (newLine.put("$", action) != null) {
                        throw new RuntimeException("Conflict");
                    }
                }
                if (item.dotPosition == item.getTo().size()) { // reduce
                    for (String followItem : follow.get(item.nonTerminal)) {
                        Action action = new Action();
                        action.to = item.getTo();
                        action.from = item.nonTerminal;
                        action.actionType = Action.ActionType.REDUCE;
                        if (newLine.put(followItem, action) != null) {
                            throw new RuntimeException("Reduce/reduce conflict for " + followItem);
                        }
                    }
                }
            }
            for (String use : s.transitions.keySet()) {
                if (Grammar.terminals.contains(use)) {
                    Action action = new Action();
                    action.index = states.indexOf(s.transitions.get(use));
                    action.actionType = Action.ActionType.SHIFT;
                    if (newLine.put(use, action) != null) {
                        throw new RuntimeException("Reduce/shift conflict for " + use);
                    }
                } else {
                    goToLine.put(use, states.indexOf(s.transitions.get(use)));
                }
            }
            actionTable.add(newLine);
            goTo.add(goToLine);
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("resources/code"))) {
            String line;
            int lineNr = 0;
            while ((line = bufferedReader.readLine()) != null) {
                ++lineNr;

                if (line.isBlank()) {
                    continue;
                }
                Matcher matcher = Pattern.compile("").matcher(line.trim());
                nextToken:
                while (!matcher.hitEnd()) {
                    for (TokenType t : TokenType.values()) {
                        String matched = t.match(matcher);
                        if (matched != null) {
                            tokens.add(t.toString());
                            continue nextToken;
                        }
                    }
                    throw new InvalidTokenException(line, lineNr, matcher.regionStart());
                }
            }
        } catch (InvalidTokenException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
        tokens.add("$");

        parse(actionTable, goTo);
    }

    private static void parse(List<Map<String, Action>> actionTable, List<Map<String, Integer>> goTo) {
        if (tokens.size() == 1) {
            System.out.println("Correct text.");
            return;
        }

        int currStateIndex = 0;
        Stack<String> stack = new Stack<>();
        stack.push("0");
        System.out.printf("%30s %50s %40s %n", "Stack", "InputBuffer", "Action");
        while (true) {
            String currentChar = tokens.get(0);
            Action currAction = actionTable.get(currStateIndex).get(currentChar);
            System.out.printf("%30s %50s ", String.join(" ", stack.subList(Math.max(stack.size() - 4, 0), stack.size() - 1)), String.join(" ", tokens.subList(0, Math.min(5, tokens.size()) - 1)));
            if (currAction == null) {
                System.out.printf("%40s %n", "Can't parse the text.");
                break;
            }

            if (currAction.actionType == Action.ActionType.SHIFT) {
                stack.push(currentChar);
                tokens = tokens.subList(1, tokens.size());
                currStateIndex = currAction.index;
                stack.push(String.valueOf(currStateIndex));
                System.out.printf("%40s %n", "Shift" + currStateIndex);
            }

            if (currAction.actionType == Action.ActionType.REDUCE) {
                int toRemove = currAction.to.size() * 2;
                while (toRemove != 0) {
                    stack.pop();
                    toRemove--;
                }

                int goToIndex = Integer.parseInt(String.valueOf(stack.peek()));
                stack.push(currAction.from);
                currStateIndex = goTo.get(goToIndex).get(currAction.from);
                stack.push(String.valueOf(currStateIndex));
                System.out.printf("%40s %n", "Reduce " + currAction.from + "->" + currAction.to);
            }

            if (currAction.actionType == Action.ActionType.ACCEPT) {
                System.out.printf("%40s %n", "Correct text.");
                break;
            }
        }
    }

    private static List<State> states() {
        List<State> states = new ArrayList<>();
        State initialState = new State();

        for (var key : Grammar.transitions.keySet()) {
            for (Grammar.TokenSequence s : Grammar.transitions.get(key)) {
                if (!s.isEmpty()) {
                    initialState.items.add(new Item(key, s));
                }
            }
        }

        states.add(initialState);

        for (int i = 0; i < states.size(); ++i) {
            Set<String> stringWithDot = new HashSet<>();
            for (Item item : states.get(i).items) {
                boolean startVarCondition = item.nonTerminal.equals("augmented_grammar_start_var") && item.dotPosition < item.getTo().size() - 1;
                boolean restOfVarsCondition = !item.nonTerminal.equals("augmented_grammar_start_var") && item.dotPosition < item.getTo().size();
                if (startVarCondition || restOfVarsCondition) {
                    stringWithDot.add(item.getTo().get(item.dotPosition));
                }
            }
            for (String s : stringWithDot) {
                HashSet<Item> nextStateItems = new HashSet<>();
                for (Item item : states.get(i).items) {
                    if (item.dotPosition < item.getTo().size() && s.equals(item.getTo().get(item.dotPosition))) {
                        Item newItem = new Item(item.nonTerminal, item.getTo());
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
                        if (item.dotPosition < item.getTo().size() && Grammar.nonterminals.contains(item.getTo().get(item.dotPosition))) {
                            Grammar.transitions.get(item.getTo().get(item.dotPosition)).forEach(rule -> {
                                if (!rule.isEmpty()) {
                                    temp.add(new Item(item.getTo().get(item.dotPosition), rule));
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

    private static Set<String> plus(Set<String> l1, Set<String> l2) {
        Set<String> res = new HashSet<>();
        for (String x : l1) {
            for (String y : l2) {
                if(x.equals("") && !y.equals(""))
                    res.add(y);
                if(!x.equals("") && y.equals(""))
                    res.add(x);
                if(x.equals("") && y.equals(""))
                    res.add("");
                if(!x.equals("") && !y.equals(""))
                    res.add(x);
            }
        }
        return res;
    }

    private static void calcFirst() {
        for(String terminal: Grammar.terminals){
            first.put(terminal, new HashSet<>(Set.of(terminal)));
        }

        for (String nonterminal : Grammar.nonterminals) {
            first.put(nonterminal, new HashSet<>());
            for (int i = 0; i < Grammar.transitions.get(nonterminal).size(); i++) {
                Grammar.TokenSequence transition = Grammar.transitions.get(nonterminal).get(i);
                if (Grammar.terminals.contains(transition.get(0)) || transition.get(0).equals("")) {
                    first.get(nonterminal).add(transition.get(0));
                }
            }
        }

        boolean changed;
        do {
            changed = false;
            for (String nonterminal : Grammar.nonterminals) {
                List<Set<String>> result = new ArrayList<>();
                for (int j = 0; j < Grammar.transitions.get(nonterminal).size(); j++) {
                    Grammar.TokenSequence transition = Grammar.transitions.get(nonterminal).get(j);
                    Set<String> currentRes;

                    if (transition.size() == 1) {
                        if (Grammar.nonterminals.contains(transition.get(0))) {
                            currentRes = first.get(transition.get(0));
                            result.add(new HashSet<>(currentRes));
                        } else {
                            currentRes = new HashSet<>();
                            currentRes.add(transition.get(0));
                            result.add(currentRes);
                        }
                        continue;
                    }

                    int n;
                    Set<String> l1, l2;
                    if (Grammar.nonterminals.contains(transition.get(0))) {
                        l1 = first.get(transition.get(0));
                    } else {
                        l1 = new HashSet<>();
                        l1.add(transition.get(0));
                    }

                    if (Grammar.nonterminals.contains(transition.get(1))) {
                        l2 = first.get(transition.get(1));
                    } else {
                        l2 = new HashSet<>();
                        l2.add(transition.get(1));
                    }

                    currentRes = plus(l1, l2);
                    n = 2;
                    while (n < transition.size()) {
                        if(Grammar.nonterminals.contains(transition.get(n))){
                            l2 = first.get(transition.get(n));
                        }else {
                            l2 = new HashSet<>();
                            l2.add(transition.get(n));
                        }
                        currentRes = plus(currentRes, l2);
                        n++;
                    }
                    result.add(new HashSet<>(currentRes));
                }

                Set<String> newFirst = new HashSet<>();
                for(Set<String> set: result){
                    newFirst.addAll(set);
                }

                if(!first.get(nonterminal).equals(newFirst)){
                    first.put(nonterminal, newFirst);
                    changed = true;
                }
            }
        } while (changed);
    }

    private static void calcFollow() {
        Map<String, Set<String>> keepAccount = new HashMap<>();
        for (String c : Grammar.nonterminals) {
            follow.put(c, new HashSet<>());
            keepAccount.put(c, new HashSet<>());
        }
        follow.get(Grammar.start).add("$");

        while (true) {
            boolean changed = false;
            for (String nonterminal : Grammar.nonterminals) {
                for (var rule : Grammar.transitions.entrySet()) {
                    for (Grammar.TokenSequence rh : rule.getValue()) {
                        if (!rh.contains(nonterminal)) {
                            continue;
                        }
                        List<Integer> indexes = new ArrayList<>();
                        for (int i = 0; i < rh.size(); ++i) {
                            if (rh.get(i).equals(nonterminal))
                                indexes.add(i);
                        }
                        for (Integer index : indexes) {
                            if (index == rh.size() - 1) {
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                changed = follow.get(nonterminal).addAll(tmp);
                                keepAccount.get(nonterminal).add(rule.getKey());
                                continue;
                            }
                            String symbol = rh.get(index + 1);
                            if (Grammar.terminals.contains(symbol)) {
                                changed = follow.get(nonterminal).add(symbol);
                            }
                            Set<String> frst = first.get(symbol);
                            if (frst.contains("")) {
                                Set<String> tmp = new HashSet<>(follow.get(rule.getKey()));
                                changed = follow.get(nonterminal).addAll(tmp);
                                frst.remove("");
                            }
                            frst.removeAll(follow.get(nonterminal));
                            if (!frst.isEmpty()) {
                                follow.get(nonterminal).addAll(frst);
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