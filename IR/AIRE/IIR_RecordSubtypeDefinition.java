// IIR_RecordSubtypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_RecordSubtypeDefinition</code> class represents
 * the subtype of a pre-existing record type definition; always a constrained
 * step in a record type definition.  This subtype has a record domain which
 * is a subset of the base type's domain.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordSubtypeDefinition.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RecordSubtypeDefinition extends IIR_RecordTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_RECORD_SUBTYPE_DEFINITION
    
    //METHODS:  
    public static IIR_RecordSubtypeDefinition get(IIR_RecordTypeDefinition base_type, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, resolution_function } );
        IIR_RecordSubtypeDefinition ret = (IIR_RecordSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_RecordSubtypeDefinition(base_type, resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    public void set_base_type(IIR_RecordTypeDefinition base_type)
    { _base_type = base_type; }
 
    public IIR_TypeDefinition /*IIR_RecordTypeDefinition*/ get_base_type()
    { return _base_type; } // FIXME?
 
    public void set_resolution_function(IIR_FunctionDeclaration resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_RecordTypeDefinition _base_type;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_RecordSubtypeDefinition(IIR_RecordTypeDefinition base_type, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

