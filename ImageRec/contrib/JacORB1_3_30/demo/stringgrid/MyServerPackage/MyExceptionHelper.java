package demo.stringgrid.MyServerPackage;

/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

public class MyExceptionHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_struct_tc(demo.stringgrid.MyServerPackage.MyExceptionHelper.id(),"MyException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("why",org.omg.CORBA.ORB.init().create_string_tc(0),null)});
	public MyExceptionHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, demo.stringgrid.MyServerPackage.MyException s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static demo.stringgrid.MyServerPackage.MyException extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:demo/stringgrid/MyServer/MyException:1.0";
	}
	public static demo.stringgrid.MyServerPackage.MyException read(org.omg.CORBA.portable.InputStream in)
	{
		demo.stringgrid.MyServerPackage.MyException result = new demo.stringgrid.MyServerPackage.MyException();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		result.why=in.read_string();
		return result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, demo.stringgrid.MyServerPackage.MyException s)
	{
		out.write_string(id());
		out.write_string(s.why);
	}
}
