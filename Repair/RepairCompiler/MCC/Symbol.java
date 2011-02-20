package MCC;

/**
 * Our Symbol class that adds line number functionality.
 */
public class Symbol extends java_cup.runtime.Symbol {
    int line;

    /**
     * The simple constructor - just calls super and sets the line number
     */
    public Symbol(int type, int left, int right, Object value, int line) {
        super(type, left, right, value);
        this.line = line;
    }
}
