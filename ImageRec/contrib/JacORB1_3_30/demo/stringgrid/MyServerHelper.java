package demo.stringgrid;
/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public class MyServerHelper
{
	public MyServerHelper()
	{
	}
	public static void insert(org.omg.CORBA.Any any, demo.stringgrid.MyServer s)
	{
		any.insert_Object(s);
	}
	public static demo.stringgrid.MyServer extract(org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:demo/stringgrid/MyServer:1.0", "MyServer");
	}
	public static String id()
	{
		return "IDL:demo/stringgrid/MyServer:1.0";
	}
	public static MyServer read(org.omg.CORBA.portable.InputStream in)
	{
		return narrow( in.read_Object());
	}
	public static void write(org.omg.CORBA.portable.OutputStream _out, demo.stringgrid.MyServer s)
	{
		_out.write_Object(s);
	}
	public static demo.stringgrid.MyServer narrow(org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (demo.stringgrid.MyServer)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:demo/stringgrid/MyServer:1.0"))
			{
				demo.stringgrid._MyServerStub stub;
				stub = new demo.stringgrid._MyServerStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
