package MCC.IR;

import java.util.*;

public class Constraint implements Quantifiers {

    private static int count = 1;

    String label = null;
    boolean crash = false;
    SymbolTable st = new SymbolTable();
    Vector quantifiers = new Vector();
    LogicStatement logicstatement = null;
    DNFConstraint dnfconstraint;
    int num;

    public Constraint() {
        num = count;
        label = new String("c" + count++);
    }

    public DNFConstraint getDNFConstraint() {
        return dnfconstraint;
    }

    public String toString() {
	String name="";
	for(int i=0;i<numQuantifiers();i++) {
	    name+=getQuantifier(i).toString()+",";
	}
	name+=logicstatement.name();
	return name;
    }

    public int getNum() {
        return num;
    }

    public int numQuantifiers() {
	return quantifiers.size();
    }

    public Quantifier getQuantifier(int i) {
	return (Quantifier) quantifiers.get(i);
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
	// Construct DNF form for analysis
	dnfconstraint=logicstatement.constructDNF();
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
