import java.lang.*;
import java.io.*;
import java.net.*;

class clientSocketTest {

  Socket            sock;
  DataInputStream   getS;
  PrintStream       putS;  //caution: using autoflush mode

  // コンストラクタ
  public clientSocketTest(String host, int port) {
    try {
      sock = new Socket(host, port);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  public void run() {
    try {
      getS = new DataInputStream(
               new BufferedInputStream(sock.getInputStream())
             );
      putS = new PrintStream(
               new BufferedOutputStream(sock.getOutputStream()),
               true  // for autoflush buffer
             );

      //-----------------------------------
      // chat


      for(int i=0;i<10000;i++){

        // ソケットから標準出力へ
        String s;
        while( !(s = new String(getS.readLine())).equals("over.") ){
          System.out.println("server: "+s);
        }
        System.out.println("");

        // 標準入力からソケットへ
        int n;
        byte buf[] = new byte[2048];
        n = System.in.read( buf );
        putS.write( buf,0,n );
        putS.println("over.");
      }

      getS.close();
      putS.close();
      sock.close();
      System.out.println("close");

    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

  }

  public static void main(String args[]) {
    if (args.length != 2) {
      System.err.println("usage: java clientSocketTest host port");
      System.exit(-1);
    }

    clientSocketTest i = new clientSocketTest(
                           args[0],
                           (new Integer(args[1])).intValue() 
                         );
    i.run();
  }
}



