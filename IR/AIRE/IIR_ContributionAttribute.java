// IIR_ContributionAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ContributionAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ContributionAttribute.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ContributionAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONTRIBUTION_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ContributionAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

