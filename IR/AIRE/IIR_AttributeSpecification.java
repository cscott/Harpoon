// IIR_AttributeSpecification.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AttributeSpecification</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AttributeSpecification.java,v 1.2 1998-10-10 11:05:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AttributeSpecification extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ATTRIBUTE_SPECIFICATION
    //CONSTRUCTOR:
    public IIR_AttributeSpecification() { }
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    public void set_entity_class(IIR_Identifier entity_class)
    { _entity_class = entity_class; }
 
    public IIR_Identifier get_entity_class()
    { return _entity_class; }
 
    //MEMBERS:  
    public IIR_DesignatorList entity_name_list;

// PROTECTED:
    IIR _value;
    IIR_Identifier _entity_class;
} // END class

