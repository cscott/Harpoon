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
 * @version $Id: Print.java,v 1.1.2.3 1999-01-15 17:56:40 duncan Exp $
 */
public class Print 
{
  private PrintVisitor pv;

  public Print(PrintStream o)
    {
      this(o, null);
    }

  public Print(PrintStream o, TempMap t)
    {
      pv = new PrintVisitor(o, t);
    }

  public void prStm(Stm s)
    {
      s.visit(pv);
    }

  public void prExp(Exp e)
    {
      e.visit(pv);
    }

  static class PrintVisitor extends TreeVisitor
  {
    private PrintStream m_out; 
    private TempMap     m_tMap;
    private int         m_indent;

    public PrintVisitor(PrintStream out) { this(out, null); }

    public PrintVisitor(PrintStream out, TempMap tMap)
      {
	m_out  = out;
	m_tMap = tMap;
      }
    
    private void indent(int d) 
      {
	for(int i=0; i<d; i++) 
	  m_out.print(' ');
      }

    private void printCONST(CONST e, String constStr)
      {
	indent(m_indent);
	say(constStr + " "); say(String.valueOf(e.value()));
      }

    private void printMEM(MEM e, String memStr)
      {
	indent(m_indent++);
	sayln(memStr + "("); visit(e.exp); say(")");
	m_indent--;
      }
    
    private void printTEMP(TEMP e, String tempStr)
      {
	indent(m_indent); say(tempStr + " ");
	Temp t = (m_tMap==null)?e.temp:m_tMap.tempMap(e.temp);
	say(t.toString());
      }

    private void say(String s) 
      {
	m_out.print(s);
      }
    
    private void sayln(String s) 
      {
	say(s); say("\n");
      }

    public void visit(BINOP e)
      {
	indent(m_indent++);
	say("BINOP("); 
	say("FIXME");
	/*
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
	visit(e.left); sayln(","); 
	visit(e.right); say(")");
	m_indent--;
      }

    public void visit(CALL s)
      {
	indent(m_indent++); sayln("CALL(");
	visit(s.func);
	m_indent++;
        for(ExpList a = s.args; a!=null; a=a.tail) 
	  {
	    sayln(","); visit(a.head);
	  }
        say(")");
	m_indent -= 2;
      }
    
    public void visit(CONST e)  { printCONST(e, "CONST"); }

    public void visit(CONSTD e) { printCONST(e, "CONSTD"); }

    public void visit(CONSTF e) { printCONST(e, "CONSTF"); }

    public void visit(CONSTI e) { printCONST(e, "CONSTI"); }

    public void visit(CONSTL e) { printCONST(e, "CONSTL"); }

    public void visit(CJUMP s)
      {
	indent(m_indent++);
	say("CJUMP("); visit(s.test); sayln(",");
	indent(m_indent--);
	say(s.iftrue.toString()); say(","); 
	say(s.iffalse.toString()); say(")");
      }

    public void visit(ESEQ e)
      {
	indent(m_indent++);
	sayln("ESEQ("); visit(e.stm); sayln(",");
	visit(e.exp); say(")");
	m_indent--;
      }

    public void visit(EXP s)
      {
	indent(m_indent++);
	sayln("EXP(");
	visit(s.exp); say(")");
	m_indent--;
      }

    public void visit(Exp e)
      {
	throw new Error("Print.visit(Exp e)");
      }
    
    public void visit(JUMP s)
      {
	indent(m_indent++);
	sayln("JUMP("); 
	visit(s.exp); say(")");
	m_indent--;
      }

    public void visit(LABEL s)
      {
	indent(m_indent);
	say("LABEL "); say(s.label.toString());
      }

    public void visit(MEM e)  { printMEM(e, "MEM"); }

    public void visit(MEMA e) { printMEM(e, "MEMA"); }

    public void visit(MEMD e) { printMEM(e, "MEMD"); }

    public void visit(MEMF e) { printMEM(e, "MEMF"); }

    public void visit(MEMI e) { printMEM(e, "MEMI"); }

    public void visit(MEML e) { printMEM(e, "MEML"); }

    public void visit(MOVE s)
      {
	indent(m_indent++);
	sayln("MOVE("); 
	visit(s.dst); sayln(","); 
	visit(s.src); sayln(")");
	m_indent--;
      }

    public void visit(NAME e)
      {
	indent(m_indent); say("NAME "); say(e.label.toString());
      }

    public void visit(SEQ s)
      {
	indent(m_indent++);
	sayln("SEQ("); 
	visit(s.left); visit(s.right); say(")");
	m_indent--;
      }

    public void visit(Stm s)
      {
	throw new Error("Print.visit(Stm s)");
      }

    public void visit(TEMP e)  { printTEMP(e, "TEMP"); }

    public void visit(TEMPA e) { printTEMP(e, "TEMPA"); }

    public void visit(TEMPD e) { printTEMP(e, "TEMPD"); }

    public void visit(TEMPF e) { printTEMP(e, "TEMPF"); }

    public void visit(TEMPI e) { printTEMP(e, "TEMPI"); }

    public void visit(TEMPL e) { printTEMP(e, "TEMPL"); }

    public void visit(UNOP e)
      {
	indent(m_indent++); say("UNOP("); 
	say("FIXME");
	/*
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
	visit(e.operand); say(")");
	m_indent--;
      }
    
  }
}



