// CarDemoGroundMain.java, created by benster 5/27/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;

import imagerec.graph.*;

import imagerec.util.RunLength;
import imagerec.util.ImageDataManip;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.util.StringTokenizer;

//import imagerec.corba.CORBA;
import imagerec.corba.Sockets;

public class CarDemoGroundMain {
    /** Ground component of the two-component version of the ATR.
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {	
	/*
	if (args.length == 0){
	    System.out.println("Usage: java -jar carDemoGroundATR.jar <pipeline #>");
	    System.out.println("       <compress|nocompress>");
	    System.out.println("       <CORBA name for ground server>");
	    System.out.println("       <CORBA name for ground client>");
	    System.out.println("       (CORBA options)");
	    System.out.println("");
	    System.out.println("  The compress option must match that of carDemoEmbeddedATR.jar");
	    System.out.println("  If compression is turned on, the ground ATR will receive");
	    System.out.println("  outlines of objects instead of full color images.");
	    System.exit(-1);
	}
	*/

	if (args.length == 0){
	    System.out.println("Usage: java -jar carDemoGroundATR.jar <pipeline #>");
	    System.out.println("       <compress|nocompress>");
	    System.out.println("       <server port #>");
	    System.out.println("       <client port #>");
	    System.out.println("       <camera client port #>");
	    System.out.println("       <label client port #>");
	    System.out.println("       <car client port #>");
	    System.out.println("       <default filename|'none'>");
	    System.out.println("");
	    System.out.println("  The compress option must match that of carDemoEmbeddedATR.jar");
	    System.out.println("  If compression is turned on, the ground ATR will receive");
	    System.out.println("  outlines of objects instead of full color images.");
	    System.exit(-1);
	}


	int pipelineNumber = 0;
	try {
	    pipelineNumber = Integer.parseInt(args[0]);
	}
	catch (NumberFormatException nfe) {
	    System.out.println("Error: Pipeline argument was not a valid integer.");
	    System.exit(-1);
	}
	
	if (pipelineNumber <= 0) {
	    System.out.println("Error: Pipeline # must be > 0 (value was '"+pipelineNumber+")");
	    System.exit(-1);
	}
	else {
	    if (pipelineNumber == 1) {
		System.out.println("**** THIS PIPELINE IS DEPRECATED *****");
		System.exit(-1);
		Node pipe;
		Server embedToGrServer = new Server(new Sockets(), args[2], null);
		//Server embedToGrServer = new Server(new CORBA(args), args[2], null);

		Node ifNotThen = new IfNotThen("ObjectIsTank");
		HumanRecognition human = new HumanRecognition();
		
		//human.setCommonMemory("ObjectIsTank");
		Node goRtCmd = new Command(Command.GO_RIGHT, null);
		Node strip = new Strip();
		System.out.println("Just about to look for client connection");
		Node grToEmbedClient = new Client(new Sockets(), args[3]);
		//Node grToEmbedClient = new Client(new CORBA(args), args[3]);
		System.out.println("Just got client connection");
		//Node pause = new Pause(-1, 1, null);
		Node pause = new Node();
		
		pipe = embedToGrServer.link(ifNotThen.link(human.linkL(strip.linkL(grToEmbedClient)),
							   goRtCmd.linkL(strip)),
					    pause);
		
		//if compression is selected,
		//
		boolean willCompress = args[1].equalsIgnoreCase("compress");
		if (willCompress) {
		    Node decompress = new Decompress(new RunLength(), null);
		    embedToGrServer.linkL(decompress.linkL(ifNotThen));
		}
		System.out.println("About to run embedToGroundServer");
		Thread t = new Thread(pipe);
		t.start(); //calls pipe.run()

	    }
	    else if (pipelineNumber == 2) {
		HumanRecognition human = new HumanRecognition();
		LatencySlider slider = new LatencySlider(human, 500);


		Node pipe;
		Server embedToGrServer = new Server(new Sockets(), args[2], null);
		Node grToEmbedClient = new Client(new Sockets(), args[3]);
		pipe = embedToGrServer.linkL(human.linkL(grToEmbedClient));

		boolean willCompress = args[1].equalsIgnoreCase("compress");
		if (willCompress) {
		    Node decompress = new Decompress(new RunLength(), null);
		    embedToGrServer.linkL(decompress.linkL(human));
		}
		Thread t = new Thread(pipe);
		t.start(); //calls pipe.run()

		
		System.out.println("*** Running camera Client on port #"+args[4]+"***");
		Node cameraClient = new Client(new Sockets(), args[4]);
		Node cameraControlKeyboard = new CameraControlKeyboard(cameraClient);
		Thread t2 = new Thread(cameraControlKeyboard);
		t2.start();
		

      		System.out.println("*** Running label Client on port #"+args[5]+"***");
		Node labelClient = new Client(new Sockets(), args[5]);
		Node labelControlKeyboard = new LabelControlKeyboard(labelClient);
		Thread t3 = new Thread(labelControlKeyboard);
		t3.start();
		
		System.out.println("*** Running car Client on port #"+args[6]+"***");
		Node carClient = new Client(new Sockets(), args[6]);
		Node carControlKeyboard = new CarControlKeyboard(carClient);
		Thread t4 = new Thread(carControlKeyboard);
		t4.start();

		System.out.println("Waiting for 5 seconds");
		try {
		    Thread.currentThread().sleep(5000);
		}
		catch (InterruptedException e) {
		}

		String filename = args[7];
		System.out.println("Filename: "+filename);
		if (!filename.equalsIgnoreCase("none")) {
		    try {
			FileInputStream reader = new FileInputStream(new File(filename));
			System.out.println("Opened file '"+filename+"' for reading...");
			int c = reader.read();
			String s = "";
			while (c != -1) {
			    s += (char)c;
			    c = reader.read();
			}
			System.out.println("File read.");
			StringTokenizer st = new StringTokenizer(s);
			
			while (st.hasMoreTokens()) {
			    String token = st.nextToken();
			    if (token.equalsIgnoreCase("LabelDelay")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				labelClient.process(ImageDataManip.create(Command.SET_TIME, value));
				System.out.println("Set Label value to "+value);
			    }
			    else if (token.equalsIgnoreCase("Brightness")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				cameraClient.process(ImageDataManip.create(Command.SET_BRIGHTNESS, value));
				System.out.println("Set Camera brightness to "+value);
			    }
			    else if (token.equalsIgnoreCase("Contrast")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				cameraClient.process(ImageDataManip.create(Command.SET_CONTRAST, value));
				System.out.println("Set Camera contrast to "+value);
			    }
			    else if (token.equalsIgnoreCase("Gain")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				cameraClient.process(ImageDataManip.create(Command.SET_GAIN, value));
				System.out.println("Set Camera gain to "+value);
			    }
			    else if (token.equalsIgnoreCase("FrameRate")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				cameraClient.process(ImageDataManip.create(Command.SET_FRAME_RATE, value));
				System.out.println("Set Camera frame rate to "+value);
			    }
			    else if (token.equalsIgnoreCase("InterruptDelay")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				carClient.process(ImageDataManip.create(Command.SET_INTERRUPT_TIME, value));
				System.out.println("Set Car interrupt time to "+value);
			    }
			    else if (token.equalsIgnoreCase("ForwardSpeed")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				carClient.process(ImageDataManip.create(Command.SET_FORWARD_SPEED, value));
				System.out.println("Set Car forward speed to "+value);
			    }
			    else if (token.equalsIgnoreCase("BackwardSpeed")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				carClient.process(ImageDataManip.create(Command.SET_BACKWARD_SPEED, value));
				System.out.println("Set Car backward speed to "+value);
			    }
			    else if (token.equalsIgnoreCase("Turn")) {
				token = st.nextToken();
				int value = Integer.parseInt(token);
				carClient.process(ImageDataManip.create(Command.SET_TURN_AMOUNT, value));
				System.out.println("Set Car turn amount to "+value);
			    }
			    else {
				System.out.println("Unrecognized token: "+token);
			    }
			} //[while (st.hasMoreTokens())]
		    }//[try]
		    catch (FileNotFoundException e) {
			System.out.println("********* File '"+filename+"' not found! *******");
			System.out.println("  ******* Not loading defaults ********");
		    }
		    catch (IOException e2) {
			System.out.println("******* Error while reading '"+filename+"'.");
			System.out.println(" **** Stopping default loading ******");
		    }
		} //[if does not equal "none"]
		else {
		    System.out.println("No default file loaded.");
		}
	    }
	    else {
		System.out.println("Error: Pipeline #"+pipelineNumber+" not implemented yet.");
		System.exit(-1);
		
	    }
	}//else [if(pipelineNumber <= 0)]
    }//public static void main()
    
    private static class LatencySlider extends JFrame implements ChangeListener {
	VariableLatency v;
	LatencySlider(VariableLatency v, int initialLatency) {
	    super("Adjust Latency");
	    this.v = v;
	    v.setLatency(initialLatency);
	    Container c = this.getContentPane();
	    c.setLayout(new BorderLayout());
	    c.add(new JLabel("<html><center>Adjust the approximate latency<br>of the ground ATR (ms):</center></html>"), BorderLayout.NORTH);
	    JSlider slider = new JSlider(250, 2000, initialLatency);
	    slider.addChangeListener(this);
	    slider.setMajorTickSpacing(250);
	    slider.setMinorTickSpacing(50);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(true);
	    slider.setPreferredSize(new Dimension(500, 100));
	    
	    c.add(slider, BorderLayout.CENTER);
	    this.pack();
	    this.setVisible(true);
	}

	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    int value = source.getValue() - 250;
	    v.setLatency(value);
	}
    }
    
}//public class CarDemoGroundMain
