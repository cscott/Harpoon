// Print.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.LabelList;
import java.io.PrintWriter;

/**
 * <code>Print</code> pretty-prints Trees.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Print.java,v 1.1.2.14 1999-08-03 21:12:57 duncan Exp $
 */
public class Print {
    final static void print(PrintWriter pw, Code c, TempMap tm) {
        Tree tr = (Tree) c.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm);

        pw.print("Codeview \""+c.getName()+"\" for "+c.getMethod()+":");
        tr.visit(pv);
        pw.println();
        pw.flush();
    }

    final static void print(PrintWriter pw, Data d, TempMap tm) { 
	Tree tr = (Tree)d.getRootElement();
        PrintVisitor pv = new PrintVisitor(pw, tm);

        pw.print("Dataview \""+d.getName()+"\" for "+d.getHClass()+":");
        tr.visit(pv);
        pw.println();
        pw.flush();
    }

    final static void print(PrintWriter pw, Code c) {
        print(pw, c, null);
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

        public void visit(BINOP e) {
            indent(indlevel++);
            pw.print("BINOP<" + Type.toString(e.optype) + ">(");
            pw.print(Bop.toString(e.op) + ", ");
            e.left.visit(this);
            pw.print(",");
            e.right.visit(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(CALL s) {
            printInvocation("CALL", s);
        }

        private void printInvocation(String name, INVOCATION s) {
            ExpList list = s.args;
            indent(indlevel++);
            pw.print(name + "(");
            indent(indlevel++);
            pw.print("return value:");
            s.retval.visit(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("exceptional value:");
            s.retex.visit(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("function:");
            s.func.visit(this);
            pw.print(",");
            indent(--indlevel); indlevel++;
            pw.print("arguments:");
            while (list != null) {
                list.head.visit(this);
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
            s.test.visit(this); pw.print(",");
            indent(indlevel);
            pw.print("if-true: " + s.iftrue + ",");
            indent(indlevel);
            pw.print("if-false: " + s.iffalse + ")");
            indlevel--;
        }

        public void visit(CONST e) {
            indent(indlevel);
            pw.print("CONST<" + Type.toString(e.type) + ">(" + e.value + ")");
        }

	public void visit(DATA s) { 
	    indent(indlevel++);
	    pw.print("DATA(");
	    if (s.data==null) pw.print("unspecified value");
	    else s.data.visit(this); 
	    indent(indlevel--);
	    pw.print(")");
	}

        public void visit(ESEQ e) {
            indent(--indlevel);
            indlevel++;
            pw.print("ESEQ(");
            e.stm.visit(this);
            pw.print(",");
            e.exp.visit(this);
            pw.print(")");
        }

        public void visit(EXP s) {
            indent(indlevel++);
            pw.print("EXP(");
            s.exp.visit(this);
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
            s.exp.visit(this);
            pw.print(")");
            indlevel--;
        }
            

        public void visit(LABEL s) {
            indent(indlevel);
            pw.print("LABEL(" + s.label + ")");
        }

        public void visit(MEM e) {
            indent(indlevel++);
            pw.print("MEM<" + Type.toString(e.type) + ">(");
            e.exp.visit(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(MOVE s) {
            indent(indlevel++);
            pw.print("MOVE(");
            s.dst.visit(this);
            pw.print(",");
            s.src.visit(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(NAME e) {
            indent(indlevel);
            pw.print("NAME(" + e.label + ")");
        }

        public void visit(NATIVECALL s) {
            printInvocation("NATIVECALL", s);
        }

        public void visit(RETURN s) {
            indent(indlevel++);
            pw.print("RETURN(");
            s.retval.visit(this);
            pw.print(")");
            indlevel--;
        }

	public void visit(SEGMENT s) { 
	    indent(--indlevel);
	    pw.print(s.toString());
	    indlevel++;
	}

        public void visit(SEQ s) {
            indent(--indlevel);
            indlevel++;
            pw.print("SEQ(");
            s.left.visit(this);
            pw.print(",");
            s.right.visit(this);
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
            s.retex.visit(this);
            pw.print(")");
            indlevel--;
        }

        public void visit(UNOP e) {
            indent(indlevel++);
            pw.print("UNOP<" + Type.toString(e.optype) + ">(");
            pw.print(Uop.toString(e.op) + ",");
            e.operand.visit(this);
            pw.print(")");
            indlevel--;
        }
    } 
}
