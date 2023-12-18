package utils;

public class Action {
    public enum ActionType{
        REDUCE,
        SHIFT,
        ACCEPT
    }
    public ActionType actionType;
    public String from;
    public String to;
    public int index;
}
