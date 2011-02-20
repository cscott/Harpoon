package demo.grid.MyServerPackage;
/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

final public class MyExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public demo.grid.MyServerPackage.MyException value;

	public MyExceptionHolder ()
	{
	}
	public MyExceptionHolder (demo.grid.MyServerPackage.MyException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return demo.grid.MyServerPackage.MyExceptionHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream _in)
	{
		value = demo.grid.MyServerPackage.MyExceptionHelper.read(_in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		demo.grid.MyServerPackage.MyExceptionHelper.write(_out,value);
	}
}
