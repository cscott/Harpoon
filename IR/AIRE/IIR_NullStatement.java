// IIR_NullStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NullStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NullStatement.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NullStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NULL_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_NullStatement() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

