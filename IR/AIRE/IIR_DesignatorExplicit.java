// IIR_DesignatorExplicit.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignatorExplicit</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorExplicit.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorExplicit extends IIR_Designator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DESIGNATOR_EXPLICIT
    //CONSTRUCTOR:
    public IIR_DesignatorExplicit() { }
    //METHODS:  
    public void set_name(IIR name)
    { _name = name; }
 
    public IIR get_name()
    { return _name; }
 
    public void set_signature(IIR_Signature signature)
    { _signature = signature; }
 
    public IIR_Signature get_signature()
    { return _signature; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _name;
    IIR_Signature _signature;
} // END class

