// TreeToC.java, created Thu Jun 22 14:23:38 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HData;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.PreciselyTyped;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.Util;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>TreeToC</code> converts Tree form to C code (used as a
 * "portable assembly language").
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeToC.java,v 1.1.2.8 2000-06-28 20:06:46 cananian Exp $
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
	if (t!=null)
	    out.print(new TranslationVisitor().translate(t, isCode));
    }

    /** Tree Visitor does the real work. */
    private static class TranslationVisitor extends TreeVisitor {
	// default case throws error: we should handle each tree specifically.
	public void visit(Tree e) {
	    throw new Error("Unmatched Tree: "+e);
	}
	// StringWriters and PrintWriters for the translation.
	// four output sections: gbl_decls, meth_decls, meth_body, gbl_data
	/** Method declaration and local variables */
	private final int MD=0;
	/** Method body. */
	private final int MB=1;
	/** Data declarations (as struct) */
	private final int DD=2;
	/** Data initializations. */
	private final int DI=3;
	/** Number of sections */
	private final int SECTIONS=4;

	private StringWriter[] swa = new StringWriter[SECTIONS];
	private PrintWriter[] pwa = new PrintWriter[SECTIONS];
	private PrintWriter pw;
	TranslationVisitor() {
	    for (int i=0; i<SECTIONS; i++) {
		swa[i] = new StringWriter();
		pwa[i] = new PrintWriter(swa[i]);
	    }
	    pw = null;
	}
	// keep track of current alignment, section, method, etc.
	ALIGN align = null;
	SEGMENT segment = null;
	METHOD method = null;
	LABEL label = null;
	int field_counter = 0;

	// switch from one mode to the next.
	private static final int NONE = 0;
	private static final int DATA = 1;
	private static final int CODE = 2;
	int current_mode = NONE;
	void switchto(int mode) {
	    switch(current_mode) {
	    case CODE:
		flushAndAppend(MD);
		pwa[MB].println("}");
		flushAndAppend(MB);
		temps_seen.clear();
		break;
	    case DATA:
		pwa[DI].println("};");
		flushAndAppend(DD);
		flushAndAppend(DI);
		break;
	    default: break;
	    }
	    current_mode = mode;
	    switch(current_mode) {
	    case CODE:
		pw = pwa[MD];
		break;
	    case DATA:
		field_counter = 0;
		pw = pwa[DI];
		break;
	    default: break;
	    }
	}
	StringBuffer output = new StringBuffer();
	void flushAndAppend(int which) {
	    pwa[which].flush();
	    StringBuffer sb = swa[which].getBuffer();
	    output.append(sb);
	    sb.setLength(0);
	}

	/** these are the symbols referenced (and declarations for them) */
	Map sym2decl = new HashMap();
	/** these are the *local* labels which are defined in this file. */
	Set local_labels = new HashSet();
	String translate(Tree t, boolean isCode) {
	    // foreach tree...
	    switchto(NONE);
	    trans(t);
	    switchto(NONE);
	    // collect symbol declarations.
	    StringWriter mysw = new StringWriter();
	    PrintWriter mypw = new PrintWriter(mysw);
	    mypw.println("#include <precisec.h>");
	    for (Iterator it=sym2decl.keySet().iterator(); it.hasNext(); ) {
		Label l = (Label) it.next();
		if (!local_labels.contains(l))
		    mypw.println(sym2decl.get(l));
	    }
	    // now collect output.
	    mypw.print(output);
	    mypw.close();
	    return mysw.getBuffer().toString();
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
	private static int sizeof(Typed t) {
	    if (t instanceof PreciselyTyped) {
		PreciselyTyped pt = (PreciselyTyped) t;
		if (pt.isSmall())
		    return (pt.bitwidth()+7) / 8;
	    }
	    return t.isDoubleWord() ? 8 : 4;
	}
	private static String ctype(Typed t) {
	    String result;
	    if (t instanceof PreciselyTyped) {
		PreciselyTyped pt = (PreciselyTyped) t;
		if (pt.isSmall())
		    return (pt.signed()?"int":"u_int")+pt.bitwidth()+"_t";
	    }
	    return ctype(t.type());
	}
	private static String ctype(int type) {
	    if (type==Type.DOUBLE) return "jdouble";
	    if (type==Type.FLOAT) return "jfloat";
	    if (type==Type.INT) return "jint";
	    if (type==Type.LONG) return "jlong";
	    if (type==Type.POINTER) return "jptr";
	    throw new Error("unknown type: "+type);
	}
	private static String label(Label l) {
	    String r = l.toString();
	    return r.startsWith(".")?r.substring(1):r;
	}
	private static String sectionname(SEGMENT s) {
	    switch (s.segtype) {
	    case SEGMENT.TEXT: return ".text";
	    case SEGMENT.ZERO_DATA: return ".flex.zero";
	    default:
		return ".flex."+s.decode(s.segtype).toLowerCase();
	    }
	}	    

	// okay, shoot:
	public void visit(ALIGN e) {
	    this.align = e;
	}
	public void visit(BINOP e) {
	    pw.print("(");
	    if (e.op==Bop.SHR) pw.print("SHR("); // use macro
	    if (e.op==Bop.USHR) pw.print("USHR("); // use macro
	    //if (e.type()==e.POINTER) pw.print("(void*)");
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
	    //if (e.type()==e.POINTER) pw.print("(void*)");
	    trans(e.getRight());
	    if (e.op==Bop.SHR||e.op==Bop.USHR) pw.print(")"); // close macro
	    pw.print(")");
	}
	public void visit(CALL e) {
	    boolean callv = (e.getRetval()==null);
	    if (callv) {
		pw.print("\tCALLV(");
		pw.print("((FUNCPROTOV(");
	    } else {
		pw.print("\tCALL("+ctype(e.getRetval())+", ");
		trans(e.getRetval());
		pw.print(", ((FUNCPROTO("+ctype(e.getRetval())+", ");
	    }
	    pw.print("(FIRST_PROTO_ARG(void *) ");
	    /* function type cast */
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		pw.print(ctype(el.head));
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.print("))) ");
	    /* function expression */
	    trans(e.getFunc());
	    pw.print("), (FIRST_CALL_ARG(&&"+label(e.getHandler().label)+") ");
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		trans(el.head);
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.print("), ");
	    trans(e.getRetex());
	    pw.print(", "+label(e.getHandler().label)+");");
	    pw.println();
	}
	public void visit(CJUMP e) {
	    pw.print("\tif ("); trans(e.getTest()); pw.print(")");
	    pw.print(" goto "+label(e.iftrue)+";");
	    pw.print(" else goto "+label(e.iffalse)+";");
	    pw.println();
	}
	public void visit(CONST e) {
	    pw.print(e.value==null?"NULL":e.value.toString());
	}
	public void visit(DATUM e) {
	    struct_size += sizeof(e.getData());
	    // alignment constraints mean we ought to start a new struct.
	    // so does exceeding a struct size of 32 bytes (this is a
	    // very odd gcc oddity, where too-large structs are aligned
	    // to a 32-byte boundary).  Also if we haven't entered the
	    // data mode via a label yet, we should make up a label and do so.
	    if (current_mode==NONE || struct_size >= 32 || this.align!=null) {
		startData(new Label(), false);
		struct_size += sizeof(e.getData());
	    }
	    // we ought to handle mode==CODE case, but I don't feel like
	    // thinking hard today.
	    Util.assert(current_mode==DATA);
	    pwa[DD].print("\t"+ctype(e.getData())+" ");
	    pwa[DD].print("f"+(field_counter++));
	    pwa[DD].print(" __attribute__ ((packed))");
	    if (align!=null)
		pwa[DD].print(" __attribute__ ((aligned (" +
			      this.align.alignment + ")))");
	    this.align=null; // clear alignment.
	    pwa[DD].println(";");
	    pw.print("\t"); trans(e.getData()); pw.println(",");
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
		visit(((NAME)exp), false/*don't take address*/);
	    else { pw.print("*("); trans(exp); pw.print(")"); }
	    pw.println("; /* targets: "+LabelList.toList(e.targets)+" */");
	}
	public void visit(LABEL e) {
	    if (!e.exported) local_labels.add(e.label);
	    if (current_mode==CODE) pw.println(label(e.label)+":");
	    else startData(e.label, e.exported);
	}
	private int struct_size;
	public void startData(Label l, boolean exported) {
	    switchto(DATA);
	    struct_size = 0;
	    if (!exported) pwa[DD].print("static ");
	    pwa[DD].println("struct "+label(l)+" {");
	    pwa[DI].print("} __attribute__ ((packed)) ");
	    pwa[DI].print(label(l));
	    if (this.align!=null)
		pwa[DI].print(" __attribute__ ((aligned (" +
			      this.align.alignment + ")))");
	    this.align=null; // clear alignment.
	    if (segment!=null)
		pwa[DI].print(" __attribute__ ((section (\"" +
			      sectionname(this.segment) +"\")))");
	    pwa[DI].println(" = {");
	}
	public void visit(MEM e) {
	    Exp exp = e.getExp();
	    pw.print("(*(");
	    pw.print("("+ctype(e)+"*)");
	    trans(exp);
	    pw.print("))");
	}
	private boolean isVoidMethod = false;
	public void visit(METHOD e) {
	    this.method = e;
	    // have to discard the alignment (gcc won't let us specify it)
	    this.align = null;
	    // switch!
	    switchto(CODE);
	    // create common contents of declaration and definition
	    StringWriter declw = new StringWriter();
	    pw = new PrintWriter(declw);
	    if (e.getReturnType() < 0) {
		isVoidMethod = true;
		pw.print("FUNCV(");
	    } else {
		isVoidMethod = false;
		pw.print("FUNC("+ctype(e.getReturnType())+", ");
	    }
	    pw.print(label(e.getMethod())+", (");
	    for (int i=0; i<e.getParamsLength(); i++) {
		if (i==0) pw.print("FIRST_DECL_ARG(");
		pw.print(ctype(e.getParams(i))+" ");
		temps_seen.add(e.getParams(i).temp);//suppress declaration
		trans(e.getParams(i));
		if (i==0) pw.print(") ");
		else if (i+1 < e.getParamsLength()) pw.print(", ");
	    }
	    pw.print("), \""+sectionname(this.segment)+"\")");
	    pw.close();
	    sym2decl.put(e.getMethod(), "DECLARE"+declw.getBuffer()+";");
	    // emit declaration.
	    pw = pwa[MD];
	    pw.println("DEFINE"+declw.getBuffer());
	    pw.println("{");
	    pw = pwa[MB];
	}
	public void visit(MOVE e) {
	    pw.print("\t");
	    trans(e.getDst()); pw.print(" = "); trans(e.getSrc());
	    pw.println(";");
	}
	public void visit(NAME e) { visit(e, true); }
	public void visit(NAME e, boolean take_address) {
	    /* add entry in symbol declaration table */
	    if (!sym2decl.containsKey(e.label))
		sym2decl.put(e.label, "extern struct "+label(e.label)+" "+
			     label(e.label)+";");
	    if (take_address) pw.print("(&");
	    pw.print(label(e.label));
	    if (take_address) pw.print(")");
	}
	public void visit(NATIVECALL e) {
	    pw.print("\t");
	    if (e.getRetval()!=null) {
		trans(e.getRetval()); pw.print(" = ");
	    }
	    pw.print("(");
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
	    pw.print(")(");
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		trans(el.head);
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.println(");");
	}
	public void visit(RETURN e) {
	    if (isVoidMethod)
		pw.println("\tRETURNV();");
	    else {
		pw.print("\tRETURN("+ctype(this.method.getReturnType())+",");
		trans(e.getRetval());
		pw.println(");");
	    }
	}
	public void visit(SEGMENT e) {
	    this.segment = e;
	}
	public void visit(SEQ e) {
	    trans(e.getLeft());
	    trans(e.getRight());
	}
	private Set temps_seen = new HashSet();
	public void visit(TEMP e) {
	    // declare the temp, if it hasn't already been seen.
	    if (!temps_seen.contains(e.temp)) {
		temps_seen.add(e.temp);
		pwa[MD].println("\tregister "+ctype(e)+" "+e.temp+";");
	    }
	    pw.print(e.temp);
	}
	public void visit(THROW e) {
	    if (isVoidMethod) {
		pw.print("\tTHROWV("); trans(e.getRetex()); pw.println(");");
	    } else {
		pw.print("\tTHROW("+ctype(this.method.getReturnType())+", ");
		trans(e.getRetex());
		pw.println(");");
	    }
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
