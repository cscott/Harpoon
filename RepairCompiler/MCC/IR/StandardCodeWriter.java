package MCC.IR;

import java.util.*;

public class StandardCodeWriter implements CodeWriter { 

    boolean linestarted = false;
    int indent = 0;
    java.io.PrintWriter output;
    Stack symboltables = new Stack();

    public StandardCodeWriter(java.io.PrintWriter output) { this.output = output; }

    public void startblock() {
        indent();
        outputline("{");
    }

    public void endblock() {
        outputline("}");
        unindent();
    }
    
    public void indent() { 
        indent++; 
    }
    
    public void unindent() { 
        indent--; 
        assert indent >= 0; 
    }
    
    private void doindent() {
        for (int i = 0; i < indent; i++) { 
            output.print("  ");
        }
        linestarted = true;
    }
    
    public void outputline(String s) {
        if (!linestarted) {
            doindent();
        }
        output.println(s);
        linestarted = false;
        output.flush();
    }                 
    
    public void output(String s) {
        if (!linestarted) {
            doindent();
        }
        output.print(s);
        output.flush(); 
    }

    public void pushSymbolTable(SymbolTable st) {
        symboltables.push(st);
    }

    public SymbolTable popSymbolTable() {
        return (SymbolTable) symboltables.pop();
    }

    public SymbolTable getSymbolTable() { 
        if (symboltables.empty()) {
            throw new IRException(); 
        }
        return (SymbolTable) symboltables.peek();
    }
    
}
