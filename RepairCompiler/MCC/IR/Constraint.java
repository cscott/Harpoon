package MCC.IR;

import java.util.*;

public class Constraint {
    
    private static int count = 1;

    String label = null;
    boolean crash = false;
    SymbolTable st = new SymbolTable();
    Vector quantifiers = new Vector(); 
    LogicStatement logicstatement = null;

    int num;

    public Constraint() {
        num = count;
        label = new String("c" + count++);
    }

    public int getNum() {
        return num;
    }

    public String getLabel() {
        return label;
    }

    public SymbolTable getSymbolTable() {
        return st;
    }

    public void addQuantifier(Quantifier q) {
        quantifiers.addElement(q);
    }

    public void setLogicStatement(LogicStatement ls) {
        logicstatement = ls;
    }

    public LogicStatement getLogicStatement() {
        return logicstatement;
    }
    
    public void setCrash(boolean crash) {
        this.crash = crash;
    }

    public ListIterator quantifiers() {
        return quantifiers.listIterator();
    }

    public Set getRequiredDescriptorsFromQuantifiers() {

        HashSet topdescriptors = new HashSet();

        for (int i = 0; i < quantifiers.size(); i++) {            
            Quantifier q = (Quantifier) quantifiers.elementAt(i);
            topdescriptors.addAll(q.getRequiredDescriptors());                
        }

        return SetDescriptor.expand(topdescriptors);
    }

    public Set getRequiredDescriptorsFromLogicStatement() {
        
        HashSet topdescriptors = new HashSet();

        topdescriptors.addAll(logicstatement.getRequiredDescriptors());

        return SetDescriptor.expand(topdescriptors);
    }
   
    public Set getRequiredDescriptors() {
        Set set = getRequiredDescriptorsFromQuantifiers();
        set.addAll(getRequiredDescriptorsFromLogicStatement());
        return set;
    }
   
}

