package harpoon.Analysis.Tree;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

public class TreeFolding {
    private static final int NO_GEN = -1;
    private static final int NO_EXP = -1;

    private static BitSet ALL_ZEROS; // 0000000...
    private static BitSet ALL_ONES;  // 1111111...
    private static BitSet DATA_MASK; // 1010101...
    private static BitSet MEM_MASK;  // 0101010...

    /** Returns an HCodeFactory which produces folded trees */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod hm) {
		if (parent.getCodeName().equals("canonical-tree")) { 
		    CanonicalTreeCode hc = 
			(CanonicalTreeCode)parent.convert(hm);
		    if (hc!=null) {
			Frame frame = ((Stm)hc.getRootElement()).
			    getFactory().getFrame();
			hc = (CanonicalTreeCode)hc.clone(hm, frame);
			new TreeFolding().toFoldedTree(hc);
		    }
		    return hc;
		}
		else { 
		    throw new Error("This code isn't in canonical-tree form!");
		}
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    // Folds "code".  Note that it modifies this parameter directly
    //
    private void toFoldedTree(harpoon.IR.Tree.CanonicalTreeCode code) {
	GenVisitor gv;    KillVisitor kv;  
	InOutVisitor iov; DefUseVisitor duv;

	int numDefs, numExprs;
	long time;
	Hashtable availableExprs = new Hashtable();
	Hashtable inSets   = new Hashtable();
	Hashtable outSets  = new Hashtable();
	Hashtable inExprs  = new Hashtable();
	Hashtable outExprs = new Hashtable();
	Hashtable kgSets   = new Hashtable();
	Hashtable UDchains = new Hashtable();
	TreeCFG cfg;

	// Calculate GEN sets
	gv = new GenVisitor(kgSets, availableExprs);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) 
	    ((Tree)e.nextElement()).visit(gv);
	numDefs = gv.numDefs(); numExprs = gv.numExprs();
	gv = null;

	// Forced by the limitations of the BitSet class
	computeUtilityBitsets(numDefs);

	// Calculate KILL sets
	kv = new KillVisitor(kgSets, availableExprs, numDefs, numExprs);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) 
	    ((Tree)e.nextElement()).visit(kv);
	kv.finish();  // finished delayed calculation
	kv = null;

 	// Calculate IN and OUT set
	iov = new InOutVisitor(code, inSets, outSets, inExprs, 
			       outExprs, kgSets, availableExprs,
			       numDefs, numExprs);
	cfg = iov.getCFG();
	for (iov.changed = true; iov.changed; ) {
	    iov.changed = false; 
	    // depth-first yields a soln fastest
	    for (Enumeration e = iov.cfg.depthFirstEnumeration(); 
		 e.hasMoreElements();)
		((Tree)e.nextElement()).visit(iov);
	}
	iov = null; outSets = null; outExprs = null;

	// Calculate DU and UD chains	
	duv = new DefUseVisitor(kgSets, inSets, UDchains, numDefs);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) { 
	    Tree tree = (Tree)e.nextElement();
	    tree.visit(duv);
	}
	duv = null; inSets = null;

	// Perform the tree folding
	TreeFolder tf = new TreeFolder(code, kgSets, availableExprs, 
				       inExprs, UDchains);
	for (Enumeration e = cfg.depthFirstEnumeration(); 
	     e.hasMoreElements();) 
	    ((Tree)e.nextElement()).visit(tf);
    }

    // Required data:  none
    // Exported data:  availableExprs  (Stm-->AvailableExp, Integer-->Stm)
    //                 kgSets          (Stm-->KillGenSet, Integer-->Stm)
    //                 numDefs         (number of Temp defns)
    //                 numExprs        (number of subexpressions)
    class GenVisitor extends TreeVisitor {
	private int numDefs  = 0;
	private int numExprs = 0;
	private Hashtable kgSets;
	private Hashtable availableExprs;
	
	public GenVisitor(Hashtable kgSets, Hashtable availableExprs) {
	    this.kgSets = kgSets;
	    this.availableExprs = availableExprs;
	}
	    
	public void visit(Tree t) { 
	    throw new Error("No defaults allowed");
	}

	public void visit(Exp e) { /* Do nothing for Exps */ }

	public void visit(Stm s) { 
	    kgSets.put(s, new KillGenSet(null, NO_GEN, null));
	    availableExprs.put(s, new AvailableExp
			       (null, null, null, NO_EXP));
	}

	// Only the MOVE instruction generates Temps
	public void visit(MOVE s) { 
	    if (s.dst instanceof MEM) {
		visit((Stm)s); // default behavior (doesn't GEN)
	    }
	    else if (s.dst instanceof TEMP) {
	        kgSets.put
		    (s, new KillGenSet(((TEMP)s.dst).temp, numDefs, null));
		kgSets.put(new Integer(numDefs++), s);

		availableExprs.put(s, new AvailableExp
				   (((TEMP)s.dst).temp, s.use(), 
				    null, numExprs));
		availableExprs.put(new Integer(numExprs++), s);
	    }
	    else 
		throw new Error("Unexpected destination type!");
	}

	int numDefs() { return numDefs; }
	int numExprs() { return numExprs; } 
    }

    // Required data:  availableExprs  (Stm-->AvailableExp, Integer-->Stm)
    //                 kgSets          (Stm-->KillGenSet, Integer-->Stm)
    //                 numDefs         (number of Temp defns)
    //                 numExprs        (number of subexpressions)
    // Exported data:  availableExprs  (Stm-->AvailableExp, Integer-->Stm)
    //                 kgSets          (Stm-->KillGenSet, Integer-->Stm)
    //
    // Computes the KILL sets for all subexpressions and Temps
    //
    class KillVisitor extends TreeVisitor { 
	private Hashtable tmp    = new Hashtable();
	private Hashtable tmpExp = new Hashtable();
	private Hashtable kgSets;
	private Hashtable availableExprs;
	private int numDefs;
	private int numExprs;
	
	public KillVisitor(Hashtable kgSets, Hashtable availableExprs,
			   int numDefs, int numExprs) { 
	    this.kgSets         = kgSets;
	    this.availableExprs = availableExprs;
	    this.numDefs        = numDefs;
	    this.numExprs       = numExprs;
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults allowed");
	}

	public void visit(Exp e) { /* No nothing for Exps */ }
	
	public void visit(Stm s) { 
	    KillGenSet kgs = (KillGenSet)kgSets.get(s);
	    kgs.kill = new BitSet(numDefs<<1);
	    AvailableExp aes = (AvailableExp)availableExprs.get(s);
	    aes.kill = new BitSet(numDefs);
	}

	// Kill all defs, GEN nothing
	public void visit(INVOCATION s) {
	    KillGenSet kgs = (KillGenSet)kgSets.get(s);
	    kgs.kill = new BitSet(numDefs<<1);
	    AvailableExp aes = (AvailableExp)availableExprs.get(s);
	    aes.kill = new BitSet(numDefs);

	    if ((s.retval instanceof MEM) || (s.retex instanceof MEM)) {
		kgs.kill.or(ALL_ONES);  
		aes.kill.or(ALL_ONES);  
	    }
	    else {
		Util.assert((s.retval instanceof TEMP) && 
			    (s.retex instanceof TEMP));
		
		killAll(kgs.kill, ((TEMP)s.retval).temp);
		killAll(kgs.kill, ((TEMP)s.retex).temp);
		killAllExprs(aes.kill, s.use());
		
	    }
	}
	
	public void visit(MOVE s) { 
	    KillGenSet kgs = (KillGenSet)kgSets.get(s);
	    kgs.kill = new BitSet(numDefs<<1);
	    AvailableExp aes = (AvailableExp)availableExprs.get(s);
	    aes.kill = new BitSet(numDefs);

	    if (s.dst instanceof MEM) {
		kgs.kill.or(ALL_ONES); 
		aes.kill.or(ALL_ONES); 
	    }
	    else {
		Util.assert(s.dst instanceof TEMP);
		
		killAll(kgs.kill, kgs.temp);
		kgs.kill.clear(kgs.gen<<1);
		killAllExprs(aes.kill, aes.temps);
		aes.kill.clear(aes.expNum);
	    }
	}

		
	// kills all instances of "t" in this BitSet
	// This means, kills any bit where t is defined
	// computation is delayed until computeKGSets()
	private void killAll(BitSet killSet, Temp t) { 
	    Util.assert(t!=null);

	    if (tmp.containsKey(t)) 
		((Set)tmp.get(t)).union(killSet);
	    else 
		tmp.put(t, new HashSet());
	}

	// kills all instances of "t" in this BitSet.  
	// This means, kills any bit which uses "t"
	// 
	private void killAllExprs(BitSet killSet, Temp[] t) { 
	    for (int i=0; i<t.length; i++) { 
		if (tmpExp.containsKey(t[i])) 
		    ((Set)tmpExp.get(t[i])).union(killSet);
		else 
		    tmpExp.put(t[i], new HashSet());
	    }
	}

	// Must call finish() before using any data from kgSets or
	// availableExprs, because it finishes delayed computation
	//	
	void finish() { 
	    computeKGSets();
	    computeAvailableExprs();
	}

	private void computeKGSets() { 
	    for (Enumeration e = kgSets.keys(); e.hasMoreElements();) { 
		Object next = e.nextElement();
		if (next instanceof Stm) {
		    KillGenSet kgs = (KillGenSet)kgSets.get(next);
		    Temp temp = kgs.temp;
		    if (temp!=null) { 
		      Set set = (Set)tmp.get(temp);
		      for (Enumeration f = set.elements(); 
			   f.hasMoreElements();) { 
			BitSet b = (BitSet)f.nextElement();
			b.set(kgs.gen<<1);
		      }
		    }
		}
	    }
	    for (Enumeration e = kgSets.keys(); e.hasMoreElements();) { 
		Object next = e.nextElement();
		if (next instanceof Integer) { 
		    Integer i = (Integer)next;
		    KillGenSet kgs = (KillGenSet)kgSets.get(kgSets.get(i));
		    kgs.kill.clear(i.intValue()<<1);
		}
	    }
	}

	private void computeAvailableExprs() { 
	    for (Enumeration e = availableExprs.keys(); 
		 e.hasMoreElements();) {
		Object next = e.nextElement();
		if (next instanceof Stm) { 
		    AvailableExp ae = (AvailableExp)availableExprs.get(next);
		    Temp temp = ae.def;
		    if (temp!=null) { 
			if (tmpExp.containsKey(temp)) { 
			    Set set = (Set)tmpExp.get(temp);
			    for (Enumeration f = set.elements(); 
				 f.hasMoreElements();) { 
				BitSet b = (BitSet)f.nextElement();
				b.set(ae.expNum);
			    }
			}
		    }
		}			
	    }
	    for (Enumeration e = availableExprs.keys(); 
		 e.hasMoreElements();) { 
		Object next = e.nextElement();
		if (next instanceof Integer) { 
		    Integer i = (Integer)next;
		    AvailableExp ae = 
			(AvailableExp)availableExprs.get(availableExprs.get(i));
		    ae.kill.clear(i.intValue());
		}
	    }
	}
    }  

    // Calculates IN and OUT sets
    class InOutVisitor extends TreeVisitor {
	// Used to speed performance
	boolean                   changed;
	private CanonicalTreeCode code;
	private Hashtable         availableExprs;
	private Hashtable         inSets;
	private Hashtable         outSets;
	private Hashtable         inExprs;
	private Hashtable         outExprs;
	private Hashtable         kgSets;
	private TreeCFG           cfg;
	private int               numDefs;
	private int               numExprs;
	

	public InOutVisitor(CanonicalTreeCode code, Hashtable inSets, 
			    Hashtable outSets, Hashtable inExprs,
			    Hashtable outExprs, Hashtable kgSets, 
			    Hashtable availableExprs, int numDefs,
			    int numExprs) {
	    this.availableExprs = availableExprs;
	    this.code     = code;
	    this.inSets   = inSets;
	    this.outSets  = outSets;
	    this.inExprs  = inExprs;
	    this.outExprs = outExprs;
	    this.kgSets   = kgSets;
	    this.numDefs  = numDefs;
	    this.numExprs = numExprs;
	    this.cfg      = new TreeCFG(code);

	    // Initialize IN and OUT sets
	    //
	    for (Enumeration e = cfg.depthFirstEnumeration(); 
		 e.hasMoreElements();) { 
		Object next = e.nextElement();
		inSets.put(next, new BitSet(numDefs<<1));
		outSets.put(next, new BitSet(numDefs<<1));
		inExprs.put(next, new BitSet(numExprs));
		outExprs.put(next, new BitSet(numExprs));
	    }
	    
	}

	public void visit(Stm s) { 
	    // Calculate new out set
	    AvailableExp aes = (AvailableExp)availableExprs.get(s);
	    KillGenSet   kgs = (KillGenSet)kgSets.get(s);
	    BitSet in, out, inExp, outExp;

	    out = new BitSet(numDefs<<1);
	    out.or(kgs.kill);
	    out.and(DATA_MASK);
	    out.xor(ALL_ONES);
	    out.and((BitSet)inSets.get(s));
	    out.or(memMask(kgs.kill));

	    outExp = new BitSet(numExprs);
	    outExp.or(aes.kill);
	    outExp.xor(ALL_ONES);
	    outExp.and((BitSet)inExprs.get(s));

	    if (kgs.gen!=NO_GEN) { 
		out.set(kgs.gen<<1);
		out.clear((kgs.gen<<1)+1);
	    }
	    if (aes.expNum!=NO_EXP) { 
		outExp.set(aes.expNum);
	    }

	    changed = changed ||
		(!(outExp.equals(outExprs.get(s)))) ||
		(!(out.equals(outSets.get(s))));

	    outSets.put(s, out);
	    outExprs.put(s, outExp);

	    // Calculate new in sets
	    Stm[] successors = cfg.getSuccessors(s);
	    for (int i=0; i<successors.length; i++) { 
		Stm successor = successors[i];
		BitSet sInSet, sInExp, newInSet, newInExp;

		if (!changed) { 
		    sInSet = (BitSet)inSets.get(successor);
		    newInSet = (BitSet)sInSet.clone();
		    newInSet.or(out);

		    sInExp = (BitSet)inExprs.get(successor);
		    newInExp = (BitSet)sInExp.clone();
		    newInExp.or(outExp);

		    changed = (!newInSet.equals(sInSet)) || 
			(!newInExp.equals(sInExp));		

		    inSets.put(successor, newInSet);
		    inExprs.put(successor, newInExp);
		}
		else {
		    ((BitSet)inSets.get(successor)).or(out);
		    ((BitSet)inExprs.get(successor)).or(outExp);
		}
	    }
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults allowed");
	}

	public void visit(Exp e) { /* Do nothing for Exps */ }

        private BitSet memMask(BitSet b) { 
	    BitSet c = (BitSet)b.clone();
	    b.and(MEM_MASK);
	    return c;
	}

	TreeCFG getCFG() { return this.cfg; } 
    }

    // Computes DU and UD chains
    class DefUseVisitor extends TreeVisitor { 
	private Hashtable kgSets;
	private Hashtable inSets;
	private Hashtable UDchains;
	private int       numDefs;

	DefUseVisitor(Hashtable kgSets, Hashtable inSets, 
		      Hashtable UDchains, int numDefs) {
	    this.kgSets   = kgSets;
	    this.inSets   = inSets;
	    this.UDchains = UDchains;
	    this.numDefs  = numDefs;
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults allowed.");
	}

	public void visit(Exp e) { /* Do nothing for Exps */ }

	public void visit(LABEL s) { /* Do nothing for LABELs */ }
	public void visit(SEQ s) { /* Do nothing for SEQs */ }

	public void visit(Stm s) { 
	    // Dead code contains has no inset
	    if (!inSets.containsKey(s)) return;

	    BitSet inSet = (BitSet)inSets.get(s);
	    Temp[] uses  = s.use();

	    for (int i=0; i<inSet.size(); i+=2) { 
		if (inSet.get(i) && (!inSet.get(i+1))) { 
		    KillGenSet kgs = 
			(KillGenSet)kgSets.get(kgSets.get(new Integer(i>>1)));
		    for (int j=0; j<uses.length; j++) { 
			if (kgs.temp==uses[j]) { 
			    map(s, uses[j], i>>1);
			}
		    }
		}
	    }
	}

	// Adds def to the UD chain indexed by {s,t}, and 
	// adds {s,t} to the DU chain indexed by def.
	//
	private void map(Stm s, Temp t, int def) { 
	    Tuple use = new Tuple(new Object[] { s, t });
	    BitSet defs; Vector uses;

	    // Add a def to the UD chain
	    if (!UDchains.containsKey(use)) { 
		defs = new BitSet(numDefs>>1); // dont need MEM bits
		UDchains.put(use, defs);
	    }
	    else { 
		defs = (BitSet)UDchains.get(use);
	    }
	    defs.set(def); 

	    // Add a use to the DU chain
	    if (!UDchains.containsKey(new Integer(def))) {
		uses = new Vector();
		UDchains.put(new Integer(def), uses);
	    } 
	    else { 
		uses = (Vector)UDchains.get(new Integer(def));
	    }
	    uses.addElement(use);
	}
    }

    class TreeFolder extends TreeVisitor { 
	private CanonicalTreeCode code;
	private Hashtable availableExprs;
	private Hashtable kgSets;
	private Hashtable inExprs;
	private Hashtable UDchains;
	private Hashtable fm = new Hashtable();
	private TreeStructure ts;

	TreeFolder(CanonicalTreeCode code, Hashtable kgSets, 
		   Hashtable availableExprs, Hashtable inExprs, 
		   Hashtable UDchains) {
	    this.code = code;
	    this.availableExprs = availableExprs;
	    this.inExprs = inExprs;
	    this.kgSets = kgSets;
	    this.UDchains = UDchains;
	    this.ts = new TreeStructure(code);
	}	    
	
	public void visit(Tree t) { 
	    throw new Error("No defaults here");
	}

	public void visit(Exp e)   { /* Do nothing for Exps */ } 

	public void visit(LABEL s) { /* Do nothing for LABELs */ } 
	public void visit(SEQ s)   { /* Do nothing for SEQs */ } 

	public void visit(Stm s) { 
	    Temp[] uses = s.use();
	    for (int i=0; i<uses.length; i++) { 
		Tuple tuple = new Tuple(new Object[] { s, uses[i] } );
		if (UDchains.containsKey(tuple)) { 
		    BitSet defs = (BitSet)UDchains.get(tuple);
		    Integer def = new Integer(getDef(defs));
		    if (def.intValue()!=NO_GEN) { 
			if (UDchains.containsKey(def)) {
			    Vector vUses = (Vector)UDchains.get(def);
			    if (vUses.size()==1) { 
				if (available(def, s)) 
				    fold((Stm)kgSets.get(def), s, uses[i]);
			    }
			}
		    }
		}
	    }
	}
	
	private boolean available(Integer def, Stm use) { 
	    AvailableExp aes = 
		(AvailableExp)availableExprs.get(availableExprs.get(def));
	    BitSet inExp = (BitSet)inExprs.get(use);
	    return inExp.get(aes.expNum);
	}

	private void fold(Stm def, Stm use, Temp tUse) { 
	    Stm foldedUse;
	    
	    while (fm.containsKey(use)) 
		use = (Stm)fm.get(use);
	    while (fm.containsKey(def)) 
		def = (Stm)fm.get(def);

	    // Fold the source of "def" into "use"
	    foldedUse = use.build(replace(((MOVE)def).src, use.kids(), tUse));

	    // Ensure that all attempts to fold
	    fm.put(def, foldedUse); 
	    fm.put(use, foldedUse); 

	    // Update the tree structure
	    ts.remove(def);
	    ts.replace(use, foldedUse);
	}

	private ExpList replace(Exp src, ExpList use, Temp tUse) { 
	    Vector exprs = new Vector();
	    for (ExpList expList = use; expList!=null; expList=expList.tail) { 
		if (expList.head instanceof TEMP) { 
		    TEMP temp = (TEMP)expList.head;
		    if (temp.temp==tUse) 
			exprs.addElement(src);
		    else 
			exprs.addElement(temp);
		}
		else {
		    exprs.addElement
			(expList.head.build
			 (replace(src, expList.head.kids(), tUse)));
		}
	    }
	    
	    if (exprs.size()<=0) { 
		return use;
	    }
	    else { 
		ExpList newUse = 
		    new ExpList((Exp)exprs.elementAt(exprs.size()-1), null);
		
		for (int i=exprs.size()-2; i>=0; i--) { 
		    newUse = new ExpList((Exp)exprs.elementAt(i), newUse);
		}
		return newUse;
	    }
	}

	private int getDef(BitSet defs) {
	    int def = NO_GEN;
	    for (int i=0; i<defs.size(); i++) {
		if (defs.get(i)) { 
		    if (def==NO_GEN) 
			def = i;
		    else return NO_GEN;
		}
	    }
	    return def;
	}
    }

    private void computeUtilityBitsets(int numDefs) { 
	ALL_ONES = new BitSet(numDefs<<1);
	ALL_ZEROS = new BitSet(numDefs<<1);
	for (int i=0; i<numDefs<<1; i++) { 
	    ALL_ONES.set(i);
	    ALL_ZEROS.clear(i);
	}		
	DATA_MASK = new BitSet(numDefs<<1);
	for (int i=0; i<numDefs<<1; i+=2) { 
	    DATA_MASK.set(i);
	}
	MEM_MASK = (BitSet)DATA_MASK.clone();
	MEM_MASK.xor(ALL_ONES);
    }

}

class KillGenSet {
    Temp temp; int gen; BitSet kill;
    KillGenSet(Temp temp, int gen, BitSet kill) {
	this.temp = temp; this.gen = gen; this.kill = kill;
    }
    public String toString() { 
	return "< " + temp + ", " + gen + ", " + kill + " >"; 
    }
}

class AvailableExp { 
    Temp def; Temp[] temps; BitSet kill; int expNum;
    AvailableExp(Temp def, Temp[] temps, BitSet kill, int expNum) { 
	this.def    = def;
	this.temps  = temps;
	this.kill   = kill;
	this.expNum = expNum;
    }
    public String toString() { 
	return "< " + def + ", " + temps + ", " + kill + ", " + expNum; 
    } 
}
