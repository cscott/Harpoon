package demo.stringgrid;

/**
 * A very simple implementation of a 2-D grid
 */

import demo.stringgrid.MyServerPackage.MyException;


public class gridImpl  
    extends MyServerPOA
{
    protected short height = 31;
    protected short width = 14;
    protected java.lang.String[][] mygrid;
 
    public gridImpl()
    {
        mygrid = new java.lang.String[height][width];
        for( short h = 0; h < height; h++ )
        {
            for( short w = 0; w < width; w++ )
            {
                mygrid[h][w] = new java.lang.String("0.21");
            }
        }
    }

    public java.lang.String get(short n, short m)
    {
        if( ( n <= height ) && ( m <= width ) )
            return mygrid[n][m];
        else
            return null;
    }

    public short height()
    {
        return height;
    }

    public void set(short n, short m, java.lang.String value)
    {
        if( ( n <= height ) && ( m <= width ) )
            mygrid[n][m] = value;
    }

    public short width()
    {
        return width;
    }

    public short opWithException()
        throws demo.stringgrid.MyServerPackage.MyException
    {
        throw new demo.stringgrid.MyServerPackage.MyException("This is only a test exception, no harm done :-)");
    }



}


