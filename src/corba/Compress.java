// Compress.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import imagerec.graph.ImageData;
import imagerec.util.CODEC;

/** {@link Compress} is a {@link CommunicationsModel} that wraps a {@link CommunicationsModel} 
 *  and uses a {@link CODEC} to compress and decompress information sent over the network.
 *
 *  Thus {@link Compress} allows any {@link CODEC} to be used with any {@link CommunicationsModel}.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Compress implements CommunicationsModel {
    private final CommunicationsModel comm;
    private final CODEC codec;

    /** Construct a new {@link Compress} {@link CommunicationsModel}
     *
     *  @param comm The {@link CommunicationsModel} to wrap.
     *  @param codec The {@link CODEC} to perform network compression/decompression.
     */
    public Compress(CommunicationsModel comm, CODEC codec) {
	this.comm = comm;
	this.codec = codec;
    }

    /** Uses the {@link CODEC} to compress information using 
     *  this {@link CommunicationsAdapter}.
     */
    private CommunicationsAdapter compress(final CommunicationsAdapter ca) {
	return new CommunicationsAdapter() {
	    public void process(ImageData id) {
		ca.process(codec.compress(id));
	    }

	    public void alert(float c1, float c2, float c3, long time) {
		ca.alert(c1, c2, c3, time);
	    }
	};
    }

    /** Uses the {@link CODEC} to decompress information using 
     *  this {@link CommunicationsAdapter}.
     */
    private CommunicationsAdapter decompress(final CommunicationsAdapter ca) {
	return new CommunicationsAdapter() {
	    public void process(ImageData id) {
		ca.process(codec.decompress(id));
	    }
	    
	    public void alert(float c1, float c2, float c3, long time) {
		ca.alert(c1, c2, c3, time);
	    }
	};
    }

    /**
     *  Wraps {@link CommunicationsModel} <code>setupIDClient</code>.
     */
    public CommunicationsAdapter setupIDClient(String name) 
	throws Exception {
	return compress(comm.setupIDClient(name));
    }

    /**
     *  Wraps {@link CommunicationsModel} <code>runIDServer</code>.
     */
    public void runIDServer(String name, final CommunicationsAdapter out) 
	throws Exception {
	comm.runIDServer(name, decompress(out));
    }   

    /**
     *  Wraps {@link CommunicationsModel} <code>setupAlertClient</code>.
     */
    public CommunicationsAdapter setupAlertClient(String name) 
	throws Exception {
	return compress(comm.setupAlertClient(name));
    }

    /**
     *  Wraps {@link CommunicationsModel} <code>runAlertServer</code>.
     */
    public void runAlertServer(String name, CommunicationsAdapter out) 
	throws Exception {
	comm.runAlertServer(name, decompress(out));
    }

    /**
     *  Wraps {@link CommunicationsModel} <code>setupATRClient</code>.
     */
    public CommunicationsAdapter setupATRClient(String name)
	throws Exception {
	return compress(comm.setupATRClient(name));
    }

    /**
     *  Wraps {@link CommunicationsModel} <code>runATRServer</code>.
     */
    public void runATRServer(String name, CommunicationsAdapter out) 
	throws Exception {
	comm.runATRServer(name, decompress(out));
    }
}
