// IIR_EnumerationSubtypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_EnumerationSubtypeDefinition</code> class
 * represents a subset of the literals represented by an
 * <code>IIR_EnumerationTypeDefinition</code> base type.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationSubtypeDefinition.java,v 1.3 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationSubtypeDefinition extends IIR_EnumerationTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ENUMERATION_SUBTYPE_DEFINITION
    
    
    //METHODS:  
    public static IIR_EnumerationSubtypeDefinition get(IIR_EnumerationTypeDefinition base_type, IIR_EnumerationLiteral left_limit, IIR_EnumerationLiteral right_limit, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, left_limit, right_limit, resolution_function } );
        IIR_EnumerationSubtypeDefinition ret = (IIR_EnumerationSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_EnumerationSubtypeDefinition(base_type, left_limit, right_limit, resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    public void set_resolution_function(IIR_FunctionDeclaration resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing */ }
 
    //MEMBERS:  
	// GETS ENUMERATION_LITERALS FROM PARENT_CLASS. FIXME?
	//public IIR_EnumerationLiteralList enumeration_literals;

// PROTECTED:
    IIR_EnumerationTypeDefinition _base_type;
    IIR_EnumerationLiteral _left_limit;
    IIR_EnumerationLiteral _right_limit;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_EnumerationSubtypeDefinition(IIR_EnumerationTypeDefinition base_type, IIR_EnumerationLiteral left_limit, IIR_EnumerationLiteral right_limit, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _left_limit = left_limit;
        _right_limit = right_limit;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

