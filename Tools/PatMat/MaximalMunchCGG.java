// MaximalMunchCGG.java, created Thu Jun 24 18:07:16 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import harpoon.Util.Util;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.PreciselyTyped;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/**
 * <code>MaximalMunchCGG</code> finds an optimal tiling of
 * instructions on a Tree-IR.
 *
 * This Code Generator Generator produces Code Generators that use the
 * Maximal Munch algorithm to find an optimal tiling for an input
 * tree.  See Appel "Modern Compiler Implementation in Java", Section
 * 9.1 for a description of Maximal Munch.
 * 
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: MaximalMunchCGG.java,v 1.3.2.1 2002-02-27 08:37:37 cananian Exp $ */
public class MaximalMunchCGG extends CodeGeneratorGenerator {


    private static final String TREE_ALIGN = "harpoon.IR.Tree.ALIGN";
    private static final String TREE_BINOP = "harpoon.IR.Tree.BINOP";
    private static final String TREE_CALL = "harpoon.IR.Tree.CALL";
    private static final String TREE_CJUMP = "harpoon.IR.Tree.CJUMP";
    private static final String TREE_CONST = "harpoon.IR.Tree.CONST";
    private static final String TREE_DATUM = "harpoon.IR.Tree.DATUM";
    private static final String TREE_EXPR = "harpoon.IR.Tree.EXPR";
    private static final String TREE_JUMP = "harpoon.IR.Tree.JUMP";
    private static final String TREE_LABEL = "harpoon.IR.Tree.LABEL";
    private static final String TREE_MEM = "harpoon.IR.Tree.MEM";
    private static final String TREE_METHOD = "harpoon.IR.Tree.METHOD";
    private static final String TREE_MOVE = "harpoon.IR.Tree.MOVE";
    private static final String TREE_NAME = "harpoon.IR.Tree.NAME";
    private static final String TREE_NATIVECALL = "harpoon.IR.Tree.NATIVECALL";
    private static final String TREE_OPER = "harpoon.IR.Tree.OPER";
    private static final String TREE_RETURN = "harpoon.IR.Tree.RETURN";
    private static final String TREE_SEQ = "harpoon.IR.Tree.SEQ";
    private static final String TREE_SEGMENT = "harpoon.IR.Tree.SEGMENT";
    private static final String TREE_TEMP = "harpoon.IR.Tree.TEMP";
    private static final String TREE_THROW = "harpoon.IR.Tree.THROW";
    private static final String TREE_UNOP = "harpoon.IR.Tree.UNOP";

    private static final String TREE_BOP = "harpoon.IR.Tree.Bop";
    private static final String TREE_UOP = "harpoon.IR.Tree.Uop";

    private static final String TREE_Tree = "harpoon.IR.Tree.Tree";
    private static final String TREE_ExpList = "harpoon.IR.Tree.ExpList";
    private static final String TREE_Exp = "harpoon.IR.Tree.Exp";
    private static final String TREE_Stm = "harpoon.IR.Tree.Stm";
    private static final String TREE_TreeVisitor = "harpoon.IR.Tree.TreeVisitor";
    private static final String TREE_Type = "harpoon.IR.Tree.Type";

    private static final String TEMP_Label = "harpoon.Temp.Label";
    private static final String TEMP_Temp = "harpoon.Temp.Temp";
    private static final String TEMP_TempList = "harpoon.Temp.TempList";

    /** Creates a <code>MaximalMunchCGG</code>. 
	<BR> <B>requires:</B> <OL>
	     <LI> <code>s</code> follows the standard template for
	          defining a machine specification.  
	     <LI> <code>className</code> is a legal Java identifier
	          string for a class.
	     <LI> For each node-type in the <code>Tree</code> IR,
	          there exists a single-node tile pattern (TODO: I
		  took this 'requirement' straight from Appel, but it
		  can't REALLY be that strict.  Find a tighter
		  requirement that we can actually satisfy)
	     <LI> if <code>s</code> contains Java statements that rely
	          on knowledge about the class to be produced (such as
		  a Constructor implementation) then the class named
		  must match the <code>className</code> parameter.
	     </OL>
	<BR> <B>effects:</B> Creates a new
             <code>MaximalMunchCGG</code> and associates the
	     machine specification <code>s</code> with the newly
	     constructed <code>MaximalMunchCGG</code>.
	@see <A HREF="doc-files/instr-selection-tool.html">Standard Specification Template</A>
    */
    public MaximalMunchCGG(Spec s, String className) {
        super(s, className);
    }

    protected String producedClassType() {
	return "harpoon.Backend.Generic.MaxMunchCG";
    }

    /** Abstract interface for 'appending visitors' */
    static interface AppendingVisitor {
	/** Append 's' to 'buf' with the proper indentation. */
	public void append(StringBuffer buf, String s);
    }

    /** Sets up a series of checks to ensure all of the values in the
	visited statement are of the appropriate type.
    */
    static class TypeStmRecurse extends Spec.StmVisitor
	implements AppendingVisitor {
	/** constantly updated boolean expression to match a tree's
	    types. */
	StringBuffer exp;
	
	/** constantly updated set of statements to initialize the
            identifiers that the action statements will reference. */
	StringBuffer initStms;
	/** constantly updated set of statements to munch the rules'
	 *  children if this rule matches. */
	StringBuffer munchStms;

	/** constantly updated (and reset) with current statement
	    prefix throughout recursive calls. */
	String stmPrefix;

	/** number of nodes "munched" by this so far. */
	int degree;
	
	/** Indentation. */
	String indentPrefix;
	
	/** Type of root. */
	String rootType;

	/** Helper function to merge a TypeExpRecurse into this one. */
	public void mergeStms(TypeExpRecurse r) {
	    initStms.append(r.initStms);
	    munchStms.append(r.munchStms);
	}
	/** helper function to prettify output */
	public void append(StringBuffer buf, String s) {
	    buf.append(indentPrefix + s + "\n");
	}

	TypeStmRecurse(String stmPrefix, String indentPrefix) {
	    // hack to make everything else additive
	    exp = new StringBuffer("true\n"); 
	    initStms = new StringBuffer();
	    munchStms = new StringBuffer();
	    degree = 0;
	    this.stmPrefix = stmPrefix;
	    this.indentPrefix = indentPrefix;
	}
	
	public void visit(Spec.Stm s) {
	    assert false : "StmRecurse should never visit Stm: " + s + 
			" Class:" + s.getClass();
	}

	public void visit(Spec.StmMethod s) {
	    if (rootType==null) rootType=TREE_METHOD;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof " + TREE_METHOD + " ");

	    // initialize params
	    append(munchStms, TEMP_Temp+"[] "+s.params+" = new "+TEMP_Temp+"["+
		           "(("+TREE_METHOD+")"+stmPrefix+").getParamsLength()];");
	    append(munchStms, "for (int _i_=0; _i_<"+s.params+".length; _i_++)");
	    append(munchStms, "  "+s.params+"[_i_] = munchExp("+
		           "(("+TREE_METHOD+")"+stmPrefix+").getParams(_i_));");
	}
	
	public void visit(Spec.StmCall s) {
	    if (rootType==null) rootType=TREE_CALL;
	    degree++;
	    
	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof " + TREE_CALL +" ");
	    
	    // look at func
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_CALL+") " +stmPrefix +").getFunc()", 
			       indentPrefix + "\t");
	    s.func.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() +indentPrefix+")");
	    mergeStms(r);

	    // initialize retval
	    String retval = "(("+TREE_CALL+")"+stmPrefix + ").getRetval()";
	    append(munchStms, TEMP_Temp+" "+s.retval+" = "+
		   "("+retval+"==null) ? null : "+
		   "munchExp("+retval+");");

	    // initialize retex
	    String retex = "(("+TREE_CALL+")"+stmPrefix + ").getRetex()";
	    append(munchStms, TEMP_Temp+" "+s.retex+" = "+
		   "munchExp("+retex+");");

	    // initialize handler
	    append(initStms, TEMP_Label+" "+s.handler+" = "+
		   "(("+TREE_CALL+")"+stmPrefix + ").getHandler().label;");

	    // initialize arg list
	    append(munchStms, "/* munch argument ExpList into a TempList */");
	    append(munchStms, TEMP_TempList+" "+ s.arglist +
		   " = new "+TEMP_TempList+"(null, null);");
	    append(munchStms, "{ "+TEMP_TempList+" tl="+s.arglist+";");
	    append(munchStms, "  for ("+TREE_ExpList+" el"+
		   " = (("+TREE_CALL+")"+stmPrefix+").getArgs();" +
		   " el!=null; el=el.tail, tl=tl.tail) ");
	    append(munchStms, "    tl.tail = new "+TEMP_TempList+"("+
		   "munchExp(el.head), null);");
	    append(munchStms, "}");
	    append(munchStms, s.arglist+" = "+s.arglist+".tail;");
	}

	public void visit(Spec.StmCjump s) {
	    if (rootType==null) rootType=TREE_CJUMP;
	    degree++;
	    
	    append(exp, "// check statement type");
	    append(exp, "&& (" + stmPrefix + " instanceof "+TREE_CJUMP+")");
	    
	    // look at test
	    TypeExpRecurse r = new
		TypeExpRecurse("(("+TREE_CJUMP+") " + stmPrefix + ").getTest()",
			       indentPrefix + "\t");
	    s.test.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() + indentPrefix+")");
	    mergeStms(r);

	    // code to initialize the target label identifiers so
	    // that they can be referred to.
	    append(initStms, TEMP_Label +" "+ s.f_label + 
			    " = (("+TREE_CJUMP+")"+stmPrefix+
			    ").iffalse;"); 
	    append(initStms, TEMP_Label +" "+ s.t_label +  
			    " = (("+TREE_CJUMP+")"+stmPrefix+
			    ").iftrue;");

	}

	public void visit(Spec.StmData s) {
	    if (rootType==null) rootType=TREE_DATUM;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_DATUM+"");
	    
	    // look at exp
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_DATUM+")"+ stmPrefix + ").getData()",
			       indentPrefix + "\t");
	    s.data.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() + indentPrefix+")");
	    mergeStms(r);
	}

	public void visit(Spec.StmAlign s) {
	    if (rootType==null) rootType=TREE_ALIGN;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_ALIGN+"");
	    
	    s.alignment.accept(new Spec.LeafVisitor() {
		public void visit(Spec.Leaf l) {
		    assert false : "Should never visit generic Leaf in StmAlign";
		}
		public void visit(Spec.LeafNumber l) {
		    append(exp, "&& ((" + TREE_ALIGN + ")"+stmPrefix+
			   ").alignment == " + l.number.intValue());
		}
		public void visit(Spec.LeafId l) {
		    initStms.append("int " + l.id + " = ((" + TREE_ALIGN + ")"+
				    stmPrefix+").alignment;");
		}
	    });
	}

	public void visit(Spec.StmSegment s) {
	    if (rootType==null) rootType=TREE_SEGMENT;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_SEGMENT+"");
	    
	    s.segtype.accept(new Spec.LeafVisitor() {
		public void visit(Spec.Leaf l) {
		    assert false : "Should never visit generic Leaf in StmSegment";
		}
		public void visit(Spec.LeafSegType l) {
		    append(exp, "&& ((" + TREE_SEGMENT + ")"+stmPrefix+
			   ").segtype == " +
			   TREE_SEGMENT + "." + SEGMENT.decode(l.segtype));
		}
		public void visit(Spec.LeafId l) {
		    initStms.append("int "+l.id + " = ((" + TREE_SEGMENT + ")"+
				    stmPrefix+").segtype;");
		}
	    });
	}

	public void visit(Spec.StmExp s) {
	    if (rootType==null) rootType=TREE_EXPR;
	    degree++;
	    
	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_EXPR+"");
	    
	    // look at exp
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_EXPR+")"+ stmPrefix + ").getExp()",
			       indentPrefix + "\t");
	    s.exp.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() + indentPrefix+")");
	    mergeStms(r);
	}

	public void visit(Spec.StmJump s) {
	    if (rootType==null) rootType=TREE_JUMP;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_JUMP+"");
	    
	    // look at exp
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_JUMP+")" + stmPrefix + ").getExp()",
			       indentPrefix + "\t");
	    s.exp.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() + indentPrefix+")");
	    mergeStms(r);
	}

	public void visit(Spec.StmLabel s) {
	    if (rootType==null) rootType=TREE_LABEL;
	    degree++;
	    
	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_LABEL+" ");

	    // code to initialize the target label identifiers so that
	    // they can be referred to.
	    append(initStms, "String " + s.name + 
			    " = (("+TREE_LABEL+")"+stmPrefix+
			    ").label.toString();\n");
	}

	public void visit(Spec.StmMove s) {
	    if (rootType==null) rootType=TREE_MOVE;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_MOVE+" ");

	    String checkPrefix = "(("+TREE_MOVE+")" + stmPrefix + ")";
	    appendTypeCheck(this, exp, checkPrefix, s.types);
	    
	    // look at src
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_MOVE+") " + stmPrefix + ").getSrc()",
			       indentPrefix + "\t");
	    s.src.accept(r);
	    degree += r.degree;
	    append(exp, indentPrefix + "&& (" + r.exp.toString() + indentPrefix  +")");
	    mergeStms(r);

	    // look at dst
	    r = new TypeExpRecurse("(("+TREE_MOVE+") " + stmPrefix + ").getDst()",
				   indentPrefix + "\t");
	    s.dst.accept(r);
	    degree += r.degree;
	    append(exp, "&& (" + r.exp.toString() + indentPrefix+")");
	    mergeStms(r);
	}

	public void visit(Spec.StmNativeCall s) {
	    if (rootType==null) rootType=TREE_NATIVECALL;
	    degree++;

	    append(exp, "// check statement type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_NATIVECALL+"");

	    // look at func
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_NATIVECALL+") " +stmPrefix +").getFunc()", 
			       indentPrefix + "\t");
	    s.func.accept(r);
	    degree += r.degree;
	    append(exp, indentPrefix + "&& (" + r.exp.toString() +indentPrefix+")");
	    mergeStms(r);

	    // initialize retval
	    String retval = "(("+TREE_NATIVECALL+")"+stmPrefix+").getRetval()";
	    append(munchStms, TEMP_Temp+" "+s.retval+" = "+
		   "("+retval+"==null) ? null : "+
		   "munchExp("+retval+");");
	    
	    // initialize arg list
	    append(munchStms, "/* munch argument ExpList into a TempList */");
	    append(munchStms, TEMP_TempList+" "+ s.arglist +
		   " = new "+TEMP_TempList+"(null, null);");
	    append(munchStms, "{ "+TEMP_TempList+" tl="+s.arglist+";");
	    append(munchStms, "  for ("+TREE_ExpList+" el"+
		   " = (("+TREE_NATIVECALL+")"+stmPrefix+").getArgs();" +
		   " el!=null; el=el.tail, tl=tl.tail) ");
	    append(munchStms, "    tl.tail = new "+TEMP_TempList+"("+
		   "munchExp(el.head), null);");
	    append(munchStms, "}");
	    append(munchStms, s.arglist+" = "+s.arglist+".tail;");
	}

	public void visit(Spec.StmReturn s) {
	    if (rootType==null) rootType=TREE_RETURN;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_RETURN+"");

	    String checkPrefix = "(("+TREE_RETURN+")" + stmPrefix + ")";
	    appendTypeCheck(this, exp, checkPrefix, s.types);
	    
	    // look at exp
	    TypeExpRecurse r = new
		TypeExpRecurse("(("+TREE_RETURN+") " + stmPrefix + ").getRetval()",
			       indentPrefix + "\t");
	    s.retval.accept(r);
	    degree += r.degree;
	    append(exp, indentPrefix + "&& (" + r.exp.toString() +indentPrefix+")");
	    mergeStms(r);
	}


	public void visit(Spec.StmThrow s) {
	    if (rootType==null) rootType=TREE_THROW;
	    degree++;
	    
	    append(exp, "// check expression type");
	    append(exp, "&& " + stmPrefix + " instanceof "+TREE_THROW+"");

	    // look at exp
	    TypeExpRecurse r = new 
		TypeExpRecurse("(("+TREE_THROW+") " + stmPrefix + ").getRetex()",
			       indentPrefix + "\t");
	    s.retex.accept(r); 
	    degree += r.degree;
	    append(exp, indentPrefix + "&& (" + r.exp.toString() +indentPrefix+ ")");
	    mergeStms(r);

	    // look at handler
	    r = new 
		TypeExpRecurse("(("+TREE_THROW+") " + stmPrefix + ").getHandler()",
			       indentPrefix + "\t");
	    s.handler.accept(r); 
	    degree += r.degree;
	    append(exp, indentPrefix + "&& (" + r.exp.toString() +indentPrefix+ ")");
	    mergeStms(r);
	}
    }



    /** Sets up a series of checks to ensure all of the values
	in the visited expression are of the appropriate
	type. 
    */
    static class TypeExpRecurse extends Spec.ExpVisitor
	implements AppendingVisitor {
	/** constantly updated boolean expression to match a tree's
	    types. */ 
	StringBuffer exp;
	
	/** constantly updated set of statements to initialize the
            identifiers that the predicate expression will reference. */
	StringBuffer initStms;
	/** constantly updated set of statements to munch the rules'
	 *  children if this rule matches. */
	StringBuffer munchStms;

	/** constantly updated (and reset) with current expression
	    prefix throughout recursive calls. */ 
	String expPrefix;
		
	/** number of nodes "munched" by this so far. */
	int degree;
	
	/** Indentation. */
	String indentPrefix;

	/** Type of root. */
	String rootType;

	/** Helper function to merge a TypeExpRecurse into this one. */
	public void mergeStms(TypeExpRecurse r) {
	    initStms.append(r.initStms);
	    munchStms.append(r.munchStms);
	}
	/** Helper function to prettify resulting code. */
	public void append(StringBuffer buf, String s) {
	    buf.append(indentPrefix + s +"\n");
	}

	TypeExpRecurse(String expPrefix, String indentPrefix) {
	    // hack to make everything else additive
	    exp = new StringBuffer("true\n"); 	    
	    initStms = new StringBuffer();
	    munchStms = new StringBuffer();
	    
	    degree = 0;
	    this.expPrefix = expPrefix;
	    this.indentPrefix = indentPrefix;
	}
	
	public void visit(Spec.Exp e) {
	    assert false : "ExpRecurse should never visit Exp: "+e + 
			" Class: " + e.getClass();
	}
	public void visit(final Spec.ExpBinop e) { 
	    if (rootType==null) rootType=TREE_BINOP;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_BINOP);

	    // My god, I can't tell if this visitor is good or bad for
	    // understanding the code!
	    e.opcode.accept(new Spec.LeafVisitor() {
		public void visit(Spec.Leaf l) {
		    assert false : "Should never visit generic Leaf in ExpBinop";
		}
		public void visit(Spec.LeafOp l) {
		    append(exp, "// check opcode");
		    append(exp, "&& ((" + TREE_BINOP + ")" + expPrefix + ").op == " + TREE_BOP+"."+Bop.toString(l.op).toUpperCase());
		}
		public void visit(Spec.LeafId l) {
		    append(initStms, "int " + l.id + " = ((" + TREE_BINOP + ") " + 
			   expPrefix + ").op;");
		}
	    });

	    appendTypeCheck(this, exp, expPrefix, e.types);

	    // save state before outputing children-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";
	    
	    append(exp, "// check left child");
	    expPrefix = "((" + TREE_BINOP + ")" + oldPrefix + ").getLeft()";
	    e.left.accept(this);
	    append(exp, "// check right child");  
	    expPrefix = "((" + TREE_BINOP + ")" + oldPrefix + ").getRight()";
	    e.right.accept(this);
	    
	    // restore original state
	    indentPrefix = oldIndent;
	    expPrefix = oldPrefix;
		       
	}
	
	public void visit(Spec.ExpConst e) {
	    if (rootType==null) rootType=TREE_CONST;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_CONST + " ");

	    appendTypeCheck(this, exp, expPrefix, e.types);

	    e.value.accept(new Spec.LeafVisitor() {
		public void visit(Spec.Leaf l) {
		    assert false : "Should never visit generic Leaf in ExpConst";
		}
		public void visit(Spec.LeafId l) {
		    append(initStms, "Number " + l.id + " = ((" + TREE_CONST + ") " + 
			   expPrefix + ").value;");
		}
		public void visit(Spec.LeafNumber l) {
		    append(exp, "// check that constant value matches");
		    append(exp, "&& ( " + expPrefix + ".isFloatingPoint()?");
		    append(exp, "((" + TREE_CONST + ")" + expPrefix + ").value.doubleValue() == " + l.number.doubleValue() + ":");
		    append(exp, "((" + TREE_CONST + ")" + expPrefix + ").value.longValue() == " + l.number.longValue() + ")");
		}
		public void visit(Spec.LeafNull l) {
		    append(exp, "&& " + expPrefix + ".type() == Type.POINTER ");
		}
	    });
	}
	public void visit(Spec.ExpId e) {
	    // don't increase the munch-factor (ie
	    // 'degree') ( Spec.ExpId is strictly a way for a
	    // specification to refer back to items in the parsed
	    // tree)
	    append(exp, "// no check needed for ExpId children");
	    append(munchStms, TEMP_Temp +" "+ e.id +" = munchExp(" + expPrefix + "); ");
	    return;
	}
	public void visit(Spec.ExpMem e) { 
	    if (rootType==null) rootType=TREE_MEM;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_MEM + " ");

	    appendTypeCheck(this, exp, expPrefix, e.types);

	    // save state before outputing child-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";

	    append(exp, "// check child");
	    expPrefix = "((" + TREE_MEM + ")" + oldPrefix + ").getExp()";
	    e.addr.accept(this);

	    // restore original state
	    indentPrefix = oldIndent;
	    expPrefix = oldPrefix;

	}
	public void visit(Spec.ExpName e) { 
	    if (rootType==null) rootType=TREE_NAME;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_NAME + " ");
	    
	    append(initStms, TEMP_Label +" "+ e.name + " = ((" +TREE_NAME + ")"+
		   expPrefix + ").label;");
	    
	}
	public void visit(Spec.ExpTemp e) { 
	    if (rootType==null) rootType=TREE_TEMP;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_TEMP +" ");
	    
	    appendTypeCheck(this, exp, expPrefix, e.types);

	    append(initStms, TEMP_Temp +" "+ e.name + " = "+
		   "makeTemp((" +TREE_TEMP + ")"+expPrefix+", "+
		   "inf.tempFactory());");
	}
	public void visit(Spec.ExpUnop e) { 
	    if (rootType==null) rootType=TREE_UNOP;
	    degree++;

	    append(exp, "// check expression type");
	    append(exp, "&& " + expPrefix + " instanceof " + TREE_UNOP + " ");

	    e.opcode.accept(new Spec.LeafVisitor() {
		public void visit(Spec.Leaf l) {
		    assert false : "Should never visit generic Leaf in ExpUnop";
		}
		public void visit(Spec.LeafOp l) {
		    append(exp, "// check opcode");
		    append(exp, "&& ((" + TREE_UNOP + ")" + expPrefix + ").op == " + TREE_UOP+"."+Uop.toString(l.op).toUpperCase());
		}
		public void visit(Spec.LeafId l) {
		    append(initStms, "int " + l.id + " = ((" + TREE_UNOP + ") " + 
			   expPrefix + ").op;");
		}
	    });

	    appendTypeCheck(this, exp, expPrefix, e.types);
	    
	    // save state before outputting child-checking code
	    String oldPrefix = expPrefix;
	    String oldIndent = indentPrefix;

	    indentPrefix = oldIndent + "\t";

	    append(exp, "// check child");
	    expPrefix = "((" + TREE_UNOP + ")" + oldPrefix + ").getOperand()";
	    e.exp.accept(this);

	    // restore state
	    expPrefix = oldPrefix;
	    indentPrefix = oldIndent;
	}
    }	    

    /** Append a type-checking expression to <code>exp</code>. */
    static void appendTypeCheck(AppendingVisitor av, StringBuffer exp,
				String tree, Spec.TypeSet types) {
	String PRECISELYTYPED="harpoon.IR.Tree.PreciselyTyped";
	av.append(exp, "// check operand types");
	boolean allowInt, allowLong, allowFloat, allowDouble, allowPointer;
	allowDouble = types.contains(Type.DOUBLE);
	allowFloat = types.contains(Type.FLOAT);
	allowInt = types.contains(Type.INT);
	allowLong = types.contains(Type.LONG);
	allowPointer = types.contains(Type.POINTER);
	av.append(exp, "&& ( ");
	String checkPrefix = "\t" + tree + ".type() ==";
	String precise = "(("+PRECISELYTYPED+")"+tree+")";
	if(allowDouble) av.append(exp, checkPrefix + " Type.DOUBLE ||");
	if(allowFloat) av.append(exp, checkPrefix + " Type.FLOAT ||");
	if(allowLong) av.append(exp, checkPrefix + " Type.LONG ||");
	if(allowPointer) av.append(exp, checkPrefix + " Type.POINTER ||");
	// INT is special because Small types are also INTs
	if(allowInt) {
	    av.append(exp, "("+checkPrefix + " Type.INT && !");
	    av.append(exp, " ("+tree+" instanceof "+PRECISELYTYPED+" &&");
	    av.append(exp, "  "+precise+".isSmall())) ||");
	}
	// check for small types.
	if(types.containsSmall()) {
	    av.append(exp, "\t(" + precise + ".isSmall() && (");
	    av.append(exp, "\t "+precise + ".signed() ? (");
	    for (int i=1; i<=32; i++)
		if (types.containsSigned(i))
		    av.append(exp, "\t  "+precise+".bitwidth()=="+i+" ||");
	    av.append(exp, "\t  "+"false) : (");
	    for (int i=1; i<=32; i++)
		if (types.containsUnsigned(i))
		    av.append(exp, "\t  "+precise+".bitwidth()=="+i+" ||");
	    av.append(exp, "\t  "+"false) ) ) ||");
	}
	av.append(exp, "\tfalse )"); 
	av.append(exp, "// end check operand types");
    }

    /**javac 1.1 sez this class (which is only used inside the 
     * outputSelectionMethod below) has to be out here, instead of
     * inside the method.  Actually, it doesn't specifically, but I can't
     * figure out how to properly qualify the references to PredicateBuilder
     * below to make them work right with javac 1.1, which gets confused 
     * over whose inner class PredicateBuilder is, actually.  
     * jikes likes fine, either way. go figure. [CSA] */
    static class PredicateBuilder extends Spec.DetailVisitor {
	final StringBuffer predicate = new StringBuffer("true");
	public void visit(Spec.Detail d) { /* do nothing */ }
	public void visit(Spec.DetailPredicate d) { 
	    predicate.append("&& ("+d.predicate_string+")");
	}
    }
    /** Build 'extra' temps */
    static class ExtraBuilder extends Spec.DetailVisitor {
	final StringBuffer extras = new StringBuffer();
	public void visit(Spec.Detail d) { /* do nothing */ }
	public void visit(Spec.DetailExtra d) {
	    String clsname, tyname, isFloat, isDouble;
	    switch(d.type) {
	    case Type.INT:
		tyname="INT"; clsname="Int";
		isFloat="false"; isDouble="false"; break;
	    case Type.LONG:
		tyname="LONG"; clsname="Long";
		isFloat="false"; isDouble="true"; break;
	    case Type.FLOAT:
		tyname="FLOAT"; clsname="Float";
		isFloat="true"; isDouble="false"; break;
	    case Type.DOUBLE:
		tyname="DOUBLE"; clsname="Double";
		isFloat="true"; isDouble="true"; break;
	    case Type.POINTER:
		tyname="POINTER"; clsname="Void";
		isFloat="false"; isDouble="frame.pointersAreLong()"; break;
	    default: throw new Error("unknown type!");
	    }
	    for(Spec.IdList ilp = d.extras; ilp!=null; ilp=ilp.tail) {
		extras.append("Temp "+ilp.head+" = frame.getTempBuilder()."+
			      "makeTemp(new Typed() {\n");
		extras.append("\tpublic int type() { return "+
			      TREE_Type+"."+tyname+"; }\n");
		extras.append("\tpublic boolean isFloatingPoint() { return "+
			      isFloat+"; }\n");
		extras.append("\tpublic boolean isDoubleWord() { return "+
			      isDouble+"; }\n");
		extras.append("}, inf.tempFactory() );\n");
		if (d.type != Type.POINTER) // pointers must be declared expl.
		    extras.append("declare("+ilp.head+", "+
				  "HClass."+clsname+");\n");
	    }
	}
    }
    /** Writes the Instruction Selection Method to <code>out</code>.
	<BR> <B>modifies:</B> <code>out</code>
	<BR> <B>effects:</B>
	     Generates Java source for a MaximalMunch instruction
	     selection method, not including method signature or
	     surrounding braces.  Outputs generated source to
	     <code>out</code>. 
	@param out Target output device for the Java source code.
	@param isData indicates if we're pattern matching code or data tables
    */
    public void outputSelectionMethod(final PrintWriter out, 
				      final boolean isData) { 
	// traverse 'this.spec' to acquire spec information
	final List expMatchActionPairs = new LinkedList(); // list of RuleTuple
	final List stmMatchActionPairs = new LinkedList();

	final String expArg = "expArg";
	final String stmArg = "stmArg";
	final String indent = "\t\t\t";


	Spec.RuleVisitor srv = new Spec.RuleVisitor() {
	    public void visit(Spec.Rule r) {
		assert false : "SpecRuleVisitor should never visit Rule";
	    }
	    public void visit(Spec.RuleStm r) { 
		if (isData && !r.stm.canBeRootOfData()) {
		    return;
		}

		TypeStmRecurse recurse = 
		    new TypeStmRecurse(stmArg, indent + "\t");
		r.stm.accept(recurse);

		String typeCheck = recurse.exp.toString();
		int munchFactor = recurse.degree;

		PredicateBuilder makePred = new PredicateBuilder();
	        if (r.details!=null) r.details.accept(makePred);
		ExtraBuilder makeExtra = new ExtraBuilder();
		if (r.details!=null) r.details.accept(makeExtra);

		String matchStm = (indent + "if (" + typeCheck + indent + "){\n"+
				   recurse.initStms.toString() +
				   indent + recurse.rootType + " ROOT = (" + recurse.rootType + ") " + stmArg + ";\n" +
				   indent + "\t_matched_ = " + makePred.predicate.toString()+";\n" +
				   "");

		//String matchStm = indent + "_matched_ = (" + typeCheck + indent + ");";
		stmMatchActionPairs.add( new RuleTuple
					 ( r.stm.toString(),
					   matchStm, 
					   indent + recurse.munchStms + "\n" +
					   // declare and assign %extra Temps 
					   makeExtra.extras.toString()+
					   indent + r.action_str + 
					   indent + "return;" +
					   indent + "}", null,
					   recurse.degree ) );
	    }
	    public void visit(Spec.RuleExp r) { 
		if (isData) return;

		TypeExpRecurse recurse = 
		    new TypeExpRecurse(expArg, indent + "\t");
		r.exp.accept(recurse);

		String typeCheck = recurse.exp.toString();
		int munchFactor = recurse.degree;
		
		PredicateBuilder makePred = new PredicateBuilder();
		if (r.details!=null) r.details.accept(makePred);
		ExtraBuilder makeExtra = new ExtraBuilder();
		if (r.details!=null) r.details.accept(makeExtra);
		
		// TODO: add PREDICATE CHECK to end of matchStm
		String matchStm = 
		(indent + "if ("+typeCheck+indent+"){\n" +
		 recurse.initStms.toString() + indent + recurse.rootType + 
		 " ROOT = (" + recurse.rootType + ") " + expArg + ";\n" +
		 indent + "\t_matched_ = "+makePred.predicate.toString()+";\n");
		//String matchStm = indent + "_matched_ =  (" + typeCheck + indent + ");";
		expMatchActionPairs.add( new RuleTuple
					 ( r.exp.toString(),
					   matchStm, 
					   recurse.munchStms.toString()+"\n"+
					   "Temp " + r.result_id + " = " +
					   "frame.getTempBuilder()."+
					   "makeTemp( ROOT , inf.tempFactory());\n" +

					   // insert a type declare
					   // for r.result.id 
					   (isData?"":"clearDecl();\n"+
					    "declare("+r.result_id+", "+
					   "code.getTreeDerivation(), ROOT);"+
					    "\n")+
					   
					   // declare and assign %extra Temps 
					   makeExtra.extras.toString()+
					   
					   r.action_str + 
					   indent + "return " + r.result_id + ";\n" +
					   indent + "}", r.result_id,
					   recurse.degree ) );
		
	    }

	};
	
	Spec.RuleList list = spec.rules;
	while(list != null) {
	    list.head.accept(srv);
	    list = list.tail;
	}

	
	/*
	Spec.TypeSet nonPtr = new Spec.TypeSet();
	nonPtr.set(Type.INT); nonPtr.set(Type.LONG);
	nonPtr.set(Type.FLOAT); nonPtr.set(Type.DOUBLE);
	Spec.TypeSet ptr = new Spec.TypeSet(Type.POINTER);
	Spec.RuleExp tempRuleNP = new Spec.RuleExp
	    (new Spec.ExpTemp(nonPtr, "t_orig"), "t_new",
	     null, "Temp tt = frame.getTempBuilder().makeTemp( ROOT, inf.tempFactory());\n");
	Spec.RuleExp tempRuleP = new Spec.RuleExp
	    (new Spec.ExpTemp(ptr,    "p_orig"),"p_new",
	     null, "Temp pp = frame.getTempBuilder().makeTemp( ROOT, inf.tempFactory());\n");	

	tempRuleNP.accept(srv);
	tempRuleP.accept(srv);
	*/

	Comparator compare = new RuleTupleComparator();
	Collections.sort(expMatchActionPairs, compare);
	Collections.sort(stmMatchActionPairs, compare);
	
	// Implement a recursive function by making a helper class to
	// visit the nodes
	out.println("\tfinal class CggVisitor extends "+TREE_TreeVisitor+" {");

	// for each rule for an exp we need to implement a
	// clause in munchExp()
	out.println("\t\t " + TEMP_Temp + " munchExp(" + TREE_Exp +" "+ expArg + ") {");
	
	out.println("\t\t\tboolean _matched_ = false;");
	out.println("\t\t\tclearDecl(); // reset temp type mappings");

	Iterator expPairsIter = expMatchActionPairs.iterator();
	
	int i = 1;

	while(expPairsIter.hasNext()) {
	    RuleTuple triplet = (RuleTuple) expPairsIter.next();
	    // out.println("System.out.println(\"Checkpoint Exp: "+(i++)+" \");");
	    out.println("\t\t\t\t /* " + triplet.rule + " */");
	    out.println(triplet.matchStms);
	    out.println("\t\t\t\tif (_matched_) { // action code! degree: "+triplet.degree);
	    out.println(triplet.actionStms);
	    //out.println("\t\t\t\treturn " + triplet.resultId + ";");
	    out.println("\t\t\t}");
	}
	
	out.println("\t\tassert false : \"Uh oh...\\n"+
		    "maximal munch didn't match anything...SPEC file\\n"+
		    "is not complete enough for this program\\n"+
		    "Died on \"+prettyPrint("+expArg+")+\" in \" + prettyPrint(globalStmArg);"); 
	out.println("\t\treturn null; // doesn't matter, we're dead if we didn't match...");
	out.println("\t\t } // end munchExp");
	
	out.println("\t"+TREE_Stm +" globalStmArg=null;");

	// for each rule for a statement we need to implement a
	// clause in munchStm()
	out.println("\t\t void munchStm("+TREE_Stm + " " + stmArg + ") {");
	out.println("\t\t\t globalStmArg = " + stmArg +";");
	out.println("\t\t\tboolean _matched_ = false;");
	out.println("\t\t\tclearDecl(); // reset temp type mappings");
	
	Iterator stmPairsIter = stmMatchActionPairs.iterator();
	i=1;
	while(stmPairsIter.hasNext()) {
	    RuleTuple triplet = (RuleTuple) stmPairsIter.next();
	    // out.println("System.out.println(\"Checkpoint Stm: "+(i++)+" \");");
	    out.println("\t\t\t\t /* " + triplet.rule + " */");
	    out.println(triplet.matchStms);
	    out.println("\t\t\t\tif (_matched_) { // action code! : degree "+triplet.degree);
	    out.println(triplet.actionStms);
	    out.println("\t\t\t}");
	}

	out.println("\t\tassert _matched_ : \"Uh oh...\\n"+
		    "maximal munch didn't match anything...SPEC file\\n"+
		    "is not complete enough for this program\\n"+
		    "Died on \"+prettyPrint("+stmArg+")+\" in \" + prettyPrint(globalStmArg);"); 
	out.println("\t\t} // end munchStm");
	
	out.println("\t\tpublic void visit("+TREE_Tree+" treee){");
	out.println("\t\t\tassert false : "+
		    "\"Should never visit generic " + TREE_Tree + 
		    "in CggVisitor\";");
	out.println("\t\t} // end visit("+TREE_Tree+")");

	out.println("\t\tpublic void visit("+TREE_Stm+" treee){");
	out.println("\t\t\tdebug(\"munching \"+treee+\"\t\");");
	out.println("\t\t\tmunchStm(treee);");
	out.println("\t\t} // end visit("+TREE_Stm+")");
	
	out.println("\t\tpublic void visit("+TREE_SEQ+" treee){");
	out.println("\t\t\ttreee.getLeft().accept(this);");
	out.println("\t\t\ttreee.getRight().accept(this);");
	out.println("\t\t}");
	// BAD DOG!  Don't implement visit(TREE_Exp)...we should never
	// be munching those directly; only from calls to visit(TREE_Stm) 
	
	out.println("\t}"); // end CggVisitor
	
	out.println("\tCggVisitor visitor = new CggVisitor();");
	out.println("\t"+TREE_Tree+" t = ("+TREE_Tree+") code.getRootElement();");

	//out.println("\twhile(t instanceof "+TREE_SEQ+") {");
	//out.println("\t\t"+TREE_SEQ+" seq = ("+TREE_SEQ+")t;");
	//out.println("\t\tseq.getLeft().accept(visitor);");
	//out.println("\t\tt=seq.getRight();");
	//out.println("\t}");
	out.println("\tt.accept(visitor);");
	
	out.println("\t\t\tclearDecl(); // reset temp type mappings");
    }

    static class RuleTuple {
	final String matchStms, actionStms, rule, resultId; 
	final int degree;
	    
	/** Constructs a new <code>RuleTuple</code>.
	    @param rule A <code>String</code> describing the rule
	    @param matchStms A series of Java statements which will set 
	                    _matched_ to TRUE if expression matches
			    and initialize variables that could be
			    references in 'actionStms'.  Note that if the
			    matchStms start a new scope for the
			    variables to be initialized in (for
			    example, inside the then-clause of an
			    if-statement) then 'actionStms' must end
			    it, so that the RuleTuple is completely
			    selfcontained. 
	    @param actionStms A series of Java statements to execute if 
	                      _matched_ == TRUE after 'matchExp' executes 
	    @param resultId The identifier to return after
	                      performing <code>actionStms</code>.
			      Will be <code>null</code> for
			      <code>RuleStm</code>s. 
	    @param degree Number of nodes that this rule "eats"
	*/ 
	RuleTuple(String rule, String matchStms, String actionStms, String resultId, int degree) {
	    this.rule = rule;
	    this.matchStms = matchStms;
	    this.actionStms = actionStms;
	    this.degree = degree;
	    this.resultId = resultId;
	}
    }
    
    static class RuleTupleComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    RuleTuple r1 = (RuleTuple) o1;
	    RuleTuple r2 = (RuleTuple) o2;
	    return -(r1.degree - r2.degree);
	}
    }

}
