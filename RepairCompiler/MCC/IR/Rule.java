package MCC.IR;

import java.util.*;

public class Rule implements Quantifiers {
    
    static int count = 1;

    Vector quantifiers = new Vector();
    boolean isstatic = false;
    boolean isdelay = false;
    private Expr guard = null;
    Inclusion inclusion = null;    
    SymbolTable st = new SymbolTable();
    DNFRule dnfguard=null,dnfnegguard=null;

    String label;
    
    int num;

    public Rule () {
        num = count;
        label = new String("rule" + count++);
    }
    
    public String toString() {
	String name="";
	for(int i=0;i<numQuantifiers();i++) {
	    name+=getQuantifier(i).toString()+",";
	}
	name+=guard.name()+"=>"+inclusion.toString();
	return name;
    }

    public int numQuantifiers() {
	return quantifiers.size();
    }

    public Quantifier getQuantifier(int i) {
	return (Quantifier) quantifiers.get(i);
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
	dnfguard=guard.constructDNF();
	OpExpr opexpr=new OpExpr(Opcode.NOT,guard,null);
	dnfnegguard=opexpr.constructDNF();
    }
    
    public Expr getGuardExpr() {
        return guard;
    }

    public DNFRule getDNFGuardExpr() {
        return dnfguard;
    }

    public DNFRule getDNFNegGuardExpr() {
        return dnfnegguard;
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

    public Set getQuantifierDescriptors() {

        HashSet topdescriptors = new HashSet();

        for (int i = 0; i < quantifiers.size(); i++) {            
            Quantifier q = (Quantifier) quantifiers.elementAt(i);
            topdescriptors.addAll(q.getRequiredDescriptors());                
        }
        
        return SetDescriptor.expand(topdescriptors);
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
