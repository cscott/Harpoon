// IIR_SimultaneousAlternativeByOthers.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternativeByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternativeByOthers.java,v 1.2 1998-10-11 00:32:26 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternativeByOthers extends IIR_SimultaneousAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS; }
    //CONSTRUCTOR:
    public IIR_SimultaneousAlternativeByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

