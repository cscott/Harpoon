// IIR_Expression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Expression</code> %
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Expression.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Expression extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    public void set_subtype( IIR_TypeDefinition subtype){ _subtype = subtype;}
 
    public IIR_TypeDefinition get_subtype(){return _subtype;}
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

