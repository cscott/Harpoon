// IIR.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR.java,v 1.3 1998-10-11 00:32:15 cananian Exp $
 *///

//-----------------------------------------------------------
public abstract class IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    //METHODS:  
    public abstract IR_Kind get_kind();
 
    public static String get_kind_text(IR_Kind kind) 
    { return kind.toString(); }
 
    public void set_file_name(IIR_Identifier file_name)
    { _file_name = file_name; }
 
    public IIR_Identifier get_file_name() 
    { return _file_name; }
 
    public void set_character_offset(int character_offset)
    { _character_offset = character_offset; }
 
    public int get_character_offset()
    { return _character_offset; }
 
    public void set_line_number(int line_number)
    { _line_number = line_number; }
 
    public int get_line_number()
    { return _line_number; }
 
    public void set_column_number(int column_number)
    { _column_number = column_number; }
 
    public int get_column_number()
    { return _column_number; }
 
    public void set_sheet_name(IIR_Identifier sheet_name)
    { _sheet_name = sheet_name; }
 
    public IIR_Identifier get_sheet_name()
    { return _sheet_name; }
 
    public void set_x_coordinate(int x_coordinate)
    { _x_coordinate = x_coordinate; }
 
    public int get_x_coordinate()
    { return _x_coordinate; }
 
    public void set_y_coordinate(int y_coordinate)
    { _y_coordinate = y_coordinate; }
 
    public int get_y_coordinate()
    { return _y_coordinate; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_Identifier _file_name;
    int _character_offset;
    int _line_number;
    int _column_number;
    IIR_Identifier _sheet_name;
    int _x_coordinate;
    int _y_coordinate;
    int _kind;
} // END class

