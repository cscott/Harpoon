package demo.grid.MyServerPackage;
/**
 *	Generated from IDL definition of alias "fixedT"
 *	@author JacORB IDL compiler 
 */

final public class fixedTHolder
	implements org.omg.CORBA.portable.Streamable
{
	public java.math.BigDecimal value;

	public fixedTHolder ()
	{
	}
	public fixedTHolder (java.math.BigDecimal initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return fixedTHelper.type();
	}
	public void _read(org.omg.CORBA.portable.InputStream in)
	{
		value = fixedTHelper.read(in);
	}
	public void _write(org.omg.CORBA.portable.OutputStream out)
	{
		fixedTHelper.write(out,value);
	}
}
