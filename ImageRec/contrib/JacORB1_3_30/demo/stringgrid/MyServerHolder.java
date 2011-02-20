package demo.stringgrid;

/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public class MyServerHolder	implements org.omg.CORBA.portable.Streamable{
	 public MyServer value;
	public MyServerHolder()
	{
	}
	public MyServerHolder(MyServer initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return MyServerHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = MyServerHelper.read(in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		MyServerHelper.write(_out,value);
	}
}
