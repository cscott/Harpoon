// IIR_PhysicalUnit.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_PhysicalUnit</code> class represents
 * physical units within a list of such physical units (and indirectly
 * within a physical type or subtype definition).
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PhysicalUnit.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PhysicalUnit extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PHYSICAL_UNIT; }

    /** The constructor method initializes a physical unit with an 
     *  unspecified source location, an unspecifier declarator, and
     *  an unspecified unit multiplier with no attributes. */
    public IIR_PhysicalUnit() { }
    //METHODS:  
    public void set_multiplier(IIR multiplier)
    { _multiplier = multiplier; }
    public IIR get_multiplier()
    { return _multiplier; }
 
    public void set_unit_name(IIR_PhysicalUnit unit_name)
    { _unit_name = unit_name; }
    public IIR_PhysicalUnit get_unit_name()
    { return _unit_name; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR _multiplier;
    IIR_PhysicalUnit _unit_name;
} // END class

