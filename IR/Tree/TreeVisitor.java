package harpoon.IR.Tree;

/**
 * <code>TreeVisitor</code> is a Design Pattern, courtesy of Martin.
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version 1.1.2.3
 */
public abstract class TreeVisitor
{
  protected TreeVisitor() { } 

  public abstract void visit(Exp e);
  public abstract void visit(Stm e);
  public void visit(BINOP e)          { visit((OPER)e); }
  public void visit(CALL e)           { visit((Stm)e); }
  public void visit(CJUMP e)          { visit((Stm)e); }
  public void visit(CONST e)          { visit((Exp)e); }
  public void visit(CONSTD e)         { visit((CONST)e); }
  public void visit(CONSTF e)         { visit((CONST)e); }
  public void visit(CONSTI e)         { visit((CONST)e); }  
  public void visit(CONSTL e)         { visit((CONST)e); }
  public void visit(ESEQ e)           { visit((Exp)e); }
  public void visit(EXP e)            { visit((Stm)e); }
  public void visit(JUMP e)           { visit((Stm)e); }
  public void visit(LABEL e)          { visit((Stm)e); }
  public void visit(MEM e)            { visit((Exp)e); }
  public void visit(MEMA e)           { visit((MEM)e); }
  public void visit(MEMD e)           { visit((MEM)e); }
  public void visit(MEMF e)           { visit((MEM)e); }
  public void visit(MEMI e)           { visit((MEM)e); }
  public void visit(MEML e)           { visit((MEM)e); }
  public void visit(MOVE e)           { visit((Stm)e); }
  public void visit(NAME e)           { visit((Exp)e); }
  public void visit(OPER e)           { visit((Exp)e); }
  public void visit(SEQ e)            { visit((Stm)e); }
  public void visit(TEMP e)           { visit((Exp)e); }
  public void visit(TEMPA e)          { visit((TEMP)e); }
  public void visit(TEMPD e)          { visit((TEMP)e); }
  public void visit(TEMPF e)          { visit((TEMP)e); }
  public void visit(TEMPI e)          { visit((TEMP)e); }
  public void visit(TEMPL e)          { visit((TEMP)e); }
  public void visit(UNOP e)           { visit((OPER)e); }

}

