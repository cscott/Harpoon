// RunLength.java, created by Amerson Lin
// Copyright (C) 2003 Amerson H. Lin <amerson@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.


package imagerec.util;
import imagerec.graph.ImageData;
import java.util.ArrayList;
 
/* {@link RunLength} implements the {@link CODEC} interface for compressing and decompresing ImageData objects

   This CODEC performs run-length encoding:

   Compresses using the following idea:
   A sequence of [1 2 2 2 2 5 5 5 3 3]
   is encoded as [1 1 2 4 5 3 3 2]

   Sequence of [1 2 4 5 2 2]
   is decoded as [1 1 4 4 4 4 4 2 2]

   Used by {@link Compress} & {@link Decompress}  
 @author Amerson Lin <amerson@mit.edu>
*/

public class RunLength implements CODEC {

  ImageData id;

  /* Constructs a {@link RunLength} {@link CODEC} that does run-length encoding
   */
  public void RunLength() {
    id = null;
  }

  /* Compresses an ImageData
    @param image  //The {@link ImageData} to compress
    @return Returns an ImageData object that has the encoded sequence. Sequence might be longer than the original seqeunce depending on the image
  */
  public ImageData compress(ImageData image){
    id = image;

    byte[] green = id.gvals;
    int glength = green.length;
    byte[] red  = id.rvals;
    int rlength = red.length;
    byte[] blue = id.bvals;
    int blength = blue.length;
    ArrayList gIntPair = new ArrayList();
    ArrayList rIntPair = new ArrayList();
    ArrayList bIntPair = new ArrayList();
    int num = 0;
    
    
    // GREEN
    // first step is to convert into an int arrray
    System.out.println("Original number of gvals: " + glength);
    int gcount = 1;
    int gcurr = 0;
    int gprev = ((green[0]|256)&255);
    for (int i=1; i < glength ; i++) {
      gcurr = ((green[i]|256)&255);
      if (gcurr == gprev) {
        // increment rcount is the current value is the same as the previous
         if (gcount > 250) {
          gIntPair.add(new Integer(gprev));
          gIntPair.add(new Integer(gcount));
          gcount = 1;
        } else {
          gcount++ ; }
      }
      else {
        gIntPair.add(new Integer(gprev));
        gIntPair.add(new Integer(gcount));
        gprev = gcurr;
        gcount = 1;
      }
    }
    if (gcount>0) {
      gIntPair.add(new Integer(gprev));
      gIntPair.add(new Integer(gcount));
    }
    // ARRAYLIST -> byte[]
    byte[] newgreen = new byte[gIntPair.size()];
    for (int j=0 ; j < gIntPair.size() ; j++){
      num = ((Integer)gIntPair.get(j)).intValue();
      newgreen[j] = (byte)num;
      
    }
    id.gvals=  newgreen;
    // end of green value run-length encoding

   
    // RED
    // first step is to convert into an int arrray
    int rcount = 1;
    int rcurr = 0;
    int rprev = ((red[0]|256)&255);
    for (int i=1; i < rlength ; i++) {
      rcurr = ((red[i]|256)&255);
      if (rcurr == rprev) {
        // increment rcount is the current value is the same as the previous
         if (rcount > 250) {
          rIntPair.add(new Integer(rprev));
          rIntPair.add(new Integer(rcount));
          rcount = 1;
        } else {
          rcount++ ; }
      }

      else {
        rIntPair.add(new Integer(rprev));
        rIntPair.add(new Integer(rcount));
        rprev = rcurr;
        rcount = 1;
      }
    }
    if (rcount>0) {
      rIntPair.add(new Integer(rprev));
      rIntPair.add(new Integer(rcount));
    }
    // ARRAYLIST -> byte[]
    byte[] newred = new byte[rIntPair.size()];
    for (int j=0 ; j < rIntPair.size() ; j++){
      num = ((Integer)rIntPair.get(j)).intValue();
      newred[j] = (byte)num;
      
    }
    id.rvals=  newred;
    // end of green value run-length encoding


    //BLUE
    // first step is to convert into an int arrray
    int bcount = 1;
    int bcurr = 0;
    int bprev = ((blue[0]|256)&255);
    for (int i=1; i < blength ; i++) {
      bcurr = ((blue[i]|256)&255);
      if (bcurr == bprev) {
        // increment bcount is the current value is the same as the previous
         if (bcount > 250) {
          bIntPair.add(new Integer(bprev));
          bIntPair.add(new Integer(bcount));
          bcount = 1;
        } else {
          bcount++ ; }
      }

      else {
        bIntPair.add(new Integer(bprev));
        bIntPair.add(new Integer(bcount));
        bprev = bcurr;
        bcount = 1;
      }
    }
    if (bcount>0) {
      bIntPair.add(new Integer(bprev));
      bIntPair.add(new Integer(bcount));
    }
    
    // ARRAYLIST -> byte[]
    byte[] newblue = new byte[bIntPair.size()];
    for (int j=0 ; j < bIntPair.size() ; j++){
      num = ((Integer)bIntPair.get(j)).intValue();
      newblue[j] = (byte)num;
      
    }
    id.bvals=  newblue;
    // end of green value run-length encoding
    

    return id;
  }



  /* Decompresses an ImageData 
  @param image //the {@link ImageData} that contains a run-length encoding sequence
  @return Returns an ImageData that representing the original image before encoding
  */
  public ImageData decompress(ImageData image){
    id = image;
    byte[] green = id.gvals;
    int glength = (green.length)/2;
    System.out.println("size of new gvals/2: " + glength);
    byte[] red = id.rvals;
    int rlength = (red.length)/2;
    byte[] blue = id.bvals;
    int blength = (blue.length)/2;
    
    // generating the original byte array lengths
    int gtotal = 0;
    for(int i=0 ; i<glength ; i++) {
      gtotal += ((green[2*i + 1]|256)&255);
    }
    byte[] newgreen = new byte[gtotal];
    System.out.println("Decompressed array size " + newgreen.length);

    int rtotal = 0;
    for(int i=0 ; i<rlength ; i++) {
      rtotal += ((red[2*i + 1]|256)&255);
    }
    byte[] newred = new byte[rtotal];
    
    int btotal = 0;
    for(int i=0 ; i<blength ; i++) {
      btotal += ((blue[2*i + 1]|256)&255);
    }
    byte[] newblue = new byte[btotal];

    int value = 0;
    int count = 0;
    int total = 0;
    
    //GREEN 
    for (int i=0 ; i<glength ; i++){
      value = ((green[2*i]|256)&255);  // value is an int
      count = ((green[2*i+1]|256)&255);
      
      // writing the value down "count" number of times
      for (int j=0 ; j<count ; j++){
        newgreen[total+j] = (byte)value; }
      total += count;
    }
    total = 0;
    
    //RED
    for (int i=0 ; i<rlength ; i++){
      value = ((red[2*i]|256)&255);  // value is an int
      count = ((red[2*i+1]|256)&255);
      
      // writing the value down "count" number of times
      for (int j=0 ; j<count ; j++){
        newred[total+j] = (byte)value; }
      total += count;
    }
    total = 0;

    //BLUE
    for (int i=0 ; i<blength ; i++){
      value = ((blue[2*i]|256)&255);  // value is an int
      count = ((blue[2*i+1]|256)&255);
      
      // writing the value down "count" number of times
      for (int j=0 ; j<count ; j++){
        newblue[total+j] = (byte)value; }
      total += count;
    }

    // putting the new byte arrays into id
    id.gvals = newgreen;
    id.rvals = newred;
    id.bvals = newblue;
    
    return id;
  }

}
