package MCC.IR;

public interface CodeWriter extends PrettyPrinter{

    public void outputline(String s);
    public void indent();
    public void unindent();

    public SymbolTable getSymbolTable();
    
}
