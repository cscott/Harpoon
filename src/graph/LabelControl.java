// LabelControl.java, created by benster 6/1/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

public class LabelControl extends Node {
    private Label myLabel;

    private static final int MAX_TIME = 3000;
    private static final int MIN_TIME = 0;
    private static final int DEFAULT_TIME = 2000;
    private static final int DELTA_TIME = 100;



    public LabelControl(Label l) {
	super(null);
	init(l);
    }

    private void init(Label l) {
	myLabel = l;
	myLabel.setTimeBetweenSends(DEFAULT_TIME);
    }

    public void process(ImageData id) {
	long currentTime = myLabel.getTimeBetweenSends();
	switch (id.command) {
	case (Command.TIME_DOWN): {
	    currentTime -= DELTA_TIME;
	    if (currentTime < MIN_TIME)
		currentTime = MIN_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	    break;
	}
	case (Command.TIME_UP): {
	    currentTime += DELTA_TIME;
	    if (currentTime > MAX_TIME)
		currentTime = MAX_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	}
	case (Command.SET_TIME): {
	    currentTime = id.time;
	    if (currentTime > MAX_TIME)
		currentTime = MAX_TIME;
	    if (currentTime < MIN_TIME)
		currentTime = MIN_TIME;
	    System.out.println("LabelControl: Time="+currentTime);
	    myLabel.setTimeBetweenSends(currentTime);
	    break;
	}
	default: {
	    System.out.println("LabelControl: Unrecognized command");
	}
	}
	super.process(id);
    }
}
