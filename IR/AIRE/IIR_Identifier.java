// IIR_Identifier.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Identifier</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Identifier.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Identifier extends IIR_TextLiteral
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_IDENTIFIER
    
    //METHODS:  
    public IIR_Identifier get( String text, int length){
       return get(text);
    }
    public IIR_Identifier get( String text ) {
	  IIR_Identifier retval = new IIR_Identifier(text);
	  return retval;
    }

    public String get_text(){ return _text; }
    public int get_text_length(){ return _text.length(); }
 
    //MEMBERS:  

// PROTECTED:
    protected IIR_Identifier(String text, int length) {
       _text = text;
    }
    protected IIR_Identifier(String text) {
       _text = text;
    }
    protected String _text;

} // END class

