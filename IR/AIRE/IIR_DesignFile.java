// IIR_DesignFile.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignFile</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignFile.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignFile extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DESIGN_FILE
    //CONSTRUCTOR:
    public IIR_DesignFile() { }
    //METHODS:  
    public void set_name(IIR_Identifier name)
    { _name = name; }
 
    public IIR_Identifier get_name()
    { return _name; }
 
    public void set_source_language(IR_SourceLanguage source_language)
    { _source_language = source_language; }
 
    public IR_SourceLanguage get_source_language()
    { return _source_language; }
 
    //MEMBERS:  
    static IIR_DesignFileList design_files;
    IIR_CommentList comments;
    IIR_LibraryUnitList library_units;

// PROTECTED:
    IIR_Identifier _name;
    IR_SourceLanguage _source_language;
} // END class

