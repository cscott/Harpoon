// IIR_DesignatorExplicit.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignatorExplicit</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorExplicit.java,v 1.3 1998-10-11 01:24:56 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorExplicit extends IIR_Designator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DESIGNATOR_EXPLICIT).
     * @return <code>IR_Kind.IR_DESIGNATOR_EXPLICIT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DESIGNATOR_EXPLICIT; }
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

