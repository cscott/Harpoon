/*
 * @(#)FileInputStream.java	1.33 06/17/98
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

import java.util.*;
import java.net.*;
import java.io.*;

/**
 This class extends the java.io.InputStream and provides some extra functionality
 for caching the data. It also keeps a count of the number of filestreams
 opened by the SpecJVMClient, number of files read, number of bytes read,
 io times spent
 @see java.io.InputStream
 @see FileCacheData
 */
public class FileInputStream extends java.io.InputStream{
  


  //************************* Static variables****************************//
  // Constants
  private static final int LENGTH_ERROR = -1;

  // Cache variables

/**
 Maintains a list of cached files
 */    
  private static Hashtable filecache = new Hashtable();

/**
 Maintains a list of opened InputStreams
 */
  private static java.util.Vector openedInputStreams = new java.util.Vector();

/**
 Enables or disables the Debug mode of operation
 */
  public  static boolean debug = false;
  public  static boolean testing = false;

/**
 Keeps a count of closed filestreams
 */  
  public  static int numofCloses = 0;

  // How and whether to retry http operations
/**
 Number of retries before declaring the error in opening the file
 */  
  private static int nRetry = 5;		// times to try

/**
 Sleep time between two successive retries
 */  
  private static int retryDelay = 5000;		// milliseconds

/**
 Total retries made throughout the benchmark suit
 */    
  private static int totalRetries = 0;		// count over all benchmarks

/**
 A flag to control which API to be used
 */
  private static boolean use11net = true;	// use 1.1 API instead of

  // IO Stats variables

/**
 Number of open files
 */  
  private static int num_open_files = 0;

/**
 Number of files used
 */  
  private static int num_files_used = 0;

/**
 Number of cached files
 */  
  private static int num_cached_files = 0;

/**
 Size of the cache at a given time
 */  
  private static int size_cached_data = 0;

/**
 Total number of bytes read from the cache for executing this benchmark
 */  
  private static int total_num_of_byte_reads_from_cache = 0;
  
/**
 Total number of bytes read from file
 */  
  private static int total_num_of_byte_reads_from_file = 0;
  
/**
 Total number of bytes read from the URL  
 */
  private static int total_num_of_byte_reads_from_url = 0;

/**
 Number of cache hits
 */    
  private static int num_cache_hits = 0;
  
/**
 Number of cache misses or number of files hits
 */
  private static int num_cache_misses = 0;

/**
 IOtime for executing this benchmark
 */    
  private static long IOtime =0;
  
/**
 Caching time for executing this benchmark
 */
  private static long Cachingtime = 0;
  //********************** End Static variables****************************//

 

  //************************* Static methods******************************//

  // Cache methods

/**
 Clears the cache data and updates the Caching parameters
 */
  public static void clearCache(){
    filecache = new Hashtable();
    num_cached_files = 0;
    size_cached_data = 0;
    num_cache_hits = 0;
    num_cache_misses = 0;  
  }

/**
 Closes all the opened streams
 */
  public static void closeAll() throws java.io.IOException{

    Enumeration all_opened_in_streams = openedInputStreams.elements();
    if(debug) {
        System.out.println("Close all called");
    }

    while(all_opened_in_streams.hasMoreElements()){
      
      try{
	java.io.InputStream in = (java.io.InputStream)all_opened_in_streams.nextElement();
	in.close();
	num_open_files--;
      }catch(NoSuchElementException e){
      }
    }

  }
  

  // IO Stats methods

  /*
   * could be useful in debugging
  public static String getListCachedFiles(){
      StringBuffer buf = new StringBuffer();
      for (Enumeration e = filecache.keys(); e.hasMoreElements(); ){
	  buf.append ((String)(e.nextElement()));
	  buf.append (",");
      }
      return buf.toString();
  }
   */
/**
 Returns the number of open files
 */
  public static int getNumOpenFiles(){
    return num_open_files;
  }

/** 
 Returns the number of used files
 */    
  public static int getNumUsedFiles(){
    return num_files_used;
  }
  
/** 
 Returns the number of Cached files
*/  
  public static int getNumCachedFiles(){
    return num_cached_files;
  }
  
/**
 Returns the size of the cached data
*/  
  public static int getCachedDataSize(){
    return size_cached_data;
  }

 /**
  Returns the number of bytes read from the cache
  */
  public static int getNumCacheByteReads(){
    return total_num_of_byte_reads_from_cache;
  }

 /**
  Returns the number of bytes read from the file
  */
  public static int getNumFileByteReads(){
    return total_num_of_byte_reads_from_file;
  }

/**
 Returns the number of bytes read form the URL
 */    
  public static int getNumUrlByteReads(){
    return total_num_of_byte_reads_from_url;
  }

/**
 Returns the number of cache hits in executing the current benchmark 
 */    
  public static int getNumCacheHits(){
    return num_cache_hits;
  }

/**  
 Returns the number of cache misses in executing the current benchmark 
 */
  public static int getNumCacheMisses(){
    return  num_cache_misses;
  }

/**
 Returns the IO time spent in executing the current benchmark 
 */   
  public static long getIOtime(){
    return IOtime;
  }

/**
 Returns the Caching time
 */    
  public static long getCachingtime(){
    return Cachingtime;
  }

/**
 Return the total retries
 */
  public static int getTotalRetries(){
    return totalRetries;
  }

/**
 Clears the IO statistics variables.
 */    
  public static void clearIOStats(){
    num_files_used = 0;
    total_num_of_byte_reads_from_cache = 0;
    total_num_of_byte_reads_from_file = 0;
    total_num_of_byte_reads_from_url = 0;
    IOtime = 0;
    Cachingtime = 0;
  }

/**
 Checks whether the given URL is valid or not.
 */
  public static boolean IsURLOk(URL url){

    /* should have no leading/trailing LWS
     * expedite the typical case by assuming it has
     * form "HTTP/1.x <WS> 2XX <mumble>"
     */
    int ind;
    try {  
      
      URLConnection urlc = url.openConnection();
    
      String resp = urlc.getHeaderField(0);

      if( resp == null ) {
//	  System.out.println( "resp == NULL in IsURLOk" );
	  return true;	   
      }
         
      ind = resp.indexOf(' ');
      while(resp.charAt(ind) == ' ')
	ind++;
      int responseCode = Integer.parseInt(resp.substring(ind, ind + 3));
      
      if (debug){
	System.out.println("responseCode = " + responseCode);
      }

      return !( ((responseCode >= 400) && (responseCode <= 415)) || 
		((responseCode >= 500) && (responseCode <= 505))
		);
            
    } catch (java.io.IOException e) { 
      System.out.println("Exception in IsURLOk " + e );	    
      return false;
    }

  }


/**
 Constructs the URL for the server file given the file name
 */
  private static String makeGoodUrl(String str){

    String retstr;

    retstr = str.replace( '\\', '/' );
    
    int ind = 0;
     
    if (retstr.startsWith("http")){
      ind = 6;
    }

    if (retstr.indexOf("//" , ind) != -1){
      char c[] = retstr.toCharArray();
      int len = retstr.length();
      for(int i = ind; i < len; i++){
	
	if (c[i] == '/'){
	  int j = i+1;

	  while( (j < len) && (c[j] == '/') ){
	    j = j+1;
	  }
	  if (j > (i+1)){
	    for(int k = i+1 , l = j; l < len; l++ , k++){
	      c[k] = c[l];
	    }
	    len = len - (j-i-1);
	  }
	}
	retstr = new String(c , 0 , len);
      }
    }
    return retstr; 
  }

  //********************** End Static methods****************************//

  

/**
 The file URL
 */  
  private String my_file_url;

/**
  
 */  
  private java.io.InputStream orig_in;
 
/**
 Length of the FileInputStream
 */   
  private int len = 0;

/**
 Flag indicating whether the file stream is closed or not
 */    
  private boolean closed = false;

/**
 Flag indicating whether to cache the data during the first loading or not
 */    
  private boolean doCache;

/**
 Flag indicating whether the data is from the Cache
 */    
  private boolean fromCache = false;

/**
 Flag indicating whether the data is from URL
 */  
  private boolean fromUrl = false;
  
 /**
  Updates the file read statistics
  @param len Length of the FileStream
  */
  private void collectReadStats(int len){
    
    if (len <= 0){
      return;
    }
    
    if (fromCache){
      total_num_of_byte_reads_from_cache += len;
    }
    else{
      if (fromUrl){
	total_num_of_byte_reads_from_url += len;
      }
      else{
	total_num_of_byte_reads_from_file += len;
      }
    }

  }
    
    
    /**
     * Main class constructor.
     * 
     * Try and find the strange URL open bug by catching any exceptions and calling the
     * System finalizer to try and close the sockets.
     */     
    public FileInputStream(String input_filename) throws java.io.FileNotFoundException,
				    java.net.MalformedURLException , java.io.IOException {
	super();
    
	try {
            construct(input_filename);
	} catch(Exception a) {
	    System.out.println ( "----------------------------------");
	    System.out.println ( "FileInputStream: Caught exception in constructor: "+a);
	    System.out.println ( "trying to open: " + input_filename); //wnb
	    System.out.println ( "FileInputStream: Attempting recovery with System.runFinalization()");	    
	    System.gc();
	    System.runFinalization();
	    try {
		construct(input_filename);
	    } catch(Exception b){
   		System.out.println ( "----------------------------------");
		System.out.println ( "FileInputStream: Caught exception in constructor at second level: "+a);
		if ( !(b instanceof java.io.FileNotFoundException) ) {
		    System.out.println ( "FileInputStream: Exiting benchmark");		    
		    printStackTrace(b);
		    throw new StopBenchmarkException( "FileInputStream failure" );
		} else {
		    throw (java.io.FileNotFoundException)b;
		}
	    }
	}
    }

/**
 Constructs thd FileInputStream object from the given file name
 */	    
  private void construct(String input_filename) throws Exception{           
      
	long startTime = System.currentTimeMillis();

	my_file_url = makeGoodUrl(Context.getBasePath() + input_filename);
	
	num_open_files++;
	num_files_used++;

	if (filecache.containsKey(my_file_url)){

	  long cacheStartTime = System.currentTimeMillis();
	
	  doCache = false;    // no need to cache data -- data is already in cache
	  fromCache = true;   // read data from cache
	
	  num_cache_hits++;   // we have a hit

	  if (debug){
	    System.out.println("\n++++++++making InputStream from cached " + my_file_url);
	  }


	  // Get the cached data from the filecache
	  FileCacheData fcd = (FileCacheData)filecache.get(my_file_url);  

	  // Get the length of the data
	  len = fcd.getLength();

	  // Get an inputStream to read data form the cache
	  orig_in = fcd.getInputStream();

	  long cacheEndTime = System.currentTimeMillis();
	  Cachingtime += (cacheEndTime - cacheStartTime);
	}
	else{
	
	  num_cache_misses++;  // we have a miss
	
	  if (debug){
	    System.out.println("\n++++++++Opening InputStream for " + my_file_url);
	  }

	
	  openStream();  // open an inputStream through network or file I/O

		
	  if (Context.isCachedInput()){

	    long cacheStartTime = System.currentTimeMillis();
	 
	    doCache = true;  // we have to cache data

	    // create a new FileCacheData object of size len and put it in the filecache 
	    // with my_file_url as the key
	
	    FileCacheData fcd = new FileCacheData(len);  
	    filecache.put(my_file_url , fcd);
	  
	    size_cached_data += len;
	    num_cached_files++;
	 
	    long cacheEndTime = System.currentTimeMillis();
	    Cachingtime += (cacheEndTime - cacheStartTime);
	  }
	  else{
	    doCache = false;
	  }

	  // Keep track of all the InputStreams opened so far.
	  openedInputStreams.addElement(orig_in);
	
	
	}
      
	long endTime = System.currentTimeMillis();
	long totalTime = endTime - startTime;


	Context.addIOtime(totalTime);
	IOtime += totalTime;
  } 
  
  
  /**
   * Prints the stack backtrace to Context.out. If this is an instance of
   * PrintStream (it normally will be) then the default validity
   * check value is changed to '1'. This is the code that means all data
   * should be compaired and will ensure that the benchmark fails the 
   * validity check process.
   */
  void printStackTrace(Exception x) {
      PrintStream str = (PrintStream)Context.out;      
      char save = str.cout.setValidityCheckValue(ValidityCheckOutputStream.ALL);
      x.printStackTrace(Context.out);
      str.cout.setValidityCheckValue(save);	  
  }
  
/**
 Opens the FileInputStream
 */  
  private void openStream() throws java.io.FileNotFoundException,
      java.net.MalformedURLException , java.io.IOException{

    if (Context.isNetworkAccess()){
      fromUrl = true;
      URL url = new URL(my_file_url);
      URLConnection urlc = url.openConnection();
      len = urlc.getHeaderFieldInt("content-length", LENGTH_ERROR);
      for (int retry=0; retry < nRetry && len == LENGTH_ERROR; retry++){
        if (urlc instanceof HttpURLConnection)
          ((HttpURLConnection) urlc).disconnect();
        urlc = null;
        System.gc();
        System.runFinalization();
	if (debug)
            System.out.println ("GET failed on " + my_file_url);
        try{
          Thread.sleep (retryDelay);
        }catch (InterruptedException e){}
        urlc = url.openConnection();
        urlc.connect();
        len = urlc.getHeaderFieldInt("content-length", LENGTH_ERROR);
	totalRetries++;
      }
      if (use11net && (urlc instanceof HttpURLConnection)){
	HttpURLConnection urlh = (HttpURLConnection) urlc;
	if (urlh.getResponseCode() != HttpURLConnection.HTTP_OK)
	  throw new java.io.FileNotFoundException( my_file_url +
	    ": " + urlh.getResponseMessage());
      }else{
        if (!IsURLOk(url))
	  throw new java.io.FileNotFoundException( my_file_url );
      }

      if (len == LENGTH_ERROR){
	throw new java.io.IOException("Cannot establish URL content-length");
/*      
	System.out.println("Error getting size of file -- turning off caching");
	Context.setCachedInputFlag(false);
*/	
      }

      if (debug){
	if (len == LENGTH_ERROR){
	  if (urlc == null){
	    System.out.println("urlc is null");
	  }
	  else{
	    System.out.println("urlc = " + urlc);
	  }
	}
	System.out.println("++++++++FileInputStream url len = " + len);
      }

      try{
	orig_in = urlc.getInputStream();
      }catch(java.io.IOException ioe){
	System.out.println("Error trying to get an inputStream from url connection " + urlc);
	throw ioe;
      }

    }else{    // NOT (Context.isNetworkAccess())
    
      File file = new File(my_file_url);
      len = (int)file.length();

      if (debug){
	System.out.println("++++++++FileInputStream file len = " + len);
      }
      orig_in = new java.io.FileInputStream(my_file_url);
      
    }
  }
	
/**
 Constructor
 @param file File object
 */	
  public FileInputStream(File file) throws java.io.FileNotFoundException,
    java.net.MalformedURLException , java.io.IOException{
        this(file.getPath());
  }

/**
 Returns the stream length
 */    
  public int getContentLength(){
  
    return len;
    
  }

/**
  Returns the number of bytes that can be read from this input stream without 
  blocking. 
 */		
  public synchronized int available() throws java.io.IOException{
    
    try{
     
      return orig_in.available();
    
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    }
  }

/**
 Reads the next byte of data from this input stream.
 */
  public synchronized int read() throws java.io.IOException{
    
    try{
     
      long startTime = System.currentTimeMillis();
    
      int byte_read = orig_in.read();
      if ((doCache) && (byte_read != -1)){
      
	long cacheStartTime = System.currentTimeMillis();
	((FileCacheData)filecache.get(my_file_url)).copyData((byte)byte_read);
	long cacheEndTime = System.currentTimeMillis();
	Cachingtime += (cacheEndTime - cacheStartTime);
      }
    
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;


      Context.addIOtime(totalTime);
      IOtime += totalTime;
    
      if( debug ){
//        System.out.println("byte_read = " + byte_read);
      }

      collectReadStats(1);
      return byte_read;
    
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    }
  }

/**
 Reads up to byte.length bytes of data from this input stream into an array of 
 bytes. This method blocks until some input is available. 
	
 The read method of FilterInputStream calls the read method of three arguments
 with the arguments b, 0, and b.length, and returns whatever value that method
 returns. 
	
 Note that this method does not call the one-argument read method of its 
 underlying stream with the single argument b. Subclasses of FilterInputStream
 do not need to override this method if they have overridden the three-argument
 read method. 
 @param b Buffer into which the data is read
*/    
  public int read(byte b[]) throws java.io.IOException{
  
    try{
      long startTime = System.currentTimeMillis();
   
      int bytes_read = orig_in.read(b);
    
      if ((doCache) && (bytes_read > 0)){

	long cacheStartTime = System.currentTimeMillis();
	((FileCacheData)filecache.get(my_file_url)).copyData(b, 0, bytes_read);
	long cacheEndTime = System.currentTimeMillis();
	Cachingtime += (cacheEndTime - cacheStartTime);
      }
    
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;


      Context.addIOtime(totalTime);
      IOtime += totalTime;
    
    
      if( debug ){
	System.out.println("bytes_read = " + bytes_read);
      }
 
      collectReadStats(bytes_read);
      return bytes_read;
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    }
  }

/**
 Reads up to len bytes of data from this input stream into an array of bytes. 
 This method blocks until some input is available. 

 The read method of FilterInputStream calls the read method of its underlying 
 input stream with the same arguments and returns whatever value that method 
 returns. 
 @param b - the buffer into which the data is read
 @param off - the start offset of the data
 @param len - the maximum number of bytes read
 */ 

  public synchronized int read(byte b[], int off, int len) throws java.io.IOException{
				 
				 
    try{
     
      long startTime = System.currentTimeMillis();
    
      int bytes_read = orig_in.read(b, off, len);
      if ((doCache) && (bytes_read > 0)){
      
	long cacheStartTime = System.currentTimeMillis();
	((FileCacheData)filecache.get(my_file_url)).copyData(b, off, bytes_read);
	long cacheEndTime = System.currentTimeMillis();
	Cachingtime += (cacheEndTime - cacheStartTime);
      }
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;


      Context.addIOtime(totalTime);
      IOtime += totalTime;
    
    
      if( debug ){
        System.out.println("bytes_read = " + bytes_read);
      }
    
      collectReadStats(bytes_read);
      return bytes_read;
   
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    }
  }

/**
 Closes this input stream and releases any system resources associated with the 
 stream. The close method of FilterInputStream calls the close method of its 
 underlying input stream. 
 */
  public void close() throws java.io.IOException{
    
    try{
      if (!closed){

	orig_in.close();
	numofCloses ++;
	num_open_files--;
    
	
	if (debug || testing){
	  System.out.println("++++++++Closing InputStream for " + my_file_url + 
	    " " + num_open_files + " " + numofCloses);
	}
     
	closed = true;
       
      }
      else{
	if (debug){
	  System.out.println("++++++++InputStream for " + my_file_url + " has been closed before");
	}
      }
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    } 
    
  }

/**
 Skips over and discards n bytes of data from the input stream. The skip method
 may, for a variety of reasons, end up skipping over some smaller number of 
 bytes, possibly 0. The actual number of bytes skipped is returned. 

 The skip method of FilterInputStream calls the skip method of its underlying
 input stream with the same argument, and returns whatever value that method 
 does. 
 @param n - number of bytes to be skipped
 */    
  public long skip(long n) throws java.io.IOException{
  
    try{
      if (debug){
	System.out.println("++++++++ Skipping " + n + " positions in InputStream for " + my_file_url);
      }

      if (doCache){
	((FileCacheData)filecache.get(my_file_url)).skipPos(n);
      }
      return orig_in.skip(n);
    }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    } 

  }

  /** 
   Finalization method.
   */
    
  protected void finalize() throws java.io.IOException{
    
  //  try{
      if (!closed){

	if (debug || testing ){
	  System.out.println("++++++++Calling close Inside finalize for " + my_file_url );
	}

	this.close();
	
      }
  /*  }catch(java.io.IOException ioe){
      printStackTrace(ioe);
      throw new StopBenchmarkException(ioe.toString());
    }   
  */  
    
  }

}
