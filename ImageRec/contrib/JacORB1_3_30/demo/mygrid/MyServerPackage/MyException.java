package demo.mygrid.MyServerPackage;
/**
 *	Generated from IDL definition of exception "MyException"
 *	@author JacORB IDL compiler 
 */

public final class MyException
	extends org.omg.CORBA.UserException
{
	public MyException()
	{
		super(demo.mygrid.MyServerPackage.MyExceptionHelper.id());
	}

	public java.lang.String why = "";
	public MyException(java.lang.String _reason,java.lang.String why)
	{
		super(demo.mygrid.MyServerPackage.MyExceptionHelper.id()+""+_reason );
		this.why = why;
	}
	public MyException(java.lang.String why)
	{
		this.why = why;
	}
}
