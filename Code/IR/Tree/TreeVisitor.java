// TreeVisitor.java, created Thu Jan 14 19:06:18 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>TreeVisitor</code> is a Design Pattern, courtesy of Martin.
 * 
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: TreeVisitor.java,v 1.2 2002-02-25 21:05:42 cananian Exp $
 */
public abstract class TreeVisitor
{
    protected TreeVisitor() { } 

    public abstract void visit(Tree e);

    public void visit(ALIGN e)          { visit((Stm)e); }
    public void visit(BINOP e)          { visit((OPER)e); }
    public void visit(CALL e)           { visit((INVOCATION)e); }
    public void visit(CJUMP e)          { visit((Stm)e); }
    public void visit(CONST e)          { visit((Exp)e); }
    public void visit(DATUM e)          { visit((Stm)e); } 
    public void visit(ESEQ e)           { visit((Exp)e); }
    public void visit(Exp e)            { visit((Tree)e); }
    public void visit(EXPR e)           { visit((Stm)e); }
    public void visit(INVOCATION e)     { visit((Stm)e); }
    public void visit(JUMP e)           { visit((Stm)e); }
    public void visit(LABEL e)          { visit((Stm)e); }
    public void visit(MEM e)            { visit((Exp)e); }
    public void visit(METHOD e)         { visit((Stm)e); }
    public void visit(MOVE e)           { visit((Stm)e); }
    public void visit(NAME e)           { visit((Exp)e); }
    public void visit(NATIVECALL e)     { visit((INVOCATION)e); }
    public void visit(OPER e)           { visit((Exp)e); }
    public void visit(RETURN e)         { visit((Stm)e); }
    public void visit(SEGMENT e)        { visit((Stm)e); } 
    public void visit(SEQ e)            { visit((Stm)e); }
    public void visit(Stm e)            { visit((Tree)e); }
    public void visit(TEMP e)           { visit((Exp)e); }
    public void visit(THROW e)          { visit((Stm)e); }
    public void visit(UNOP e)           { visit((OPER)e); }
}

