// Display.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 {@link Display} displays the images from the in-node on the screen. Upon
 creation, you may specify which (if not all) color channels should be displayed
 from the images.<br><br>

 Multiple {@link Display}s will automatically position themselves on
 your screen so as not to block each other.
 
 @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Display extends Node {

    /**
       Indicates whether the red color channel of images should be displayed.
    */
    private boolean displayRed;
    /**
       Indicates whether the green color channel of images should be displayed.
    */
    private boolean displayGreen;
    /**
       Indicates whether the blue color channel of images should be displayed.
    */
    private boolean displayBlue;

    /**
       The width of all new windows.
    */
    public static int defaultWidth = 133;
    /**
       The height of all new windows.
    */
    public static int defaultHeight = 100;
    /**
       The horizonal padding between all new windows.
    */
    public static int defaultXPadding = 5;
    /**
       The vertical padding between all new windows.
    */
    public static int defaultYPadding = 5;

    /**
       The number of windows that should be displayed in a single row.
    */
    public static int numAcross = 3;
    /**
       The row position of the next new window to be displayed.
    */
    private static int currentNumAcross = 0;

    /**
       The physical horizontal screen location of the next new window to be displayed.
    */
    private static int currentX = 0;
    
    /**
       The physical vertical screen location of the next new window to be displayed.
    */
    private static int currentY = 0;

    /**
       The {@link Frame} in which images passed to this {@link Display} node are
       displayed.
    */
    private Frame frame;
    /**
       The {@link BufferedImage} which provides the {@link WritableRaster} onto which
       image data is written.
    */
    private BufferedImage image = 
	new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    /**
       The title of the display frame.
    */
    private String title;

    /** Inner class that represents the actual canvas that the image is painted on. */
    class ImageCanvas extends Canvas {
	ImageCanvas() { super(); }
	public void paint(Graphics g) {
	    g.drawImage(Display.this.image, 0, 0, getWidth(), getHeight(), this);
	}
	public void update(Graphics g) {
	    paint(g);
	}
    }

    private ImageCanvas canvas = new ImageCanvas();

    /** Construct a {@link Display} node, given a <code>title</code> for the window. 
     *  Note that this actually opens a frame visible on the screen. By default,
     *  all color channels are displayed.
     *  @param The title of the window that will be opened.
     */
    public Display(String title) {
	super();
	init(title, true, true, true);
    }

    /**
       Construct a {@link Display} node, given a <code>title</code> for the window,
       which displays only the color channels specified.
       @param title The title of the window that will be opened.
       @param displayRed Specifies whether the red color channel should be displayed.
       @param displayGreen Specifies whether the green color channel should be displayed.
       @param displayBlue Specifies whether the blue color channel should be displayed.
     */
    public Display(String title, boolean displayRed, boolean displayGreen, boolean displayBlue) {
	super();
	init(title, displayRed, displayGreen, displayBlue);
    }

    /**
       Method that should be called by all constructors to initialize object fields.
    */
    private void init(String title, boolean displayRed, boolean displayGreen, boolean displayBlue) {
	this.title = title;
	frame = new Frame(title);
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
	    }
	});
	frame.setLayout(new BorderLayout());
	frame.add(canvas, BorderLayout.CENTER);
	frame.setSize(new Dimension(defaultWidth, defaultHeight));
	frame.setLocation(currentX, currentY);
	currentX += defaultWidth + defaultXPadding;
	currentNumAcross++;
	if (currentNumAcross == 3) {
	    currentNumAcross = 0;
	    currentX = 0;
	    currentY += defaultHeight + defaultYPadding;
	}
	frame.setVisible(true);
	
	this.displayRed = displayRed;
	this.displayGreen = displayGreen;
	this.displayBlue = displayBlue;
    }

    /** Display the image represented by <code>id</code>, usually called by the in-node. */
    public synchronized void process(ImageData id) {
	//System.out.println("DISPLAY: displaying "+title);
	if (frame.isVisible()) {
	    BufferedImage newImage;
	    if (image.getWidth() != id.width || image.getHeight() != id.height)
		newImage = new BufferedImage(id.width, id.height, 
					     BufferedImage.TYPE_INT_RGB);
	    else
		newImage = image;
	    WritableRaster raster = newImage.getRaster();
	    
	    /* Horribly inefficient - find a better way... */
	    int[] vals = new int[id.rvals.length];
	    int temp = 0;
	    if (displayRed) {
		for (int i=0; i<id.rvals.length; i++) vals[i]=(id.rvals[i]|256)&255;
		raster.setSamples(0,0,id.width,id.height,0,vals);
	    }
	    if (displayGreen) {
		for (int i=0; i<id.gvals.length; i++) vals[i]=(id.gvals[i]|256)&255;       
		raster.setSamples(0,0,id.width,id.height,1,vals);
	    }
	    if (displayBlue) {
		for (int i=0; i<id.bvals.length; i++) vals[i]=(id.bvals[i]|256)&255;
		raster.setSamples(0,0,id.width,id.height,2,vals);
	    }
	    image = newImage;
	    canvas.repaint();
	}
	super.process(id);
    }
    
    /**
       Sets the size of all subsequently created windows.
       @param width The new window width.
       @param height The new window height.
    */
    public static void setDefaultSize(int width, int height) {
	defaultWidth = width;
	defaultHeight = height;
    }
}
