package demo.stringgrid.MyServerPackage;
/**
 *	Generated from IDL definition of alias "fixedT"
 *	@author JacORB IDL compiler 
 */

public class fixedTHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_alias_tc( demo.stringgrid.MyServerPackage.fixedTHelper.id(),"fixedT",org.omg.CORBA.ORB.init().create_string_tc(0));
	public fixedTHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, java.lang.String s)
	{
		any.insert_string(s);
	}
	public static java.lang.String extract(org.omg.CORBA.Any any)
	{
		return any.extract_string();
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:demo/stringgrid/MyServer/fixedT:1.0";
	}
	public static java.lang.String read(org.omg.CORBA.portable.InputStream _in)
	{
		java.lang.String _result;
		_result=_in.read_string();
		return _result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream _out, java.lang.String _s)
	{
		_out.write_string(_s);
	}
}
