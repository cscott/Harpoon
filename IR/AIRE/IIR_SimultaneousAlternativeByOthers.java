// IIR_SimultaneousAlternativeByOthers.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternativeByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternativeByOthers.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternativeByOthers extends IIR_SimultaneousAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS
    //CONSTRUCTOR:
    public IIR_SimultaneousAlternativeByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

