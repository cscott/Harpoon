// IIR_ProcessStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ProcessStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ProcessStatement.java,v 1.4 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ProcessStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PROCESS_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ProcessStatement() { }
    //METHODS:  
    public void set_postponed(boolean postponed)
    { _postponed = postponed; }
 
    public boolean get_postponed()
    { return _postponed; }
 
    //MEMBERS:  
    public IIR_DeclarationList process_declarative_part;
    public IIR_SequentialStatementList process_statement_part;

// PROTECTED:
    boolean _postponed;
} // END class

