// IIR_SensitizedProcessStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SensitizedProcessStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SensitizedProcessStatement.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SensitizedProcessStatement extends IIR_ProcessStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SENSITIZED_PROCESS_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SensitizedProcessStatement() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

