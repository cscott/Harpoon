package MCC.IR;

import java.util.*;

public class Rule {
    
    static int count = 1;

    Vector quantifiers = new Vector();
    boolean isstatic = false;
    boolean isdelay = false;
    Expr guard = null;
    Inclusion inclusion = null;    
    SymbolTable st = new SymbolTable();
    
    String label;
    
    int num;

    public Rule () {
        num = count;
        label = new String("rule" + count++);
    }
    
    public int getNum() {
        return num;
    }

    public String getLabel() {
        return label;
    }

    public void setStatic(boolean val) {
        isstatic = val;
    }

    public void setDelay(boolean val) {
        isdelay = val;
    }

    public void addQuantifier(Quantifier q) {
        quantifiers.addElement(q);
    }

    public ListIterator quantifiers() {
        return quantifiers.listIterator();
    }

    public void setGuardExpr(Expr guard) {
        this.guard = guard;
    }
    
    public Expr getGuardExpr() {
        return guard;
    }

    public void setInclusion(Inclusion inclusion) {
        this.inclusion = inclusion;
    }

    public Inclusion getInclusion() {
        return inclusion;
    }
    
    public SymbolTable getSymbolTable() {
        return st;
    }

    public Set getRequiredDescriptors() {

        HashSet topdescriptors = new HashSet();

        for (int i = 0; i < quantifiers.size(); i++) {            
            Quantifier q = (Quantifier) quantifiers.elementAt(i);
            topdescriptors.addAll(q.getRequiredDescriptors());                
        }

        assert guard != null;            
        topdescriptors.addAll(guard.getRequiredDescriptors());
        
        assert inclusion != null;
        topdescriptors.addAll(inclusion.getRequiredDescriptors());
        
        return SetDescriptor.expand(topdescriptors);
    }

}
