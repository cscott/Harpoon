package demo.mygrid;

/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public abstract class MyServerPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, demo.mygrid.MyServerOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "_get_height", new java.lang.Integer(0));
		m_opsHash.put ( "opWithException", new java.lang.Integer(1));
		m_opsHash.put ( "set", new java.lang.Integer(2));
		m_opsHash.put ( "get", new java.lang.Integer(3));
		m_opsHash.put ( "_get_width", new java.lang.Integer(4));
	}
	private String[] ids = {"IDL:demo/mygrid/MyServer:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public demo.mygrid.MyServer _this()
	{
		return demo.mygrid.MyServerHelper.narrow(_this_object());
	}
	public demo.mygrid.MyServer _this(org.omg.CORBA.ORB orb)
	{
		return demo.mygrid.MyServerHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // _get_height
			{
			_out = handler.createReply();
			_out.write_short(height());
				break;
			}
			case 1: // opWithException
			{
			try
			{
				_out = handler.createReply();
				_out.write_short(opWithException());
			}
			catch(demo.mygrid.MyServerPackage.MyException _ex0)
			{
				_out = handler.createExceptionReply();
				demo.mygrid.MyServerPackage.MyExceptionHelper.write(_out, _ex0);
			}
				break;
			}
			case 2: // set
			{
				short _arg0=_input.read_short();
				short _arg1=_input.read_short();
				java.math.BigDecimal _arg2=demo.mygrid.MyServerPackage.fixedTHelper.read(_input);
				_out = handler.createReply();
				set(_arg0,_arg1,_arg2);
				break;
			}
			case 3: // get
			{
				short _arg0=_input.read_short();
				short _arg1=_input.read_short();
				_out = handler.createReply();
				demo.mygrid.MyServerPackage.fixedTHelper.write(_out,get(_arg0,_arg1));
				break;
			}
			case 4: // _get_width
			{
			_out = handler.createReply();
			_out.write_short(width());
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
