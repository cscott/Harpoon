// IIR_AssociationElementByOthers.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AssociationElementByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElementByOthers.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssociationElementByOthers extends IIR_AssociationElement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ASSOCIATION_ELEMENT_BY_OTHERS
    //CONSTRUCTOR:
    public IIR_AssociationElementByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

