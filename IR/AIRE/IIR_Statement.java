// IIR_Statement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Statement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Statement.java,v 1.3 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Statement extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_label(IIR_Label label)
    { _label = label; }
 
    public IIR_Label get_label()
    { return _label; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_Label _label;
} // END class

