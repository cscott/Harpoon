// IIR_EntityClassEntry.java, created by cananian
package harpoon.IR.AIRE;

/**
 * A predefined <code>IIR_EntityClassEntry</code> represents a specific
 * kind of entity within an <code>IIR_EntityClassList</code>.  The
 * <code>IIR_EntityClassList</code> in turn appears only within a group
 * template declaration.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EntityClassEntry.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EntityClassEntry extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ENTITY_CLASS_ENTRY
    //CONSTRUCTOR:
    public IIR_EntityClassEntry() { }
    //METHODS:  
    public void set_entity_kind(int entity_kind)
    { _entity_kind = entity_kind; }
    public int get_entity_kind()
    { return _entity_kind; }
 
    public void set_boxed(boolean is_boxed)
    { _boxed = is_boxed; }
    public boolean get_boxed()
    { return _boxed; }
 
    //MEMBERS:  

// PROTECTED:
    int _entity_kind;
    boolean _boxed;
} // END class

