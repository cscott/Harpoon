package java.io;

public class NativeIO {
  /****************** BASIC FILE INPUT/OUTPUT ******************/
  /* Flags used for opening a file */
  public final static int
  O_RDONLY=0, O_WRONLY=1, O_RDWR=2, O_NDELAY=4, O_APPEND=8, O_SYNC=16,
    O_NONBLOCK=128, O_CREAT=256, O_TRUNC=512, O_EXCL=1024, O_NOCTTY=2048;

  /* RWX rights when creating a new file */
  public final static int
  S_IRUSR=256, S_IWUSR=128, S_IXUSR=64, S_IRGRP=32,
    S_IWGRP=16, S_IXGRP=8, S_IROTH=4, S_IWOTH=2, S_IXOTH=1,
    S_ISUID=2048, S_ISGID=1024, S_ISVTX=512, S_ENFMT=1024;

  /* Error codes */
  public final static int EOF=-1, ERROR=-2, TRYAGAIN=-3, BUFFERFULL=-4;

  /* Open a file
     INPUT:   Path&Name, Combination of flags
     RETURNS: Integer file descriptor
     ERRORS:  ERROR, TRYAGAIN */
    //public native static int openJNI(String path, int oflag);

  /* Open a file and set the RWX rights
     INPUT:   Path&Name, Combination of flags, Combination of rights
     RETURNS: Integer file descriptor
     ERRORS:  ERROR, TRYAGAIN */
    //public native static int openRightsJNI(String path, int oflag, int mode);

  /* Close a file
     INPUT:   File descriptor
     RETURNS: 0
     ERRORS:  ERROR, TRYAGAIN */
    //public native static int closeJNI(int handle);

  /* Read from a binary file
     INPUT:   file descriptor, buffer to read in, offset in buffer,
     number of bytes to read
     RETURNS: number of bytes read
     ERRORS:  ERROR, EOF, TRYAGAIN */
    public native static int readJNI(int handle, byte[] ptr, int ofs, int size);

  /* Write to a binary file
     INPUT:   file descriptor, buffer to write from, offset in buffer,
     number of bytes to write
     RETURNS: number of bytes written
     ERRORS:  ERROR, TRYAGAIN */
    public native static int writeJNI(int handle, byte[] ptr, int ofs,int size);

  /* Read one character from a file
     INPUT:   file descriptor
     RETURNS: character read
     ERRORS:  EOF, ERROR, TRYAGAIN */
    public native static int getCharJNI(int handle);

  /* Write one character to a file
     INPUT:   file descriptor, character to write
     RETURNS: character written
     ERRORS:  ERROR, TRYAGAIN */
  public native static int putCharJNI(int handle, int c);

  /* Check whether a file descriptor is available for immediate read
     INPUT: file descriptor
     RETURNS: True iif immediate read can be performed */
    //public native static boolean canReadJNI(int handle);

  /* Check whether a file descriptor is available for immediate write
     INPUT:   file descriptor
     RETURNS: True iif immediate write can be performed */
    //public native static boolean canWriteJNI(int handle);

  /********************** SERVER I/O ************************/
  /* Socket domains and types */
    // public final static int
    //AF_UNIX=1, AF_INET=2, SOCK_STREAM=2, SOCK_DGRAM=1, SOCK_SEQPACKET=6;

  /* Create a new socket
     INPUT:   socket domain, socket type, and protocol (usually 0)
     RETURNS: non-blocking file descriptor
     ERRORS:  ERROR */
    //public native static int socketJNI(int domain, int type, int protocol);

  /* Create a listener on a port
     INPUT:   port number
     RETURNS: non blocking file descriptor
     ERRORS:  ERROR */
    //public native static int startListenerJNI(int port);

  /* Accept a connection on a port
     INPUT:   file descriptor associated with port via startListenerJNI(),
     4-byte array where the IP address will be stored
     RETURNS: non-blocking file descriptor for the connection
     ERRORS:  ERROR */
    //public native static int acceptJNI(int handle, byte[] IP);

  /* Look for pending connections on a port
     INPUT:   file descriptor associated with port via startListenerJNI()
     RETURNS: yes or no */
  public native static boolean canAcceptJNI(int handle);

  /********************** SELECT STUFF ************************/

  /* Selects all the file descriptors that are available for immediate I/O
     INPUT: desired FD's for read, desired FD's for write
     RETURNS: Array of available file descriptors:
     - Indexes of fd's in readSet that are ready for input
     - -1
     - Indexes of fd's in writeSet that are ready for output
     - -1 */
  public native static boolean[] selectJNI(int[] readSet, int[] writeSet);

  /************************ SCHEDULER STUFF ***************************/

  /* Scheduler models */
  public final static int MOD_SELECT=0, MOD_SIGNAL=1;

  /* Call this before using any of the other functions */
  public native static void initScheduler(int model);

  /* Instruct a file descriptor to raise a real-time signal.
     Should be called immediately after obtaining the FD via accept.
     In the Select-based implementation, it simply makes the FD
     asynchronous */
  public native static void makeNonBlockJNI(int fd);

  /* Register several file descriptors for read/write events */
    //public native static void register(int[] readFD, int[] writeFD);

  /* Get a FD that is available for immediate read */
    //public native static int getReadFD();

  /* Get a FD that is available for immediate write */
    //public native static int getWriteFD();

  /* Get several FDs that are available for immediate read
     RETURNS: The number of FDs it has actually fetched */


  /* Get several FDs that are available for immediate write
     RETURNS: The number of FDs it has actually fetched */
    //public native static int getWriteFDs(int[]writeFD, int atMost);

  /* Registers some file descriptors and then returns all FDs that are
     ready for input or output. 
     INPUT: desired FD's for read, desired FD's for write
     RETURNS: Array of available file descriptors:
     - FDs that are ready for input
     - -1
     - FDs that are ready for output */

    public native static void registerRead(int fd);

    public native static void registerWrite(int fd);

    public native static int getFDs(int[] array);

    public native static int getFDsSmart(boolean block, int[] array);

  static {
    System.loadLibrary("C_IO");
  }

}
