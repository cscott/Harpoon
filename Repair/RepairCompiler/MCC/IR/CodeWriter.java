package MCC.IR;

public interface CodeWriter extends PrettyPrinter{

    public void outputline(String s);
    public void indent();
    public void unindent();
    public void startblock();
    public void endblock();

    public void startBuffer();
    public void emptyBuffer();
    public void addDeclaration(String type, String varname);
    public void addDeclaration(String function);
    public void pushSymbolTable(SymbolTable st);
    public SymbolTable popSymbolTable();
    public SymbolTable getSymbolTable();
    public InvariantValue getInvariantValue();
    public void setInvariantValue(InvariantValue iv);
}
