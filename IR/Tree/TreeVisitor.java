package harpoon.IR.Tree;

public abstract class TreeVisitor
{
  protected TreeVisitor() { } 

  public abstract void visit(Exp e);
  public abstract void visit(Stm e);
  public void visit(BINOP e)          { visit((OPER)e); }
  public void visit(CALL e)           { visit((Exp)e); }
  public void visit(CJUMP e)          { visit((Stm)e); }
  public void visit(CONST e)          { visit((Exp)e); }
  public void visit(DCONST e)         { visit((CONST)e); }
  public void visit(ESEQ e)           { visit((Exp)e); }
  public void visit(EXP e)            { visit((Stm)e); }
  public void visit(FCONST e)         { visit((CONST)e); }
  public void visit(ICONST e)         { visit((CONST)e); }
  public void visit(JUMP e)           { visit((Stm)e); }
  public void visit(LABEL e)          { visit((Stm)e); }
  public void visit(LCONST e)         { visit((CONST)e); }
  public void visit(LMEM e)           { visit((Exp)e); }
  public void visit(LTEMP e)          { visit((Exp)e); }
  public void visit(MEM e)            { visit((Exp)e); }
  public void visit(MOVE e)           { visit((Stm)e); }
  public void visit(NAME e)           { visit((Exp)e); }
  public void visit(OPER e)           { visit((Exp)e); }
  public void visit(SEQ e)            { visit((Stm)e); }
  public void visit(TEMP e)           { visit((Exp)e); }
  public void visit(UNOP e)           { visit((OPER)e); }

}

