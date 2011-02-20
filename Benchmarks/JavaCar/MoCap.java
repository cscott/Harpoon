import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class MoCap extends JFrame{
    public static int WIDTH=500;
    public static int HEIGHT=300;
    public static long time = System.currentTimeMillis();
    public MoCap (String a,final PrintWriter p,String ip) {
	super(a);
        getContentPane().setLayout(new GridLayout(5 ,3));
	JButton con = new JButton("Connect");
	final JTextField u = new JTextField();
        final IPaqServoController foo = new IPaqServoController(ip); 
	con.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    foo.move(1,126);
		    foo.move(2,128);
		    p.flush();
		    MoCap.time = System.currentTimeMillis();
		}
	    });
	con.addKeyListener(new KeyListener(){
		public void keyPressed(KeyEvent e) {
		    long t1;
		    if (e.getKeyCode()==38) {
			foo.move(2,150); 
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("u");
			p.flush();
			MoCap.time = t1;
		    } else if (e.getKeyCode()==40) {
			foo.move(2,110);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("d");
			p.flush();
			MoCap.time = t1;
		    } else if(e.getKeyCode()==39) {
			foo.move(1,255);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("r");
			p.flush();
			MoCap.time = t1;
		    } else if(e.getKeyCode()==37) {
			foo.move(1,1);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("l");
			p.flush();
			MoCap.time = t1;
		    }
		}
		public void keyReleased(KeyEvent e) {
		    long t1;
		    if (e.getKeyCode()==38) {
			foo.move(2,128);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("u1");
			p.flush();
			MoCap.time = t1;
		    } else if (e.getKeyCode()==40) {
			foo.move(2,128);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("d1");
			p.flush();
			MoCap.time = t1;
		    } else if (e.getKeyCode()==39) {
			foo.move(1,126);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("r1");
			p.flush();
			MoCap.time = t1;
		    } else if (e.getKeyCode()==37) {
			foo.move(1,126);
			p.println((t1 = System.currentTimeMillis()) - MoCap.time);
			p.println("l1");
			p.flush();
			MoCap.time = t1;
		    }
		}
		public void keyTyped(KeyEvent e) {
		    System.out.println("Use the UP,DOWN,LEFT,RIGHT keys to move the car");
		}
	    });
	getContentPane().add(con);
	getContentPane().add(u);
	
	
    }
    
    public static void main(String s[]){
	PrintWriter p = null;
	try {
	    FileOutputStream fis = new FileOutputStream(s[0]);
	    p = new PrintWriter(fis);
	}catch (IOException e){System.out.println("Error-"+e.toString());
	System.exit(-1);
	}
	
	MoCap frame = new MoCap("Connection info",p,s[1]);
	frame.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e) {
		    System.exit(0);}
	    });
	frame.setSize(WIDTH,HEIGHT);
	frame.show();
    }
}
