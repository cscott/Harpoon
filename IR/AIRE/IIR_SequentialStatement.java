// IIR_SequentialStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SequentialStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SequentialStatement.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SequentialStatement extends IIR_Statement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

