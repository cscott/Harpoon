import java.util.*;
import java.net.*;

public class Temp extends Thread{
    
    static URL class_path[];
    static ClassLoader cc;

    public static void main(String args[]){
	createClassPathURLs();
	Thread t = new Temp();
	t.setContextClassLoader(cc = new URLClassLoader(class_path, null));
	System.out.println("thread: " + t);
	t.start();
    }

    private static void createClassPathURLs()
    {
        if( class_path == null )
        {
            //set both orb properties, might have already
            //been done by the jaco script
            System.setProperty( "org.omg.CORBA.ORBClass",
                                "org.jacorb.orb.ORB" );
            System.setProperty( "org.omg.CORBA.ORBSingletonClass",
                                "org.jacorb.orb.ORBSingleton" );

            StringTokenizer tok = 
                new StringTokenizer( System.getProperty( "java.class.path" ),
                                     System.getProperty( "path.separator" ));

            Vector v = new Vector();

            while( tok.hasMoreTokens() )
            {
                v.add( tok.nextToken() );
            }
            
            class_path = new URL[ v.size() ];

            //bring raw classpathes into url format
            for( int i = 0; i < v.size(); i++ )
            {
                String s = (String) v.get( i );
		//System.out.println("token: " + s);
                //dos to unix
                s = s.replace( '\\', '/' );

                if( s.startsWith( "/" ))
                {
                    //unix path
                    s = "file:" + s;
                }
                else
                {
                    //dos path
                    s = "file:/" + s;
                }
                
                if(! (s.endsWith( ".zip" ) ||
                      s.endsWith( ".jar" )))
                {
                    s = s + "/";
                }

                try
                {                
		    //System.out.println("add: " + s);
                    class_path[i] = new URL( s );                    
                }
                catch( MalformedURLException e )
                {
                    //this is not supposed to happen
                    e.printStackTrace();
                }
            }
        }
    }


    public void run(){
	try{
	    ClassLoader cl = ClassLoader.getSystemClassLoader();
	    System.out.println("cl " + cl);
	    System.out.println("cc == cl: " + (cc == cl));
	    cl = cc;
	    URL []classpath = ((URLClassLoader)cl).getURLs();
	    System.out.println("cl " + cl);
	    for(int i = 0; i < classpath.length; i++){
		System.out.println(classpath[i].getPath());
	    }
	    System.out.println("loaded: " + cl.loadClass("java.util.LinkedList"));
	    System.out.println("loaded: " + cl.loadClass("com.sun.media.jai.codec.ByteArraySeekableStream"));
	    Object o = new com.sun.media.jai.codec.ByteArraySeekableStream(null);
	    System.out.println(o);
	}catch(java.io.IOException e){
	    System.out.println("unexpected");
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
