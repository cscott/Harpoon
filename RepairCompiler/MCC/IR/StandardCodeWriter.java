package MCC.IR;

import java.util.*;

public class StandardCodeWriter implements CodeWriter {

    boolean linestarted = false;
    PrintWrapper output;
    Stack symboltables = new Stack();
    InvariantValue ivalue;

    public StandardCodeWriter(PrintWrapper output) { this.output = output; }
    public StandardCodeWriter(java.io.PrintWriter output) { this.output = new PrintWrapper(output);}

    public void startBuffer() {
	output.startBuffer();
    }
    public void emptyBuffer() {
	output.emptyBuffer();
    }

    public void startblock() {
        indent();
        outputline("{");
    }

    public void endblock() {
        outputline("}");
        unindent();
    }
    public void addDeclaration(String type, String varname) {
	output.addDeclaration(type,varname);
    }
    public void addDeclaration(String f) {
	output.addDeclaration(f);
    }
    
    public void indent() {
        output.indent++;
    }

    public void unindent() {
        output.indent--;
    }

    private void doindent() {
        for (int i = 0; i < output.indent; i++) {
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

    public InvariantValue getInvariantValue() {
	return ivalue;
    }

    public void setInvariantValue(InvariantValue iv) {
	ivalue=iv;
    }
}
