// Print.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.LabelList;

import harpoon.Util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * <code>Print</code> pretty-prints Trees.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Print.java,v 1.1.2.38 2000-11-16 22:40:38 witchel Exp $
 */
public class Print {
    public final static void print(PrintWriter pw, Code c, TempMap tm,
                                   HashMap ht) {
        Tree tr = (Tree) c.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm, ht);

        pw.print("Codeview \""+c.getName()+"\" for "+c.getMethod()+":");
	for (StmList slp=linearize((Stm)tr, null); slp!=null; slp=slp.tail)
	    slp.head.accept(pv);
        pw.println();
        pw.flush();
    }

    public final static void print(PrintWriter pw, Data d, TempMap tm) { 
	Tree tr = (Tree)d.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm, null);

        pw.print("Dataview \""+d.getDesc()+"\" for "+d.getHClass()+":");
	for (StmList slp=linearize((Stm)tr, null); slp!=null; slp=slp.tail)
	    slp.head.accept(pv);
        pw.println();
        pw.flush();
    }

    public final static void print(PrintWriter pw, Code c) {
        print(pw, c, null);
    }
    public final static void print(PrintWriter pw, Data d) {
        print(pw, d, null);
    }

    public final static void print(PrintWriter pw, Code c, HashMap ht) {
        print(pw, c, null, ht);
    }

    public final static void print(PrintWriter pw, Tree t) {
	PrintVisitor pv = new PrintVisitor(pw, null, null);
	t.accept(pv);
	pw.println();
	pw.flush();
    }

    public final static String print(Tree t) {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	PrintVisitor pv = new PrintVisitor(pw, null, null);
	if (t!=null) {
	    t.accept(pv);
	} else {
	    pw.print("null");
	}
	    
	pw.flush();
	return sw.toString();
    }

    private static StmList linearize(Stm s, StmList tail) {
	if (s==null) return tail; // deal gracefully.
	if (s instanceof SEQ)
	    return linearize(((SEQ)s).getLeft(),
			     linearize(((SEQ)s).getRight(), tail));
	return new StmList(s, tail);
    }

    static class PrintVisitor extends TreeVisitor {
        private final static int TAB = 1;
        private PrintWriter pw;
        private TempMap tm;
        private HashMap ht;
        private int indlevel;

        PrintVisitor(PrintWriter pw, TempMap tm, HashMap ht) {
            indlevel = 1;
            this.pw = pw;
            this.tm = tm;
            if(ht == null)
               this.ht = new HashMap();
            else 
               this.ht = ht;

	    // Util.assert(false, "Printing Trees is *slow*");
        }

        private void indent(int dist) {
            pw.println();
            for (int i=0; i < TAB * dist; i++)
                pw.print(' ');
        }

	public void visit(Tree e) {
	    throw new Error("Can't print abstract class!");
	}

	public void visit(ALIGN s) { 
	    indent(indlevel);
	    pw.print(s.toString());
        if(ht.containsKey(s)) {
           pw.print(ht.get(s).toString());
        }
	}

        public void visit(BINOP e) {
            indent(indlevel++);
            pw.print("BINOP<" + Type.toString(e.optype) + ">(");
            pw.print(Bop.toString(e.op) + ", ");
            e.getLeft().accept(this);
            pw.print(",");
            e.getRight().accept(this);
            pw.print(")");
            pw.print(e.hashCode());
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
            indlevel--;
        }

        public void visit(CALL s) {
            ExpList list = s.getArgs();
            indent(indlevel++);
            pw.print("CALL" + (s.isTailCall?" [tail call] (" : "("));
            indent(indlevel++);
	    if (s.getRetval()!=null) {
		pw.print("return value:");
		s.getRetval().accept(this);
		pw.print(",");
		indent(--indlevel); indlevel++;
	    }
            pw.print("exceptional value:");
            s.getRetex().accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("function:");
            s.getFunc().accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("arguments:");
            while (list != null) {
                list.head.accept(this);
                if (list.tail != null) {
                    pw.print(",");
                }
                list = list.tail;
            }
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("handler:");
            s.getHandler().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel -= 2;
        }
            
        public void visit(CJUMP s) {
            indent(indlevel++);
            pw.print("CJUMP(");
            s.getTest().accept(this); pw.print(",");
            indent(indlevel);
            pw.print("if-true: " + s.iftrue + ",");
            indent(indlevel);
            pw.print("if-false: " + s.iffalse + ")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

        public void visit(CONST e) {
            indent(indlevel);
            pw.print("CONST<" + Type.toString(e) + ">(" + e.value + ")");
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
        }

	public void visit(DATUM s) { 
	    indent(indlevel++);
	    pw.print("DATUM<");
	    if (s.getData() instanceof PreciselyTyped)
		pw.print(Type.toString((PreciselyTyped)s.getData()));
	    else pw.print(Type.toString(s.getData().type()));
	    pw.print(">(");
	    if (!s.initialized) pw.print("unspecified value");
	    else s.getData().accept(this); 
	    indent(indlevel--);
	    pw.print(")");
        if(ht.containsKey(s)) {
           pw.print(ht.get(s).toString());
        }
	}

        public void visit(ESEQ e) {
            indent(indlevel++);
            pw.print("ESEQ(");
            e.getStm().accept(this);
            pw.print(",");
            e.getExp().accept(this);
            pw.print(")");
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
            indlevel--;
        }

        public void visit(EXPR s) {
            indent(indlevel++);
            pw.print("EXPR(");
            s.getExp().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

        public void visit(JUMP s) {
            LabelList list = s.targets;
            indent(indlevel++);
            pw.print("JUMP(");
            indent(indlevel);
            pw.print("targets:");
            while (list != null) {
                pw.print(" " + list.head);
                if (list.tail != null) 
                    pw.print(",");
                list = list.tail;
            }
            s.getExp().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }
            

        public void visit(LABEL s) {
            indent(indlevel);
            pw.print("LABEL(" + s.label + ")");
            pw.print(s.hashCode());
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
        }

        public void visit(MEM e) {
            indent(indlevel++);
            pw.print("MEM<" + Type.toString(e) + ">(");
            e.getExp().accept(this);
            pw.print(")");
            pw.print(e.hashCode());
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
            indlevel--;
        }

	public void visit(METHOD s) { 
	    indent(indlevel++);
	    pw.print("METHOD(");
	    TEMP[] params = s.getParams();
	    for (int i=0; i<params.length; i++)
		params[i].accept(this);
	    pw.print(")");
        if(ht.containsKey(s)) {
           pw.print(ht.get(s).toString());
        }
	    indlevel--;
	}

        public void visit(MOVE s) {
            indent(indlevel++);
            pw.print("MOVE(");
            s.getDst().accept(this);
            pw.print(",");
            s.getSrc().accept(this);
            pw.print(")");
            pw.print(s.hashCode());
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

        public void visit(NAME e) {
            indent(indlevel);
            pw.print("NAME(" + e.label + ")");
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
        }

        public void visit(NATIVECALL s) {
            ExpList list = s.getArgs();
            indent(indlevel++);
            pw.print("NATIVECALL" + "(");
            indent(indlevel++);
	    if (s.getRetval()!=null) {
		pw.print("return value:");
		s.getRetval().accept(this);
		pw.print(",");
		indent(--indlevel); indlevel++;
	    }
            pw.print("function:");
            s.getFunc().accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("arguments:");
            while (list != null) {
                list.head.accept(this);
                if (list.tail != null) {
                    pw.print(",");
                }
                list = list.tail;
            }
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel -= 2;
        }

        public void visit(RETURN s) {
            indent(indlevel++);
            pw.print("RETURN(");
            s.getRetval().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

	public void visit(SEGMENT s) { 
	    indent(indlevel);
	    pw.print(s.toString());
        if(ht.containsKey(s)) {
           pw.print(ht.get(s).toString());
        }
	}

        public void visit(SEQ s) {
            indent(indlevel++);
            pw.print("SEQ(");
            s.getLeft().accept(this);
            pw.print(",");
            s.getRight().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

        public void visit(TEMP e) {
            Temp t = (tm == null) ? e.temp : tm.tempMap(e.temp);
            indent(indlevel);
            pw.print("TEMP<" + Type.toString(e.type) + ">(" + t + ")");
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
        }

        public void visit(THROW s) {
            indent(indlevel++);
            pw.print("THROW(");
            s.getRetex().accept(this);
	    pw.print(",");
	    s.getHandler().accept(this);
            pw.print(")");
            if(ht.containsKey(s)) {
               pw.print(ht.get(s).toString());
            }
            indlevel--;
        }

        public void visit(UNOP e) {
            indent(indlevel++);
            pw.print("UNOP<" + Type.toString(e.optype) + ">(");
            pw.print(Uop.toString(e.op) + ",");
            e.getOperand().accept(this);
            pw.print(")");
            if(ht.containsKey(e)) {
               pw.print(ht.get(e).toString());
            }
            indlevel--;
        }
    } 
}
