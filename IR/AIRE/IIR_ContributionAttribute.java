// IIR_ContributionAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ContributionAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ContributionAttribute.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ContributionAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONTRIBUTION_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ContributionAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

