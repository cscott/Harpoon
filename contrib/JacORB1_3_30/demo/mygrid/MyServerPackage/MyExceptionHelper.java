package demo.mygrid.MyServerPackage;

/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

public class MyExceptionHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_struct_tc(demo.mygrid.MyServerPackage.MyExceptionHelper.id(),"MyException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("why",org.omg.CORBA.ORB.init().create_string_tc(0),null)});
	public MyExceptionHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, demo.mygrid.MyServerPackage.MyException s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static demo.mygrid.MyServerPackage.MyException extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:demo/mygrid/MyServer/MyException:1.0";
	}
	public static demo.mygrid.MyServerPackage.MyException read(org.omg.CORBA.portable.InputStream in)
	{
		demo.mygrid.MyServerPackage.MyException result = new demo.mygrid.MyServerPackage.MyException();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		result.why=in.read_string();
		return result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream out, demo.mygrid.MyServerPackage.MyException s)
	{
		out.write_string(id());
		out.write_string(s.why);
	}
}
