package harpoon.IR.Tree;

/**
 * <code>TreeVisitor</code> is a Design Pattern, courtesy of Martin.
 * 
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>
 * @version  $Id: TreeVisitor.java,v 1.1.2.8 1999-02-18 23:38:09 duncan Exp $
 */
public abstract class TreeVisitor
{
  protected TreeVisitor() { } 

  public abstract void visit(Exp e);
  public abstract void visit(Stm e);
  public void visit(BINOP e)          { visit((OPER)e); }
  public void visit(CALL e)           { visit((INVOCATION)e); }
  public void visit(CJUMP e)          { visit((Stm)e); }
  public void visit(CONST e)          { visit((Exp)e); }
  public void visit(ESEQ e)           { visit((Exp)e); }
  public void visit(EXP e)            { visit((Stm)e); }
  public void visit(INVOCATION e)     { visit((Stm)e); }
  public void visit(JUMP e)           { visit((Stm)e); }
  public void visit(LABEL e)          { visit((Stm)e); }
  public void visit(MEM e)            { visit((Exp)e); }
  public void visit(MOVE e)           { visit((Stm)e); }
  public void visit(NAME e)           { visit((Exp)e); }
  public void visit(NATIVECALL e)     { visit((INVOCATION)e); }
  public void visit(OPER e)           { visit((Exp)e); }
  public void visit(RETURN e)         { visit((Stm)e); }
  public void visit(SEQ e)            { visit((Stm)e); }
  public void visit(TEMP e)           { visit((Exp)e); }
  public void visit(THROW e)          { visit((Stm)e); }
  public void visit(UNOP e)           { visit((OPER)e); }
}

