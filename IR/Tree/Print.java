// Print.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import java.io.PrintStream;

/**
 * <code>Print</code> pretty-prints Trees.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Print.java,v 1.1.2.2 1999-01-14 06:04:58 cananian Exp $
 */
public class Print {

    final PrintStream out;
    final TempMap tmap;

    public Print(java.io.PrintStream o, TempMap t) {out=o; tmap=t;}

    public Print(java.io.PrintStream o) {out=o; tmap=null;}

    void indent(int d) {
	for(int i=0; i<d; i++) 
            out.print(' ');
    }

    void say(String s) {
	out.print(s);
    }

    void sayln(String s) {
	say(s); say("\n");
    }

    void prStm(SEQ s, int d) {
	indent(d); sayln("SEQ("); prStm(s.left,d+1); sayln(",");
	prStm(s.right,d+1); say(")");
    }

    void prStm(LABEL s, int d) {
	indent(d); say("LABEL "); say(s.label.toString());
    }

    void prStm(JUMP s, int d) {
	indent(d); sayln("JUMP("); prExp(s.exp, d+1); say(")");
    }

    void prStm(CJUMP s, int d) {
	indent(d); say("CJUMP("); prExp(s.test, d+1); sayln(",");
	indent(d+1); say(s.iftrue.toString()); say(","); 
	say(s.iffalse.toString()); say(")");
    }

    void prStm(MOVE s, int d) {
	indent(d); sayln("MOVE("); prExp(s.dst,d+1); sayln(","); 
	prExp(s.src,d+1); say(")");
    }

    void prStm(EXP s, int d) {
	indent(d); sayln("EXP("); prExp(s.exp,d+1); say(")"); 
    }

    void prStm(Stm s, int d) {
        if (s instanceof SEQ) prStm((SEQ)s, d);
	else if (s instanceof LABEL) prStm((LABEL)s, d);
	else if (s instanceof JUMP) prStm((JUMP)s, d);
	else if (s instanceof CJUMP) prStm((CJUMP)s, d);
	else if (s instanceof MOVE) prStm((MOVE)s, d);
	else if (s instanceof EXP) prStm((EXP)s, d);
	else throw new Error("Print.prStm");
    }

    void prExp(BINOP e, int d) {
	indent(d); say("BINOP("); 
	say("FIXME");/*
	switch(e.op) {
	case BINOP.PLUS: say("PLUS"); break;
	case BINOP.MINUS: say("MINUS"); break;
	case BINOP.MUL: say("MUL"); break;
	case BINOP.DIV: say("DIV"); break;
	case BINOP.AND: say("AND"); break;
	case BINOP.OR: say("OR"); break;
	case BINOP.LSHIFT: say("LSHIFT"); break;
	case BINOP.RSHIFT: say("RSHIFT"); break;
	case BINOP.ARSHIFT: say("ARSHIFT"); break;
	case BINOP.XOR: say("XOR"); break;
	default:
	    throw new Error("Print.prExp.BINOP");
	}
	*/
	sayln(",");
	prExp(e.left,d+1); sayln(","); prExp(e.right,d+1); say(")");
    }

    void prExp(UNOP e, int d) {
	indent(d); say("UNOP("); 
	say("FIXME");/*
	switch(e.op) {
	case BINOP.PLUS: say("PLUS"); break;
	case BINOP.MINUS: say("MINUS"); break;
	case BINOP.MUL: say("MUL"); break;
	case BINOP.DIV: say("DIV"); break;
	case BINOP.AND: say("AND"); break;
	case BINOP.OR: say("OR"); break;
	case BINOP.LSHIFT: say("LSHIFT"); break;
	case BINOP.RSHIFT: say("RSHIFT"); break;
	case BINOP.ARSHIFT: say("ARSHIFT"); break;
	case BINOP.XOR: say("XOR"); break;
	default:
	    throw new Error("Print.prExp.BINOP");
	}
	*/
	sayln(",");
	prExp(e.operand,d+1); say(")");
    }

    void prExp(MEM e, int d) {
	indent(d);
	sayln("MEM("); prExp(e.exp,d+1); say(")");
    }

    void prExp(LMEM e, int d) {
	indent(d);
	sayln("LMEM("); prExp(e.exp,d+1); say(")");
    }

    void prExp(TEMP e, int d) {
	indent(d); say("TEMP ");
	Temp t = (tmap==null)?e.temp:tmap.tempMap(e.temp);
	say(t.toString());
    }

    void prExp(LTEMP e, int d) {
	indent(d); say("LTEMP ");
	Temp t = (tmap==null)?e.temp:tmap.tempMap(e.temp);
	say(t.toString());
    }

    void prExp(ESEQ e, int d) {
	indent(d); sayln("ESEQ("); prStm(e.stm,d+1); sayln(",");
	prExp(e.exp,d+1); say(")");

    }

    void prExp(NAME e, int d) {
	indent(d); say("NAME "); say(e.label.toString());
    }

    void prExp(ICONST e, int d) {
	indent(d); say("ICONST "); say(String.valueOf(e.value));
    }

    void prExp(LCONST e, int d) {
	indent(d); say("LCONST "); say(String.valueOf(e.value));
    }

    void prExp(FCONST e, int d) {
	indent(d); say("FCONST "); say(String.valueOf(e.value));
    }

    void prExp(DCONST e, int d) {
	indent(d); say("DCONST "); say(String.valueOf(e.value));
    }

    void prExp(CALL e, int d) {
	indent(d); sayln("CALL(");
	prExp(e.func,d+1);
        for(ExpList a = e.args; a!=null; a=a.tail) {
	    sayln(","); prExp(a.head,d+2); 
        }
        say(")");
    }

    void prExp(Exp e, int d) {
        if (e instanceof BINOP) prExp((BINOP)e, d);
        else if (e instanceof UNOP) prExp((UNOP)e, d);
	else if (e instanceof MEM) prExp((MEM)e, d);
	else if (e instanceof LMEM) prExp((LMEM)e, d);
	else if (e instanceof TEMP) prExp((TEMP)e, d);
	else if (e instanceof LTEMP) prExp((LTEMP)e, d);
	else if (e instanceof ESEQ) prExp((ESEQ)e, d);
	else if (e instanceof NAME) prExp((NAME)e, d);
	else if (e instanceof ICONST) prExp((ICONST)e, d);
	else if (e instanceof LCONST) prExp((LCONST)e, d);
	else if (e instanceof FCONST) prExp((FCONST)e, d);
	else if (e instanceof DCONST) prExp((DCONST)e, d);
	else if (e instanceof CALL) prExp((CALL)e, d);
	else throw new Error("Print.prExp");
    }

    public void prStm(Stm s) {prStm(s,0); say("\n");}
    public void prExp(Exp e) {prExp(e,0); say("\n");}

}
