package MCC.IR;

import java.util.*;

public class SymbolTableStack {

    SymbolTable st;

    SymbolTableStack() {
	st = null;
    }

    SymbolTableStack(SymbolTable st) {
	this.st = st;
    }

    boolean empty() {
        return st == null;
    }

    SymbolTable peek() {
	return st;
    }

    SymbolTable pop() {
	if (st == null) {
	    throw new IRException("SymbolTableStack: tried to pop empty stack.");
        }

	SymbolTable lastst = st;
	st = st.getParent();
	return lastst;
    }

    // Link and push.
    void push(SymbolTable st) {
        if (st != null) {
            st.setParent(this.st);
        }

	this.st = st;
    }
}
