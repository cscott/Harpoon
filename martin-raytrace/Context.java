/*
 * @(#)Context.java	1.31 06/17/98
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

/**
 * This class is used to define the context for the execution of the
 * benchmark. This container class has the parameters which can hold
 * the benchmark run specific parameters like the run time, speed, 
 * files opened, number of cached_files, SpecBasePath, console window
 * for displaying the trace
 */


public final class Context{

/**
 * SpecBasePath is used to define the base directory of the specJava
 * @see java.lang.String
 */
  private static String SpecBasePath = "";

/**
 * NetworkAccess flag is used to define whether the benchmark uses
 * the network or not
 */
  static boolean NetworkAccess = false;

/**
 * BasePath 
 * @see java.lang.String
 */
  static String BasePath = "";

/**
 * speed is used to define the benchmark run length. The benchmarks
 * can be run at 1%, 10% and 100% of their full length of execution.
 * Some times the % variation is reflected by just altering the loop
 * count in the benchmark (ie repeatedly executing the same benchmark)
 */ 
  static int speed = 100;

/**
 * CachedInput is flag used to indicate, whether the classes have to 
 * be cached or not. When the benchmark is run by the harness. The 
 * classes are fetched over the net during the first run. The execution
 * time in this case includes the network load time. Depending on the 
 * user selection this flag will be toggled and used to cache the 
 * classes loaded first time. The second run uses these cached 
 * classes if flag is turned on.
 */
  static boolean CachedInput = true;
  
/**
 * IOtime is gives the cumilative io time of the all the io calls in
 * in the execution of the benchmark. The refference time is taken 
 * before entering the io routine and the refference time is taken at
 * the time of exiting from the routine. The difference between them 
 * gives the time sepent in that routine. All such times are 
 * accumilated to give the overall io time in the benchmark execution.
 * see @java.lang.System#getSystemTime
 */
  static boolean batch = false;

/**
 * This flag is used to turn on/off the graphicsMode. Some of the tests 
 * have the graphics in them. The graphics are conditionally enabled or 
 * disabled depending on the final version of SpecJava
 */
  static boolean graphicsMode = false;

/**
 * The parameter is used to for storing the IOtime of the current benchmark
 * under execution.
 */
  static long IOtime = 0;
  
/**
 * num_files_open is an integer variable to hold the number of files 
 * opened during the benchmark run. 
 */
  static int num_files_open = 0;
  
/**
 * cached_data is an integer variable to hold the number of bytes of
 * data that is cached. This can be more than zero for the first run
 * but this has to be zero for the subsequent runs.
 */
  static int cached_data = 0;
  
/**
 * num_cached_files is an integer variable to hold the number of files
 * cached during the run of the benchmark. 
 */
  static int num_cached_files = 0;
  
/**
 * userPropFile is the string used to hold the user profile file name
 * The user profile file has the user specific data, which can also be 
 * modified by using the setup key. The Setup parameters are initialized
 * with the data from the user profile file
 */
  static String userPropFile;
  
/**
 * verify flag is used to turn on/off the verification process of benchmark
 * runs.
 */
  static boolean verify = true;
  
/**
 * commandLineMode is a flag that holds the value which indicate whether the 
 * benchmark is running in command line mode or as an applet.
 */
  static boolean commandLineMode = false;  
  
/**
 * window is the ConsoleWindow object type where the trace of the 
 * benchmark execution is displayed.
 * @see spec.harness.ConsoleWindow
 */
  
/**
 * out is the PrintStream into which the trace messages will be written.
 * This is assigned to the System.output stream by default
 */
  public static java.io.PrintStream out = System.out;
  
/**
 * This function returns the integer value given the String form
 * of it. In case of any number format exception, the function returns
 * default value.
 * @param s String value passed
 * @param deft the default value to be returned.
 */
  public static int getValue(String s, int deft) {

      try {
	  return Integer.parseInt(s, 10);
      } catch(NumberFormatException x) {}
            
      return deft;
  }
    
/**
 * This function creates the new Console window and the print stream
 * @see spec.harness.ConsoleWindow
 */
  public static void setupConsoleWindow(){
      out = new PrintStream(new ConsoleOutputStream());
  }
  
/**
 * This function set the  SpecBasePath to the string value passed with
 * some data stripped.
 * @param basePath The URL of the file
 */
  public static void setSpecBasePath(String basepath){
    if (basepath.indexOf("file:" , 0) == 0){
	SpecBasePath = basepath.substring(5);
    }
    else{
        SpecBasePath = basepath;
    }
    
    if (SpecBasePath.indexOf("http:" , 0) == 0){
	NetworkAccess = true;
    } else {
	while (SpecBasePath.charAt(0) == '/' && SpecBasePath.charAt(1) == '/') {
	    SpecBasePath = SpecBasePath.substring(1); 
	}   
    }   
  }

/**
  returns the specbase path
 */
  public static String getSpecBasePath(){
	return SpecBasePath;     
  }

/**
 * Increments the the cached_data parameter by the integer passed.
 * @param num increment value
 */
  public static void addCachedDataSize(int num){
    cached_data += num;
  }
  
/**
 * gets the cached data size. 
 * @return The cached_data value
 */
  public static int getCachedDataSize(){
    return cached_data;
  }
  
/**
 * Increments the the num_cached_files parameter by 1.
 */
  public static void IncrementNumCachedFiles(){
    num_cached_files++;
  }
  
/**
 * gets the numbef of cached files. 
 * @return The num_cached_files value
 */
  public static int getNumCachedFiles(){
    return num_cached_files;
  }
  
/** 
 * Increments the number of Files open parameter. The number of files 
 * open parameter is used for debugging purposes to findout whether the 
 * finalizers are called or not
 */
  public static void IncrementNumOpenFiles(){
    num_files_open++;
  }
  
/** 
 Decrements the number of Files open parameter 
 */  
  public static void DecrementNumOpenFiles(){
    num_files_open--;
  }
  
/** 
 returns the number of files open
 */  
  public static int getNumOpenFiles(){
    return num_files_open;
  }

/**
 *  sets the benchmark relative path. For example if  SpecBasepath is
 * /var/htdocs/v11/spec, The relative benchmark path path for
 * _201_compress is benchmarks/_201_compress. This function adds these
 * two strings and forms the BasePath
 * BasePath = /var/htdocs/v11/spec + benchmarks/_201_compress
 * @param rpath Relative path of the benchmark
 */
  public static void setBenchmarkRelPath(String rpath){
    BasePath = SpecBasePath + rpath;
  }
  
  
/**
 * This function changes the basepath to point to the input directory
 * of the benchmark. this function just concatinates the "/input" 
 * to the existing basepath if it is not added already.
 */
  public static void cdInput(){
    if( !BasePath.endsWith( "input/" )) {
	BasePath = BasePath + "input/" ;
    }
  }
  
/** 
 * Sets the network access flag 
 * @param flag Flag indicating the network access
 */  
  public static void setNetworkAccess(boolean flag){

    NetworkAccess = flag;
  }

/**
 Returns the spec base path. This function is handly in loading the files
 from the relative directories.
 */
  public static String getBasePath(){

    return BasePath;
  }

/**
 Indicates whether SpecJVMClient is running as an applet or application
 */
  public static boolean isNetworkAccess(){

    return NetworkAccess;

  }

/** 
 * Sets the speed of execution to the value passed. Depending on the 
 * user's selection, the speed of execution is set as 1% 10% or 100%
 * @param n Speed selected by the user
 */  
  public static void setSpeed( int n ) {
    speed = n;
  }
  
/**
 Returns the speed of the benchmark.
 */  
  public static int getSpeed() {
    return speed;
  }

/**
 Sets the CachedInput flag. The data is read from the Cache during the second
 run if this flag is set.
 */
  public static void setCachedInputFlag( boolean cif ){
    CachedInput = cif;
  }

/**
 Sets the batch flag
 @param b boolean value for the batch flag
 */
  public static void setBatch( boolean b ){
    batch = b;
  }

/**
 Returns whether SpecJVMClient is running in batch mode
 */
  public static boolean isBatch(){
    return batch;
  }

/**
 Sets the graphics mode. Normally SepcJVM client runs with graphics disabled.
 This flag is for future extensions and debugging
 */    
  public static void setGraphicsMode (boolean mode){
    graphicsMode = mode;
  }

/**
 Returns the graphic mode flag value
 */    
  public static boolean getGraphicsMode(){
    return graphicsMode;
  }

/**
 Sets the user properties file. These properties are stored in the mail sent at
 the end of the test
 @param s Properties file name 
 */
  public static void setUserPropFile( String s){
    userPropFile = s;
  }

/**
 Returns the properties file name
 */
  public static String getUserPropFile(){
    return userPropFile;
  }

/**
 Returns the cached input flag
 */
  public static boolean isCachedInput(){
    return CachedInput;
  } 
  
/**
 Clears the IOtiming. This is normally done before starting a benchmark
 */  
  public static void clearIOtime(){
    IOtime = 0;
  }

/**
 Increments the IOTime by the value provided
 @param time Incremental value
 */
  public static void addIOtime(long time){
    IOtime = IOtime + time;
  }

/**
 Returns the IO time 
 */    
  public static long getIOtime(){
    return IOtime;
  }
 
    /**
     * Set commandLineMode flag
     */
    public static void setCommandLineMode(boolean value) {
	commandLineMode = value;
    }


    /**
     * Get commandLineMode flag
     */
    public static boolean getCommandLineMode() {
	return commandLineMode;
    }


    /**
     * Set verify flag
     */
    public static void setVerify(boolean value) {
	verify = value;
    }


    /**
     * Get verify flag
     */
    public static boolean getVerify() {
	return verify;
    }
    
          
    /**
     * Start output window
     */
    public static void startOutputWindow() {   
    }  
        
	    
    /**
     * Stop output window
     */
    public static void stopOutputWindow() {
    }   
	    
    /**
     * Output to console window
     */
    public static void appendWindow(String s) {
	    System.out.print(s);
    }       
}

