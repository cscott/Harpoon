// TreeToC.java, created Thu Jun 22 14:23:38 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.Liveness;
import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HData;
import harpoon.IR.Properties.UseDefer;
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
import harpoon.IR.Tree.INVOCATION;
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
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Util.ReverseIterator;
import harpoon.Util.Util;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
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
 * @version $Id: TreeToC.java,v 1.7 2002-06-26 01:09:41 cananian Exp $
 */
public class TreeToC extends java.io.PrintWriter {
    private TranslationVisitor tv;
    
    /** Creates a <code>TreeToC</code>. */
    public TreeToC(Writer out) {
	super(out);
	this.tv = new TranslationVisitor();
    }
    // XXX: The TempVisitor approach fails when a single temp may be
    // assigned values of more than one type.  Be careful never to
    // generate tree form where this happens.  (Else, we could use
    // a reaching-def analysis to associate the "proper" reaching
    // type with every temp, but that's too much work for me at the
    // moment.  Remember: laziness is one of a good programmer's
    // cardinal virtues. --CSA 9-jul-2001
    public void translate(HCode hc) {
	harpoon.IR.Tree.Code c = (harpoon.IR.Tree.Code) hc;
	Tree root = (Tree)c.getRootElement();
	tv.ud = c.getUseDefer();
	tv.live = new harpoon.Analysis.DataFlow.LiveVars
	    (c, c.getGrapher(), tv.ud, Collections.EMPTY_SET);
	tv.tempv = new TempVisitor(root, c.getTreeDerivation());
	tv.inh = new IdentifyNoHandler(c);
	translate(root);
	tv.ud = null;
	tv.live = null;
	tv.tempv = null;
	tv.inh = null;
    }
    public void translate(HData hd) { translate((Tree)hd.getRootElement()); }
    private void translate(Tree t) {
	tv.switchto(tv.NONE);
	if (t!=null) { tv.lv=new LabelVisitor(t); tv.trans(t); }
    }
    public void close() {
	tv.switchto(tv.NONE);
	this.println("#include <precisec.h>");
	// collect symbol declarations.
	tv.emitSymbols(this);
	// now all the code & data
	tv.emitOutput(this);
	// okay, now (really) flush and close.
	super.close();
    }
    /** TempVisitor collects the types of temps */
    private static class TempVisitor extends TreeVisitor {
	public final Map objectTemps = new HashMap();
	private final TreeDerivation treederiv;
	public TempVisitor(Tree t, TreeDerivation treederiv) {
	    this.treederiv = treederiv;
	    t.accept(this);
	}
	public void visit(Tree t) {
	    for (Tree st=t.getFirstChild(); st!=null; st=st.getSibling())
		st.accept(this);
	}
	public void visit(TEMP t) {
	    HClass hc = treederiv.typeMap(t);
	    if (hc==null || !hc.isPrimitive())
		objectTemps.put(t.temp, treederiv.derivation(t));
	}
    }
    /** LabelVisitor identifies the method-local labels. */
    private static class LabelVisitor extends TreeVisitor {
	public final Set method_labels = new HashSet();
	public final Set local_code_labels = new HashSet();
	public final Set local_table_labels = new HashSet();
	private boolean seenMethod = false;
	LabelList last_labels=null;
	public LabelVisitor(Tree t) { t.accept(this); }
	public void visit(Tree e) {
	    if (last_labels!=null) {
		if (seenMethod)
		    local_code_labels.addAll(LabelList.toList(last_labels));
		last_labels=null;
	    }
	}
	public void visit(SEQ e) {
	    e.getLeft().accept(this);
	    e.getRight().accept(this);
	}
	public void visit(METHOD e) {
	    seenMethod = true;
	    method_labels.addAll(LabelList.toList(last_labels));
	    last_labels=null;
	}
	public void visit(LABEL e) {
	    last_labels=new LabelList(e.label, last_labels);
	}
	public void visit(DATUM e) {
	    if (last_labels!=null) {
		if (seenMethod)
		    local_table_labels.addAll(LabelList.toList(last_labels));
		last_labels=null;
	    }
	}
    }
    /** TranslationVisitor does the real work. */
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
	// support liveness info for *precise* gc.
	Liveness live = null;
	UseDefer ud = null;
	TempVisitor tempv = null;
	// support no-handler optimization
	IdentifyNoHandler inh = null;

	// keep track of current alignment, section, method, etc.
	ALIGN align = null;
	SEGMENT segment = null;
	METHOD method = null;
	LABEL label = null;
	int field_counter = 0;

	// switch from one mode to the next.
	static final int NONE = 0;
	static final int DATA = 1;
	static final int CODE = 2;
	static final int CODETABLE = 3;
	int current_mode = NONE;
	void switchto(int mode) {
	    switch(current_mode) {
	    case CODE:
		if (mode==CODETABLE) break; // postpone flush.
		flushAndAppend(MD);
		pwa[MB].println("}");
		flushAndAppend(MB);
		temps_seen.clear();
		break;
	    case CODETABLE: // flush to CODE.
		assert mode==CODE;
		pwa[DI].println("};");
		flushAndAppendTo(DD, MD);
		flushAndAppendTo(DI, MD);
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
		pw = pwa[MB];
		break;
	    case CODETABLE:
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
	void flushAndAppendTo(int from, int to) {
	    pwa[from].flush();
	    StringBuffer sb = swa[from].getBuffer();
	    pwa[to].print(sb);
	    sb.setLength(0);
	}

	/** these are the symbols referenced (and declarations for them) */
	Map sym2decl = new HashMap();
	{ /* gcc complains if we don't declare certain symbols "properly". */
	    sym2decl.put(new Label("memset"),
			 "extern void *memset(void *,int,size_t);");
	    sym2decl.put(new Label("alloca"),
			 "extern void *alloca(size_t);");
	  /* we use puts in translating the DEBUG quad */
	    sym2decl.put(new Label("puts"),
			 "int puts(const char *s);");
	  /* also, we need to use FNI_GetJNIEnv() at points. */
	    sym2decl.put(new Label("FNI_GetJNIEnv"),
			 "extern JNIEnv *FNI_GetJNIEnv(void);");
	    sym2decl.put(new Label("generational_write_barrier"),
			 "extern void generational_write_barrier"+
			 "(jobject_unwrapped *);");
	}
	/** functions we have exact prototypes for */
	static Set exactproto = new HashSet();
	static { exactproto.add("memset"); exactproto.add("alloca");
	         exactproto.add("generational_write_barrier"); }
	
	/** these are the *local* labels which are defined *inside functions*
	 *  in this file. */
	LabelVisitor lv=null;

	void emitSymbols(PrintWriter pw) {
	    for (Iterator it=sym2decl.values().iterator(); it.hasNext(); )
		pw.println(it.next());
	}
	void emitOutput(PrintWriter pw) {
	    pw.print(output);
	}
	// make a string "safe". This is made necessary by obfuscators.
	private static String safe(String s) {
	    char[] ca = s.toCharArray();
	    for (int i=0; i<ca.length; i++)
		if (Character.isISOControl(ca[i])) ca[i]='#';
	    return new String(ca);
	}

	// useful line number update function.
	private boolean EMIT_LINE_DIRECTIVES=
	    Boolean.getBoolean("harpoon.precisec.emit_line_directives");
	// the line "directives" will be just comments containing the
	// line numbers
	private boolean EMIT_FAKE_LINE_DIRECTIVES=
	    Boolean.getBoolean("harpoon.precisec.emit_fake_line_directives");
	private int suppress_directives=0;
	private String last_file = null;
	private int last_line = 0;
	private void updateLine(Tree e) {
	    if (!EMIT_LINE_DIRECTIVES || suppress_directives>0) return;
	    if (e instanceof SEQ) return;
	    if (pw==null) return;
	    String curr_file = safe(e.getSourceFile());
	    int curr_line = e.getLineNumber();
	    boolean files_match =
		last_file==null ? curr_file==null :last_file.equals(curr_file);
	    if (last_line == curr_line && files_match) return;
	    pw.println();
	    boolean line_needed = !(last_line == curr_line-1 && files_match);
	    last_file = curr_file;
	    last_line = curr_line;
	    if (line_needed) emitLineDirective(!files_match);
	}
	private void emitLineDirective(boolean emitFile) {
	    if (!EMIT_LINE_DIRECTIVES || suppress_directives>0) return;
	    if (last_file==null && last_line==0) return;
	    pw.println();

	    if(EMIT_FAKE_LINE_DIRECTIVES) // just comments - useful for debug
		pw.print("/* line " + last_line);
	    else
		pw.print("#line " + last_line);

	    if (emitFile) pw.print(" \""+last_file+"\"");
	    if(EMIT_FAKE_LINE_DIRECTIVES)
		pw.print(" */");
	    pw.println();
	}
	private void nl() {
	    if ((!EMIT_LINE_DIRECTIVES) ||
		(last_file==null && last_line==0))
		pw.println();
	    else
		pw.print(" ");
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
	private String sectionname(SEGMENT s) {
	    switch (s.segtype) {
	    case SEGMENT.TEXT: return ".text";
	    case SEGMENT.ZERO_DATA: return ".flex.zero";
	    case SEGMENT.CODE: // gcc is picky about data in code segment.
		if (current_mode==CODETABLE) return ".flex.code.rw";
		// else fall through:
	    default:
		return ".flex."+s.decode(s.segtype).toLowerCase();
	    }
	}	    

	// okay, shoot:
	public void visit(ALIGN e) {
	    this.align = e;
	}
	public void visit(BINOP e) {
	    boolean macro=false, funccall=false, andwithptrargs=false;
	    pw.print("(");
		
	    if (e.op==Bop.SHR || e.op==Bop.USHR) {
		macro=funccall=true;
		if (e.type()==Type.LONG)
		    pw.print("L"); // macro prefix for long ops.
		if (e.op==Bop.SHR) pw.print("SHR("); // use macro
		if (e.op==Bop.USHR) pw.print("USHR("); // use macro
	    }
	    if (e.op==Bop.REM && e.type()==Type.FLOAT) {
		funccall=true;
		pw.print("fmodf("); // double remainder == c-lib frem()
		sym2decl.put(new Label("fmodf"),"float fmodf(float, float);");
	    }
	    if (e.op==Bop.REM && e.type()==Type.DOUBLE) {
		funccall=true;
		pw.print("fmod("); // double remainder == c-lib frem()
		sym2decl.put(new Label("fmod"),"double fmod(double, double);");
	    }
	    if (e.op==Bop.AND && e.type()==e.POINTER) {
		andwithptrargs=true;
		pw.print("(void *)(");
	    }
	    if (macro) suppress_directives++;
	    if (andwithptrargs) pw.print("(ptroff_t)");
	    trans(e.getLeft());
	    if (funccall) pw.print(", ");
	    else switch(e.op) {
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
	    case Bop.AND: pw.print("&"); break;
	    case Bop.OR: pw.print("|"); break;
	    case Bop.XOR: pw.print("^"); break;
	    default: throw new Error("unknown Bop: "+e);
	    }
	    if (andwithptrargs) pw.print("(ptroff_t)");
	    trans(e.getRight());
	    if (funccall||andwithptrargs) pw.print(")"); // close macro/call
	    if (macro) suppress_directives--;
	    pw.print(")");
	}
	public void visit(CALL e) {
	    Set liveo = liveObjects(e);
	    pw.print("\t"); emitPush(liveo); pw.print(";"); nl();
	    suppress_directives++;
	    String nh = inh.requiresHandler(e) ? "" : "_NH";
	    boolean callv = (e.getRetval()==null);
	    if (callv) {
		pw.print("\tCALLV"+nh+"(");
		pw.print("((FUNCPROTOV(");
	    } else {
		pw.print("\tCALL"+nh+"("+ctype(e.getRetval())+", ");
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
	    pw.print(", "+label(e.getHandler().label));
	    pw.print(", "); emitPop(liveo); pw.print(");");
	    suppress_directives--;
	    nl();
	}
	public void visit(CJUMP e) {
	    pw.print("\tif ("); trans(e.getTest()); pw.print(")");
	    pw.print(" goto "+label(e.iftrue)+";");
	    pw.print(" else goto "+label(e.iffalse)+";");
	    nl();
	}
	public void visit(CONST e) {
	    if (e.value()==null) { pw.print("NULL"); return; }
	    String val = e.value().toString();
	    if (e.type()==Type.DOUBLE) {
		Double d = (Double) e.value();
		if (d.isInfinite()) {
		    pw.print("("+(d.doubleValue()<0?"-":"")+"1.0/0.0)");
		    return;
		}
		if (d.isNaN()) { pw.print("(0.0/0.0)"); return; }
	    }
	    if (e.type()==Type.FLOAT) {
		Float f = (Float) e.value();
		if (f.isInfinite()) {
		    pw.print("("+(f.floatValue()<0?"-":"")+"1.0f/0.0f)");
		    return;
		}
		if (f.isNaN()) { pw.print("(0.0f/0.0f)"); return; }
	    }
	    if (e.type()==Type.INT && e.value().intValue()==Integer.MIN_VALUE)
		val="0x"+Integer.toHexString(e.value().intValue());
	    if (e.type()==Type.LONG && e.value().longValue()==Long.MIN_VALUE)
		val="0x"+Long.toHexString(e.value().longValue());
	    // XXX: assumption that jfloat==float and jlong==long long
	    // this assumption may be erroneous! =( but no way to
	    // 'portably' define constants.
	    if (e.type()==Type.FLOAT) val+="F";
	    if (e.type()==Type.LONG) val+="LL";
	    pw.print(val);
	}
	public void visit(DATUM e) {
	    if (current_mode==CODE)
		startData(CODETABLE, last_label, false);
	    struct_size += sizeof(e.getData());
	    // alignment constraints mean we ought to start a new struct.
	    // so does exceeding a struct size of 32 bytes (this is a
	    // very odd gcc oddity, where too-large structs are aligned
	    // to a 32-byte boundary).  Also if we haven't entered the
	    // data mode via a label yet, we should make up a label and do so.
	    if (current_mode != CODETABLE /* no restrictions here */ &&
		(current_mode==NONE || struct_size >= 32 || this.align!=null)){
		startData(current_mode==NONE ? DATA : current_mode,
			  new Label(), false);
		struct_size += sizeof(e.getData());
	    }
	    pwa[DD].print("\t"+ctype(e.getData())+" ");
	    pwa[DD].print("f"+(field_counter++));
	    pwa[DD].print(" __attribute__ ((packed))");
	    if (align!=null)
		pwa[DD].print(" __attribute__ ((aligned (" +
			      this.align.alignment + ")))");
	    this.align=null; // clear alignment.
	    pwa[DD].println(";");
	    pw.print("\t"); trans(e.getData()); pw.print(","); nl();
	}
	public void visit(ESEQ e) {
	    throw new Error("Non-canonical tree form.");
	}
	public void visit(EXPR e) {
	    pw.print("\t"); trans(e.getExp()); pw.print(";"); nl();
	}
	public void visit(JUMP e) {
	    pw.print("\tgoto ");
	    Exp exp = e.getExp();
	    if (exp instanceof NAME)
		visit(((NAME)exp), false/*don't take address*/);
	    else { pw.print("*("); trans(exp); pw.print(")"); }
	    pw.print("; /* targets: "+LabelList.toList(e.targets)+" */");
	    nl();
	}
	Label last_label;
	public void visit(LABEL e) {
	    // if we're outputting an inline table, switch back to regular
	    // code mode.
	    if (current_mode==CODETABLE) switchto(CODE);
	    if (current_mode==CODE) {
		if (lv.local_code_labels.contains(e.label)) {
		    pw.print(label(e.label)+":"); nl();
		} else assert lv.local_table_labels.contains(e.label);
	    } else if (!lv.method_labels.contains(e.label))
		startData(DATA, e.label, e.exported);
	    last_label=e.label;
	}
	private int struct_size;
	public void startData(int mode, Label l, boolean exported) {
	    switchto(mode);
	    if (!lv.local_table_labels.contains(l))
		sym2decl.put(l, (exported?"extern ":"static ") +
			     "struct "+label(l)+" "+label(l)+";");
	    struct_size = 0;
	    if (!exported) pwa[DD].print("static ");
	    pwa[DD].println("struct "+label(l)+" {");
	    pwa[DI].print("} __attribute__ ((packed)) ");
	    pwa[DI].print(label(l));
	    if (this.align!=null)
		pwa[DI].print(" __attribute__ ((aligned (" +
			      this.align.alignment + ")))");
	    this.align=null; // clear alignment.
	    // older versions of gcc won't allow segment attrs on local vars:
	    if (segment!=null && mode!=CODETABLE)
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
	    StringWriter protow = new StringWriter();
	    pw = new PrintWriter(protow);
	    String prologue;
	    if (e.getReturnType() < 0) {
		isVoidMethod = true;
		prologue="FUNCV(";
	    } else {
		isVoidMethod = false;
		prologue="FUNC("+ctype(e.getReturnType())+", ";
	    }
	    // there's a space for the method name here.
	    pw.print(", (");
	    for (int i=0; i<e.getParamsLength(); i++) {
		if (i==0) pw.print("FIRST_DECL_ARG(");
		pw.print(ctype(e.getParams(i))+" ");
		temps_seen.add(e.getParams(i).temp);//suppress declaration
		e.getParams(i).accept(this);
		if (i==0) pw.print(") ");
		else if (i+1 < e.getParamsLength()) pw.print(", ");
	    }
	    pw.print("), \""+sectionname(this.segment)+"\",");
	    pw.close();
	    String declstr=prologue+label(e.getMethod())+protow.getBuffer();
	    // declare this method.
	    sym2decl.put(e.getMethod(), "DECLARE"+declstr+");");
	    // declare its aliases.
	    String aliasdeclstr=protow.getBuffer()+
		" __attribute__ ((alias (\""+label(e.getMethod())+"\"))));";
	    for (Iterator it=lv.method_labels.iterator(); it.hasNext(); ) {
		Label alias = (Label) it.next();
		sym2decl.put(alias,
			     "DECLARE"+prologue+label(alias)+aliasdeclstr);
	    }
	    // emit definition.
	    pw = pwa[MD];
	    last_file=null; last_line=0; updateLine(e);
	    pw.print("DEFINE"+declstr+")"); nl();
	    pw.println("{");
	    pw = pwa[MB];
	    emitLineDirective(false);
	}
	public void visit(MOVE e) {
	    pw.print("\t");
	    trans(e.getDst()); pw.print(" = "); trans(e.getSrc());
	    pw.print(";"); nl();
	}
	public void visit(NAME e) { visit(e, true); }
	public void visit(NAME e, boolean take_address) {
	    /* add entry in symbol declaration table */
	    if (!lv.local_code_labels.contains(e.label) &&
		!lv.local_table_labels.contains(e.label) &&
		!sym2decl.containsKey(e.label))
		sym2decl.put(e.label, "extern struct "+label(e.label)+" "+
			     label(e.label)+";");
	    if (take_address) {
		pw.print("(");
		pw.print("(void*)");
		pw.print("&");
		if (lv.local_code_labels.contains(e.label)) pw.print("&");
	    }
	    pw.print(label(e.label));
	    if (take_address) pw.print(")");
	}
	public void visit(NATIVECALL e) {
	    // extract the name of the function to call, if known exactly.
	    String funcname = e.getFunc() instanceof NAME ?
		((NAME)e.getFunc()).label.name : null;
	    // this is a hack: calls to localref thunk functions aren't
	    // counted as gc points so that we don't screw up the localrefs
	    // stack in the middle of setting it up.
	    boolean nopush = "FNI_NewLocalRef".equals(funcname) ||
		"FNI_DeleteLocalRef".equals(funcname) ||
		"FNI_DeleteLocalRefsUpTo".equals(funcname) ||
		"generational_write_barrier".equals(funcname);

	    Set liveo = liveObjects(e);
	    if (!nopush) { // "special" functions known not to be gc-points.
		pw.print("\t"); emitPush(liveo); pw.print(";"); nl();
	    }
	    pw.print("\t");
	    if (e.getRetval()!=null) {
		trans(e.getRetval()); pw.print(" = ");
	    }
	    pw.print("(");
	    // hack to allow inlining calls to memset/etc.
	    if (e.getFunc() instanceof NAME &&
		exactproto.contains(((NAME)e.getFunc()).label.name))
		pw.print(((NAME)e.getFunc()).label.name);
	    else {
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
	    }
	    pw.print(")(");
	    for (ExpList el=e.getArgs(); el!=null; el=el.tail) {
		trans(el.head);
		if (el.tail!=null) pw.print(", ");
	    }
	    pw.print(");"); nl();
	    if (!nopush) { // "special" functions known not to be gc-points.
		pw.print("\t"); emitPop(liveo); pw.print(";"); nl();
	    }
	}
	public void visit(RETURN e) {
	    if (isVoidMethod)
		pw.print("\tRETURNV();");
	    else {
		suppress_directives++;
		pw.print("\tRETURN("+ctype(this.method.getReturnType())+",");
		trans(e.getRetval());
		pw.print(");");
		suppress_directives--;
	    }
	    nl();
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
	    suppress_directives++;
	    if (isVoidMethod) {
		pw.print("\tTHROWV("); trans(e.getRetex()); pw.print(");");
	    } else {
		pw.print("\tTHROW("+ctype(this.method.getReturnType())+", ");
		trans(e.getRetex());
		pw.print(");");
	    }
	    suppress_directives--;
	    nl();
	}
	public void visit(UNOP e) {
	    pw.print("(");
	    switch(e.op) {
	    case Uop.NEG: pw.print("- "); break;//space after to prevent --
	    case Uop.NOT: pw.print("~"); break;
	    case Uop.I2B: pw.print("(jbyte)"); break;
	    case Uop.I2C: pw.print("(jchar)"); break;
	    case Uop.I2S: pw.print("(jshort)"); break;
	    case Uop._2I: pw.print("(jint)"); break;
	    case Uop._2L: pw.print("(jlong)"); break;
	    case Uop._2F: pw.print("(jfloat)"); break;
	    case Uop._2D: pw.print("(jdouble)"); break;
	    default: throw new Error("unknown Uop: "+e);
	    }
	    trans(e.getOperand()); pw.print(")");
	}

	Map temp2K = new HashMap();
	// emit an expression that saves the derivation of derived pointers.
	private void emitHandleDerived(Set liveo, boolean isSave) {
	    for (Iterator it=liveo.iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		Derivation.DList dl=(Derivation.DList)tempv.objectTemps.get(t);
		if (dl==null) continue; // not a derived temp.
		// fetch name of temp in which to store derivation.
		Temp K = (Temp) temp2K.get(t);
		if (K==null) { // ooh, ooh, make new!
		    K = new Temp(t);
		    temp2K.put(t, K);
		    // declare the temp.
		    pwa[MD].println("\tregister jsize "+K+
				    " __attribute__ ((unused));");
		}
		if (isSave) pw.print(K+" = "+t); else pw.print(t+" = "+K);
		while (dl!=null) {
		    pw.print((dl.sign^isSave)?"+":"-");
		    pw.print("PTRMASK("+dl.base+")");
		    dl=dl.next;
		}
		pw.print(",");
	    }
	    pw.print("0");
	}
	// emit an expression to push object pointers and save derived types.
	private void emitPush(Set liveo) {
	    pw.print("IFPRECISE(/*push*/(");
	    // push base pointers
	    for (Iterator it=liveo.iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		Derivation.DList dl=(Derivation.DList)tempv.objectTemps.get(t);
		if (dl!=null) continue; // only base pointers!
		pw.print("PUSHOBJ("+t+"),");
	    }
	    // handle derived pointers.
	    emitHandleDerived(liveo, true/* save */);
	    pw.print("))");
	}
	// emit an expression to pop object pointers and restore derived types
	private void emitPop(Set liveo) {
	    pw.print("IFPRECISE(/*pop */(");
	    // pop base pointers
	    for (Iterator it=new ReverseIterator(liveo.iterator());
		 it.hasNext(); ) {
		Temp t = (Temp) it.next();
		Derivation.DList dl =
		    (Derivation.DList) tempv.objectTemps.get(t);
		if (dl!=null) continue; // only base pointers!
		pw.print(t+"=POPOBJ(),");
	    }
	    // handle derived pointers.
	    emitHandleDerived(liveo, false/* restore */);
	    pw.print("))");
	}
	// return the set of objects to be saved/restored at a given alloc site
	private Set liveObjects(INVOCATION e) {
	    if (live==null || tempv==null || ud==null)
		return Collections.EMPTY_SET; // bail out if no liveness info.
	    // SAVE: (liveOut(e)-e.defs()) intersected w/ objectTemps
	    Set lo = new HashSet();
	    for (Iterator it=live.getLiveOut(e).iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		// filter out non-object temps.
		if (!tempv.objectTemps.containsKey(t)) continue;
		lo.add(t);
		// derived temps are also uses of their bases.
		Derivation.DList dl=(Derivation.DList)tempv.objectTemps.get(t);
		while (dl!=null) {
		    lo.add(dl.base);
		    dl=dl.next;
		}
	    }
	    // filter out temps def'ed by INVOCATION
	    lo.removeAll(ud.defC(e));
	    // done.
	    return lo;
	}
    }
}
