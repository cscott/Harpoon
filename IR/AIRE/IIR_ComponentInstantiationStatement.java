// IIR_ComponentInstantiationStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentInstantiationStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentInstantiationStatement.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentInstantiationStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_COMPONENT_INSTANTIATION_STATEMENT
    //CONSTRUCTOR:
    public IIR_ComponentInstantiationStatement() { }
    
    //METHODS:  
    public void set_instantiated_unit(IIR instantiated_unit)
    { _instantiated_unit = instantiated_unit; }
 
    public IIR get_instantiated_unit()
    { return _instantiated_unit; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _instantiated_unit;
} // END class

