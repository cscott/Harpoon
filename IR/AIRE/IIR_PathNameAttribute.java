// IIR_PathNameAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PathNameAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PathNameAttribute.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PathNameAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_PATH_NAME_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_PathNameAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

