import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class JavaCar extends JFrame{
    public static int WIDTH=500;
    public static int HEIGHT=300;
    public static boolean p;
    public JavaCar (String a, String ip) {
	super(a);
        getContentPane().setLayout(new GridLayout(5 ,3));
	JButton con = new JButton("Connect");
	final JTextField u = new JTextField();
        final IPaqServoController foo = new IPaqServoController(ip); 
        	con.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    foo.move(1,126);
		    foo.move(2,128);
		}
	    });
	con.addKeyListener(new KeyListener(){
	public void keyPressed(KeyEvent e) {
	    if (e.getKeyCode()==38) {foo.move(2,255); foo.moveRelative(2,-55);}else{
		if (e.getKeyCode()==40) {foo.move(2,1); foo.moveRelative(2,55);}else{
		    if(e.getKeyCode()==39) {foo.move(1,200);}else{
			if(e.getKeyCode()==37) {foo.move(1,56);}
		    }
		}
		
	    }
	}
       public void keyReleased(KeyEvent e) {
	    if (e.getKeyCode()==38) {foo.move(2,128);}else{
		if(e.getKeyCode()==40) {foo.move(2,128);}else{
		    if(e.getKeyCode()==39){foo.move(1,126);}else{
		      	if(e.getKeyCode()==37){foo.move(1,126);}
		    }
		}
	    }
       }
       public void keyTyped(KeyEvent e){
	   System.out.println("Use the UP,DOWN,LEFT,RIGHT keys to move the car");
       }
	    });
	getContentPane().add(con);
	getContentPane().add(u);
	
	
    }
    
    public static void main(String s[]){
	JavaCar frame = new JavaCar("Connection info", s[0]);
	frame.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e) {
		    System.exit(0);}
	    });
	frame.setSize(WIDTH,HEIGHT);
	frame.show();
    }
}
