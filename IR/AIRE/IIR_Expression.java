// IIR_Expression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Expression</code> %
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Expression.java,v 1.3 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Expression extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_subtype( IIR_TypeDefinition subtype){ _subtype = subtype;}
 
    public IIR_TypeDefinition get_subtype(){return _subtype;}
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

