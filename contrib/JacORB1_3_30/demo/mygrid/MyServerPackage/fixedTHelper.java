package demo.mygrid.MyServerPackage;
/**
 *	Generated from IDL definition of alias "fixedT"
 *	@author JacORB IDL compiler 
 */

public class fixedTHelper
{
	private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_alias_tc( demo.mygrid.MyServerPackage.fixedTHelper.id(),"fixedT",org.omg.CORBA.ORB.init().create_fixed_tc((short)5,(short)2));
	public fixedTHelper ()
	{
	}
	public static void insert(org.omg.CORBA.Any any, java.math.BigDecimal s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}
	public static java.math.BigDecimal extract(org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}
	public static org.omg.CORBA.TypeCode type()
	{
		return _type;
	}
	public static String id()
	{
		return "IDL:demo/mygrid/MyServer/fixedT:1.0";
	}
	public static java.math.BigDecimal read(org.omg.CORBA.portable.InputStream _in)
	{
		java.math.BigDecimal _result;
				java.math.BigDecimal _fixed8814217=_in.read_fixed();
		_result = _fixed8814217.movePointLeft(2);

		return _result;
	}
	public static void write(org.omg.CORBA.portable.OutputStream _out, java.math.BigDecimal _s)
	{
		
		if(_s.scale() != 2)
			throw new org.omg.CORBA.DATA_CONVERSION("wrong scale in fixed point value, expecting 2, got " + _s.scale() );
		java.math.BigDecimal _max8814217= new java.math.BigDecimal("1000");
		if( _s.compareTo(_max8814217 ) != -1)
			throw new org.omg.CORBA.DATA_CONVERSION("more than 5 digits in fixed point value");
		_out.write_fixed(_s);

	}
}
