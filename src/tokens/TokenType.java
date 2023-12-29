package tokens;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TokenType {
    ADD("\\+"),
    MINUS("-"),
    MULTIPLY("\\*"),
    DIVIDE("/"),
    MODULUS("%"),
    BOOL_AND("&&"),
    BOOL_OR("\\|\\|"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    LESS("<"),
    GREATER(">"),
    SEMI_COLON(";"),
    COMMA(","),
    ASSIGN("="),
    LBRACKET("\\{"),
    RBRACKET("\\}"),
    LPARENTHESIS("\\("),
    RPARENTHESIS("\\)"),
    INT("int", true),
    UINT("uint", true),
    FLOAT("float", true),
    STRUCT("struct", true),
    PRINT("print", true),
    SCAN("scan", true),
    WHILE("while", true),
    IF("if", true),
    VAR_NAME("[a-z][a-z0-9]{0,7}", true),
    NUMBER("([1-9][0-9]*\\.?[0-9]*|0x[0-9a-fA-F]+\\.?[0-9a-fA-F]*|0[0-7]+\\.?[0-7]*|0b[01]+\\.?[01]*|0)", true);
    private String regex = "";


    boolean isWord;
    public String match(Matcher matcher){
        matcher.usePattern(Pattern.compile((isWord?"^\\b":"^") + regex + (isWord?"\\b *":" *")));
        if(matcher.find()){
            String res = matcher.group();
            matcher.region(matcher.regionStart() + res.length(), matcher.regionEnd());
            return res;
        }

        return null;
    }

    TokenType(String regex){
        this.regex = regex;
    }

    TokenType(String regex, boolean isWord){
        this.isWord = isWord;
        this.regex = regex;
    }


}
