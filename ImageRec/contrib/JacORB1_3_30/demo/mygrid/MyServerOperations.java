package demo.mygrid;

/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public interface MyServerOperations
{
	short height();
	short width();
	void set(short n, short m, java.math.BigDecimal value);
	java.math.BigDecimal get(short n, short m);
	short opWithException() throws demo.mygrid.MyServerPackage.MyException;
}
