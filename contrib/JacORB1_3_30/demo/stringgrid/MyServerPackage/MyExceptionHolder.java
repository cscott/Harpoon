package demo.stringgrid.MyServerPackage;
/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

final public class MyExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public demo.stringgrid.MyServerPackage.MyException value;

	public MyExceptionHolder ()
	{
	}
	public MyExceptionHolder (demo.stringgrid.MyServerPackage.MyException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return demo.stringgrid.MyServerPackage.MyExceptionHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream _in)
	{
		value = demo.stringgrid.MyServerPackage.MyExceptionHelper.read(_in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		demo.stringgrid.MyServerPackage.MyExceptionHelper.write(_out,value);
	}
}
