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
 * @version $Id: Print.java,v 1.1.2.5 1999-02-05 10:40:45 cananian Exp $
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
	say(Type.toString(e.type));
	say(", ");
	say(Bop.toString(e.op));
	sayln(",");
	e.left.visit(this); sayln(","); 
	e.right.visit(this); say(")");
	m_indent--;
      }

    public void visit(CALL s)
      {
	indent(m_indent++); sayln("CALL(");
	indent(m_indent++); sayln("Return value in: "); 
	s.retval.visit(this); sayln("");
	indent(m_indent-1); sayln("Exceptional value in: ");
	s.retex.visit(this); sayln("");
	indent(m_indent-1); sayln("Function: ");
	s.func.visit(this);
        for(ExpList a = s.args; a!=null; a=a.tail) 
	  {
	    sayln(","); a.head.visit(this);
	  }
        say(")");
	m_indent -= 2;
      }
    
    public void visit(CONST e)  {
	indent(m_indent);
	say("CONST (");
	say(Type.toString(e.type));
	say(") ");
	say(String.valueOf(e.value()));
    }

    public void visit(CJUMP s)
      {
	indent(m_indent++);
	say("CJUMP("); s.test.visit(this); sayln(",");
	indent(m_indent--);
	say(s.iftrue.toString()); say(","); 
	say(s.iffalse.toString()); say(")");
      }

    public void visit(ESEQ e)
      {
	indent(m_indent++);
	sayln("ESEQ("); e.stm.visit(this); sayln(",");
	e.exp.visit(this); say(")");
	m_indent--;
      }

    public void visit(EXP s)
      {
	indent(m_indent++);
	sayln("EXP(");
	s.exp.visit(this); say(")");
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
	s.exp.visit(this); say(")");
	m_indent--;
      }

    public void visit(LABEL s)
      {
	indent(m_indent);
	say("LABEL "); say(s.label.toString());
      }

    public void visit(MEM e)  {
	indent(m_indent++);
	sayln("MEM("); say(Type.toString(e.type)); say(", ");
	e.exp.visit(this);
	say(")");
	m_indent--;
    }

    public void visit(MOVE s)
      {
	sayln("");
	indent(m_indent++);
	sayln("MOVE("); 
	s.dst.visit(this); sayln(","); 
	s.src.visit(this); say(")");
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
	s.left.visit(this); s.right.visit(this); say(")");
	m_indent--;
      }

    public void visit(Stm s)
      {
	throw new Error("Print.visit(Stm s)");
      }

    public void visit(TEMP e)  {
	indent(m_indent); say("TEMP (");
	say(Type.toString(e.type));
	say(") ");
	Temp t = (m_tMap==null)?e.temp:m_tMap.tempMap(e.temp);
	say(t.toString());
    }

    public void visit(UNOP e)
      {
	indent(m_indent++); say("UNOP("); 
	say(Type.toString(e.type));
	say(", ");
	say(Uop.toString(e.op));
	sayln(",");
	e.operand.visit(this); say(")");
	m_indent--;
      }
    
  }
}



