package utils;

import java.util.ArrayList;
import java.util.Objects;

public class Item {
    public String nonTerminal;
    private Grammar.TokenSequence to;
    public int dotPosition = 0;

    public Item(String nonTerminal, Grammar.TokenSequence to){
        this.nonTerminal = nonTerminal;
        this.setTo(to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return dotPosition == item.dotPosition && Objects.equals(nonTerminal, item.nonTerminal) && Objects.equals(to, item.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonTerminal, getTo(), dotPosition);
    }

    @Override
    public String toString() {
        return "Item{" +
                "nonTerminal='" + nonTerminal + '\'' +
                ", to='" + getTo() + '\'' +
                ", dotPosition=" + dotPosition +
                '}';
    }

    public Grammar.TokenSequence getTo() {
        return new Grammar.TokenSequence(to);
    }

    private void setTo(Grammar.TokenSequence to) {
        this.to = to;
    }
}
