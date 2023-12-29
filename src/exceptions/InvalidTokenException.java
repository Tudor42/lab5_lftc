package exceptions;

public class InvalidTokenException extends Exception{
    String line;
    int lineNumber;
    int position;

    public InvalidTokenException(String line, int lineNumber, int position){
        this.line = line;
        this.position = position;
        this.lineNumber = lineNumber;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder("Error at line " + lineNumber + ": ");
        int l = message.length();
        message.append(line).append("\n");
        for(int i = 0; i < l + position; ++i){
            message.append(" ");
        }
        message.append("^");
        return message.toString();
    }
}
