// IIR_DesignFile.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignFile</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignFile.java,v 1.5 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignFile extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DESIGN_FILE).
     * @return <code>IR_Kind.IR_DESIGN_FILE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DESIGN_FILE; }
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
    public static IIR_DesignFileList design_files;
    public IIR_CommentList comments;
    public IIR_LibraryUnitList library_units;

// PROTECTED:
    IIR_Identifier _name;
    IR_SourceLanguage _source_language;
} // END class

