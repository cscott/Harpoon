package demo.grid;

import org.omg.PortableServer.POA;
/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public class MyServerPOATie
	extends MyServerPOA
{
	private MyServerOperations _delegate;

	private POA _poa;
	public MyServerPOATie(MyServerOperations delegate)
	{
		_delegate = delegate;
	}
	public MyServerPOATie(MyServerOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public demo.grid.MyServer _this()
	{
		return demo.grid.MyServerHelper.narrow(_this_object());
	}
	public demo.grid.MyServer _this(org.omg.CORBA.ORB orb)
	{
		return demo.grid.MyServerHelper.narrow(_this_object(orb));
	}
	public MyServerOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(MyServerOperations delegate)
	{
		_delegate = delegate;
	}
	public short height()
	{
		return _delegate.height();
	}

	public short opWithException() throws demo.grid.MyServerPackage.MyException
	{
		return _delegate.opWithException();
	}

	public void set(short n, short m, java.math.BigDecimal value)
	{
_delegate.set(n,m,value);
	}

	public java.math.BigDecimal get(short n, short m)
	{
		return _delegate.get(n,m);
	}

	public short width()
	{
		return _delegate.width();
	}

}
