// IIR_ScalarNatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The <code>IIR_ScalarNatureDefinition</code> class represents predefined
 * methods, subprograms, and public data elements describing scalar natures.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ScalarNatureDefinition.java,v 1.2 1998-10-10 09:58:35 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ScalarNatureDefinition extends IIR_NatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SCALAR_NATURE_DEFINITION
    //CONSTRUCTOR:
    public IIR_ScalarNatureDefinition() { }
    //METHODS:  
    public void set_across(IIR_NatureDefinition across)
    { _across = across; }
 
    public IIR_NatureDefinition get_across()
    { return _across; }
 
    public void set_through(IIR_NatureDefinition through)
    { _through = through; }
 
    public IIR_NatureDefinition get_through()
    { return _through; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _across;
    IIR_NatureDefinition _through;
} // END class

