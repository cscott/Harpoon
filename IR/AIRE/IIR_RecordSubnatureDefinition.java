// IIR_RecordSubnatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordSubnatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordSubnatureDefinition.java,v 1.4 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_RecordSubnatureDefinition extends IIR_RecordNatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_RECORD_SUBNATURE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RecordSubnatureDefinition() { }
    //METHODS:  
    public void set_across_tolerance(IIR across_tolerance)
    { _across_tolerance = across_tolerance; }
 
    public IIR get_across_tolerance()
    { return _across_tolerance; }
 
    public void set_through_tolerance(IIR through_tolerance)
    { _through_tolerance = through_tolerance; }
 
    public IIR get_through_tolerance()
    { return _through_tolerance; }
 
    /* FIXME
    public IIR_RecordNatureDefinition get_base_nature()
    { return _base_nature; }
    */

    //MEMBERS:  

// PROTECTED:
    IIR _across_tolerance;
    IIR _through_tolerance;
} // END class

