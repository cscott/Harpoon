// IIR_Name.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_Name</code> class represents the general class
 * of referents to explicitly or implicitly declared named entities.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Name.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Name extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    //METHODS:  
    static IIR_Declaration[] lookup(IIR_TextLiteral identifier) {
	throw new Error("unimplemented."); // FIXME!
    }
 
    public void set_prefix(IIR prefix)
    { _prefix = prefix; }
 
    public IIR get_prefix()
    { return _prefix; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _prefix;
} // END class

