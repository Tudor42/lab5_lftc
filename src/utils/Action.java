package utils;

public class Action {
    public enum ActionType{
        REDUCE,
        SHIFT,
        ACCEPT
    }
    public ActionType actionType;
    public String from;
    public Grammar.TokenSequence to;
    public int index;

    @Override
    public String toString() {
        return "Action{" +
                "actionType" + actionType +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", index=" + index +
                '}';
    }
}
