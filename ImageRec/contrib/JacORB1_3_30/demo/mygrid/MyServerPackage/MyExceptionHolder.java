package demo.mygrid.MyServerPackage;
/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

final public class MyExceptionHolder
	implements org.omg.CORBA.portable.Streamable
{
	public demo.mygrid.MyServerPackage.MyException value;

	public MyExceptionHolder ()
	{
	}
	public MyExceptionHolder (demo.mygrid.MyServerPackage.MyException initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return demo.mygrid.MyServerPackage.MyExceptionHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream _in)
	{
		value = demo.mygrid.MyServerPackage.MyExceptionHelper.read(_in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream _out)
	{
		demo.mygrid.MyServerPackage.MyExceptionHelper.write(_out,value);
	}
}
