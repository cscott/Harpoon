// IIR_NullStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NullStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NullStatement.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NullStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NULL_STATEMENT
    //CONSTRUCTOR:
    public IIR_NullStatement() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

