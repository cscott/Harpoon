// TreeToC.java, created Thu Jun 22 14:23:38 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.ClassFile.*;
import harpoon.IR.Tree.*;
import harpoon.Temp.LabelList;
import java.io.*;
/**
 * <code>TreeToC</code> converts Tree form to C code (used as a
 * "portable assembly language").
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeToC.java,v 1.1.2.1 2000-06-23 23:08:34 cananian Exp $
 */
public class TreeToC {
    
    /** Creates a <code>TreeToC</code>. */
    public TreeToC() {
        
    }
    public static void test(PrintWriter out, HCode hc) {
	test(out, (Tree)hc.getRootElement(), true);
    }
    public static void test(PrintWriter out, HData hd) {
	test(out, (Tree)hd.getRootElement(), false);
    }
    private static void test(PrintWriter out, Tree t, boolean isCode) {
	TranslationVisitor tv = new TranslationVisitor(out);
	if (t!=null) tv.translate(t, isCode);
    }

    /** Tree Visitor does the real work. */
    private static class TranslationVisitor extends TreeVisitor {
	// default case throws error: we should handle each tree specifically.
	public void visit(Tree e) {
	    throw new Error("Unmatched Tree: "+e);
	}
	// PrintWriter for the translation.
	private PrintWriter pw;
	TranslationVisitor(Writer w) { pw = new PrintWriter(w); }
	TranslationVisitor(OutputStream w) { pw = new PrintWriter(w); }
	void translate(Tree t, boolean isCode) {
	    trans(t);
	    if (isCode) pw.println("}");
	    pw.flush();
	}
	// useful line number update function.
	private boolean EMIT_LINE_DIRECTIVES=false;
	private String last_file = null;
	private int last_line = 0;
	private void updateLine(Tree e) {
	    if (!EMIT_LINE_DIRECTIVES) return;
	    String curr_file = e.getSourceFile();
	    int curr_line = e.getLineNumber();
	    if (last_line == curr_line &&
		(last_file==null?curr_file==null:last_file.equals(curr_file)))
		return;
	    pw.println();
	    pw.print("#line "+curr_line);
	    if (!(last_file==null?curr_file==null:last_file.equals(curr_file)))
		pw.print(" \""+curr_file+"\"");
	    pw.println();
	    last_file = curr_file;
	    last_line = curr_line;
	}
	private void trans(Tree e) { updateLine(e); e.accept(this); }
	private String ctype(Typed t) {
	    String result;
	    if (t instanceof PreciselyTyped) {
		PreciselyTyped pt = (PreciselyTyped) t;
		if (pt.isSmall())
		    return (pt.signed()?"int":"u_int")+pt.bitwidth()+"_t";
	    }
	    return ctype(t.type());
	}
	private String ctype(int type) {
	    if (type==Type.DOUBLE) return "jdouble";
	    if (type==Type.FLOAT) return "jfloat";
	    if (type==Type.INT) return "jint";
	    if (type==Type.LONG) return "jlong";
	    if (type==Type.POINTER) return "void *";
	    throw new Error("unknown type: "+type);
	}

	// okay, shoot:
	public void visit(ALIGN e) {
	    // ack! unimpl!
	    pw.print("/* align "+e.alignment+" */");
	}
	public void visit(BINOP e) {
	    pw.print("(");
	    if (e.op==Bop.SHR) pw.print("SHR("); // use macro
	    if (e.op==Bop.USHR) pw.print("USHR("); // use macro
	    if (e.type()==e.POINTER) pw.print("(void*)");
	    trans(e.getLeft());
	    switch(e.op) {
	    case Bop.CMPLT: pw.print("<"); break;
	    case Bop.CMPLE: pw.print("<="); break;
	    case Bop.CMPEQ: pw.print("=="); break;
	    case Bop.CMPNE: pw.print("!="); break;
	    case Bop.CMPGE: pw.print(">="); break;
	    case Bop.CMPGT: pw.print(">"); break;
	    case Bop.ADD: pw.print("+"); break;
	    case Bop.MUL: pw.print("*"); break;
	    case Bop.DIV: pw.print("/"); break;
	    case Bop.REM: pw.print("%/*rem*/"); break;
	    case Bop.SHL: pw.print("<<"); break;
	    case Bop.SHR: case Bop.USHR: pw.print(", "); break; // using macro
	    case Bop.AND: pw.print("&"); break;
	    case Bop.OR: pw.print("|"); break;
	    case Bop.XOR: pw.print("^"); break;
	    default: throw new Error("unknown Bop: "+e);
	    }
	    if (e.type()==e.POINTER) pw.print("(void*)");
	    trans(e.getRight());
	    if (e.op==Bop.SHR||e.op==Bop.USHR) pw.print(")"); // close macro
	    pw.print(")");
	}
	public void visit(CALL e) {
	    pw.println("\t/* call "+e+" */");
	}
	public void visit(CJUMP e) {
	    pw.print("\tif ("); trans(e.getTest()); pw.print(")");
	    pw.println(" goto "+e.iftrue+"; else goto "+e.iffalse+";");
	}
	public void visit(CONST e) {
	    pw.print(e.value==null?"NULL":e.value.toString());
	}
	public void visit(DATUM e) {
	    pw.print("\t/* datum "); trans(e.getData()); pw.println(" */");
	}
	public void visit(ESEQ e) {
	    throw new Error("Non-canonical tree form.");
	}
	public void visit(EXPR e) {
	    pw.print("\t"); trans(e.getExp()); pw.println(";");
	}
	public void visit(JUMP e) {
	    pw.print("\tgoto ");
	    Exp exp = e.getExp();
	    if (exp instanceof NAME)
		pw.print(((NAME)exp).label);
	    else { pw.print("*("); trans(exp); pw.print(")"); }
	    pw.println("; /* targets: "+LabelList.toList(e.targets)+" */");
	}
	public void visit(LABEL e) {
	    pw.println(e.label+":");
	}
	public void visit(MEM e) {
	    Exp exp = e.getExp();
	    pw.print("(*(");
	    pw.print("("+ctype(e)+"*)");
	    trans(exp);
	    pw.print("))");
	}
	public void visit(METHOD e) {
	    // emit declaration.
	    pw.print(e.getReturnType()<0 ? "void" : ctype(e.getReturnType()));
	    pw.print(" "+e.getMethod()+"(");
	    for (int i=0; i<e.getParamsLength(); i++) {
		pw.print(ctype(e.getParams(i))+" ");
		trans(e.getParams(i));
		if (i+1 < e.getParamsLength()) pw.print(", ");
	    }
	    pw.println(") {");
	}
	public void visit(MOVE e) {
	    pw.print("\t");
	    trans(e.getDst()); pw.print(" = "); trans(e.getSrc());
	    pw.println(";");
	}
	public void visit(NAME e) {
	    pw.print("(&"+e.label+")");
	}
	public void visit(NATIVECALL e) {
	    pw.print("\t");
	    if (e.getRetval()!=null) {
		trans(e.getRetval()); pw.print(" = ");
	    }
	    pw.print("*"); /* insert function type cast */
	    /* function type cast */
	    pw.print("(");
	    pw.print(e.getRetval()==null?"void":ctype(e.getRetval()));
	    pw.print("(*)(");
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		pw.print(ctype(el.head));
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.print("))");
	    /* function expression */
	    trans(e.getFunc());
	    pw.print("(");
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		trans(el.head);
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.println(");");
	}
	public void visit(RETURN e) {
	    pw.print("\treturn "); trans(e.getRetval()); pw.println(";");
	}
	public void visit(SEGMENT e) {
	    pw.println("\t/* segment: "+e+" */");
	}
	public void visit(SEQ e) {
	    trans(e.getLeft());
	    trans(e.getRight());
	}
	public void visit(TEMP e) {
	    pw.print(e.temp);
	}
	public void visit(THROW e) {
	    pw.println("\t/* THROW: "+e+" */");
	}
	public void visit(UNOP e) {
	    pw.print("(");
	    switch(e.op) {
	    case Uop.NEG: pw.print("-"); break;
	    case Uop.NOT: pw.print("~"); break;
	    case Uop._2B: pw.print("(jbyte)"); break;
	    case Uop._2C: pw.print("(jchar)"); break;
	    case Uop._2S: pw.print("(jshort)"); break;
	    case Uop._2I: pw.print("(jint)"); break;
	    case Uop._2L: pw.print("(jlong)"); break;
	    case Uop._2F: pw.print("(jfloat)"); break;
	    case Uop._2D: pw.print("(jdouble)"); break;
	    default: throw new Error("unknown Uop: "+e);
	    }
	    trans(e.getOperand()); pw.print(")");
	}
    }
}
