// CODEC.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import imagerec.graph.ImageData;

/** {@link CODEC} is an interface for compression and decompression of
 *  {@link ImageData}s that are sent across the network.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public interface CODEC {

    /** Compress an {@link ImageData}.
     *
     *  @param id The {@link ImageData} to compress.
     *  @return The compressed {@link ImageData}.
     */
    public ImageData compress(ImageData id);

    /** Decompress an {@link ImageData}.
     * 
     *  @param id The {@link ImageData} to decompress.
     *  @return The decompressed {@link ImageData}.
     */
    public ImageData decompress(ImageData id);
}
