package utils;

import java.util.Objects;

public class Item {
    public String nonTerminal;
    public Grammar.TokenSequence to;
    public int dotPosition = 0;

    public Item(String nonTerminal, Grammar.TokenSequence to){
        this.nonTerminal = nonTerminal;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return dotPosition == item.dotPosition && nonTerminal.equals(item.nonTerminal)
                && to.containsAll(item.to) && item.to.containsAll(to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonTerminal, to, dotPosition);
    }

    @Override
    public String toString() {
        return "Item{" +
                "nonTerminal='" + nonTerminal + '\'' +
                ", to='" + to + '\'' +
                ", dotPosition=" + dotPosition +
                '}';
    }
}
