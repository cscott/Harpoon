// IIR_Identifier.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import java.util.Hashtable;
/**
 * <code>IIR_Identifier</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Identifier.java,v 1.5 1998-10-11 02:37:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Identifier extends IIR_TextLiteral
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_IDENTIFIER).
     * @return <code>IR_Kind.IR_IDENTIFIER</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_IDENTIFIER; }
    
    //METHODS:  
    public static IIR_Identifier get( String text, int length){
       return get(text);
    }
    public static IIR_Identifier get( String text ) {
	IIR_Identifier retval = (IIR_Identifier) _h.get(text);
	if (retval==null) {
	    retval = new IIR_Identifier(text);
	    _h.put(text, retval);
	}
	return retval;
    }

    public String get_text(){ return _text; }
    public int get_text_length(){ return _text.length(); }
 
    //MEMBERS:  

// PROTECTED:
    protected String _text;
    protected IIR_Identifier(String text) {
       _text = text;
    }
    private static Hashtable _h = new Hashtable();

} // END class

