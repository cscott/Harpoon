// Print.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.LabelList;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <code>Print</code> pretty-prints Trees.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Print.java,v 1.1.2.28 1999-10-19 19:57:46 cananian Exp $
 */
public class Print {
    public final static void print(PrintWriter pw, Code c, TempMap tm) {
        Tree tr = (Tree) c.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm);

        pw.print("Codeview \""+c.getName()+"\" for "+c.getMethod()+":");
        if (tr!=null) tr.accept(pv);
        pw.println();
        pw.flush();
    }

    public final static void print(PrintWriter pw, Data d, TempMap tm) { 
	Tree tr = (Tree)d.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm);

        pw.print("Dataview \""+d.getDesc()+"\" for "+d.getHClass()+":");
        if (tr!=null) tr.accept(pv);
        pw.println();
        pw.flush();
    }

    public final static void print(PrintWriter pw, Code c) {
        print(pw, c, null);
    }
    public final static void print(PrintWriter pw, Data d) {
        print(pw, d, null);
    }

    public final static void print(PrintWriter pw, Tree t) {
	PrintVisitor pv = new PrintVisitor(pw, null);
	t.accept(pv);
	pw.println();
	pw.flush();
    }

    public final static String print(Tree t) {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	PrintVisitor pv = new PrintVisitor(pw, null);
	t.accept(pv);
	pw.flush();
	return sw.toString();
    }

    static class PrintVisitor extends TreeVisitor {
        private final static int TAB = 1;
        private PrintWriter pw;
        private TempMap tm;
        private int indlevel;

        PrintVisitor(PrintWriter pw, TempMap tm) {
            indlevel = 1;
            this.pw = pw;
            this.tm = tm;
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
	    indent(indlevel++);
	    pw.print(s.toString());
	}

        public void visit(BINOP e) {
            indent(indlevel++);
            pw.print("BINOP<" + Type.toString(e.optype) + ">(");
            pw.print(Bop.toString(e.op) + ", ");
            e.left.accept(this);
            pw.print(",");
            e.right.accept(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(CALL s) {
            ExpList list = s.args;
            indent(indlevel++);
            pw.print("CALL" + "(");
            indent(indlevel++);
            pw.print("return value:");
            s.retval.accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("exceptional value:");
            s.retex.accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("function:");
            s.func.accept(this);
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
            indlevel -= 2;
        }
            
        public void visit(CJUMP s) {
            indent(indlevel++);
            pw.print("CJUMP(");
            s.test.accept(this); pw.print(",");
            indent(indlevel);
            pw.print("if-true: " + s.iftrue + ",");
            indent(indlevel);
            pw.print("if-false: " + s.iffalse + ")");
            indlevel--;
        }

        public void visit(CONST e) {
            indent(indlevel);
            pw.print("CONST<" + Type.toString(e) + ">(" + e.value + ")");
        }

	public void visit(DATA s) { 
	    indent(indlevel++);
	    pw.print("DATA<");
	    if (s.data instanceof PreciselyTyped)
		pw.print(Type.toString((PreciselyTyped)s.data));
	    else pw.print(Type.toString(s.data.type()));
	    pw.print(">(");
	    if (!s.initialized) pw.print("unspecified value");
	    else s.data.accept(this); 
	    indent(indlevel--);
	    pw.print(")");
	}

        public void visit(ESEQ e) {
            indent(--indlevel);
            indlevel++;
            pw.print("ESEQ(");
            e.stm.accept(this);
            pw.print(",");
            e.exp.accept(this);
            pw.print(")");
        }

        public void visit(EXP s) {
            indent(indlevel++);
            pw.print("EXP(");
            s.exp.accept(this);
            pw.print(")");
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
            s.exp.accept(this);
            pw.print(")");
            indlevel--;
        }
            

        public void visit(LABEL s) {
            indent(indlevel);
            pw.print("LABEL(" + s.label + ")");
        }

        public void visit(MEM e) {
            indent(indlevel++);
            pw.print("MEM<" + Type.toString(e) + ">(");
            e.exp.accept(this);
            pw.print(")");
            indlevel--;
        }

	public void visit(METHOD s) { 
	    indent(indlevel++);
	    pw.print("METHOD(");
	    for (int i=0; i<s.params.length; i++)
		s.params[i].accept(this);
	    pw.print(")");
	    indlevel--;
	}

        public void visit(MOVE s) {
            indent(indlevel++);
            pw.print("MOVE(");
            s.dst.accept(this);
            pw.print(",");
            s.src.accept(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(NAME e) {
            indent(indlevel);
            pw.print("NAME(" + e.label + ")");
        }

        public void visit(NATIVECALL s) {
            ExpList list = s.args;
            indent(indlevel++);
            pw.print("NATIVECALL" + "(");
            indent(indlevel++);
            pw.print("return value:");
            s.retval.accept(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("function:");
            s.func.accept(this);
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
            indlevel -= 2;
        }

        public void visit(RETURN s) {
            indent(indlevel++);
            pw.print("RETURN(");
            s.retval.accept(this);
            pw.print(")");
            indlevel--;
        }

	public void visit(SEGMENT s) { 
	    indent(indlevel++);
	    pw.print(s.toString());
	}

        public void visit(SEQ s) {
            indent(--indlevel);
            indlevel++;
            pw.print("SEQ(");
            s.left.accept(this);
            pw.print(",");
            s.right.accept(this);
            pw.print(")");
        }

        public void visit(TEMP e) {
            Temp t = (tm == null) ? e.temp : tm.tempMap(e.temp);
            indent(indlevel);
            pw.print("TEMP<" + Type.toString(e.type) + ">(" + t + ")");
        }

        public void visit(THROW s) {
            indent(indlevel++);
            pw.print("THROW(");
            s.retex.accept(this);
	    pw.print(",");
	    s.handler.accept(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(UNOP e) {
            indent(indlevel++);
            pw.print("UNOP<" + Type.toString(e.optype) + ">(");
            pw.print(Uop.toString(e.op) + ",");
            e.operand.accept(this);
            pw.print(")");
            indlevel--;
        }
    } 
}
