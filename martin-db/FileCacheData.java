/*
 * @(#)FileCacheData.java	1.3 06/17/98
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

import java.util.*;

/**
 SpecJVMClient maintains the internal cache. The data is read over the network
 for the first run and it is cached. The cached data is used for subsequent runs
 The FileInputStream object contains a FileCacheData object which caches data
 @see spec.io.FileInputStream
  */
public class FileCacheData{

/**
 Data buffer to cache data
 */
  private byte[] data = null;

/**
 Total number of bytes present in the data buffer at anytime
 */    
  private int total_num_bytes = 0;
  
/**
 Constructor 
 @param len Length of the file. The byte array of 'len' size is created
 */ 
  public FileCacheData(int len){
    
    data = new byte[len];

  }
 
/**
 Creates the byte array to hold the file data. The size of the file is passed
 as argument.
 @param len Length of the file
 */
  public void createData(int len){
    
    data = new byte[len];

  }

/**
 Copies the given specified of bytes from the given buffer from given offset
 to the data buffer of the FileCacheData object
 @param b[] 'from' byte array
 @param off Offset within the byte array
 @param num_bytes Number of bytes to be copied
 */
  public void copyData(byte b[] , int off , int num_bytes){
    
    System.arraycopy(b,off,data,total_num_bytes, num_bytes);
    total_num_bytes += num_bytes;

  }
  
/**
 Copies the byte to the data buffer. Increments the number of bytes field
 @param b byte to be copied
 */
  public void copyData(byte b){

    data[total_num_bytes] = b;
    total_num_bytes += 1;

  }

/**
 Skips a portion of buffer
 */
  public void skipPos(long n){
    total_num_bytes += n;
  }

 /**
  Converts the data buffer array to InputStream
  */
  public java.io.InputStream getInputStream(){

    return new java.io.ByteArrayInputStream(data , 0 , total_num_bytes);

  }

/**
 Returns the length of the buffer. 
 */
  public int getLength(){
    return total_num_bytes;
  }

}



