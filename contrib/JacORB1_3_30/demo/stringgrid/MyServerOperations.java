package demo.stringgrid;

/**
 *	Generated from IDL definition of interface "MyServer"
 *	@author JacORB IDL compiler 
 */

public interface MyServerOperations
{
	short height();
	short width();
	void set(short n, short m, java.lang.String value);
	java.lang.String get(short n, short m);
	short opWithException() throws demo.stringgrid.MyServerPackage.MyException;
}
