// IIR_Declaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Declaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Declaration.java,v 1.2 1998-10-10 09:58:34 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Declaration extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    public void set_declarator(IIR_TextLiteral identifier)
    { _declarator = identifier; }
 
    public IIR_TextLiteral get_declarator()
    { return _declarator; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TextLiteral _declarator;
} // END class

