package MCC.IR;

public interface CodeWriter extends PrettyPrinter{

    public void outputline(String s);
    public void indent();
    public void unindent();
    public void startblock();
    public void endblock();

    public void pushSymbolTable(SymbolTable st);
    public SymbolTable popSymbolTable();
    public SymbolTable getSymbolTable();
    
}
