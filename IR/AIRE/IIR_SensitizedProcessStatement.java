// IIR_SensitizedProcessStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SensitizedProcessStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SensitizedProcessStatement.java,v 1.3 1998-10-11 01:25:01 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SensitizedProcessStatement extends IIR_ProcessStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SENSITIZED_PROCESS_STATEMENT).
     * @return <code>IR_Kind.IR_SENSITIZED_PROCESS_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SENSITIZED_PROCESS_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SensitizedProcessStatement() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

