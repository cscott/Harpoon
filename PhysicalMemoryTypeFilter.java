package javax.realtime;

/** Implementation or device providers may include classes that implement
 *  <code>PhysicalMemoryTypeFilter</code> which allow additional
 *  characteristics of memory in devices to be specified.
 */
public interface PhysicalMemoryTypeFilter {

    int VMAttributes = 0;
    int VMFlags = 0;

    boolean removable = false;
    
    /** Queries the system about whether the specified range of memory
     *  contains any of this type.
     *
     *  @param base The physical address of the beginning of the memory region.
     *  @param size The size of the memory region.
     *  @return True, if the specified range contains ANY of this type of memory.
     */
    public boolean contains(long base, long size);

    /** Search for memory of the right type.
     *
     *  @param base The address at which to start searching.
     *  @param size The amount of memory to be found.
     *  @return The address where memory was found or -1 if it ws not found.
     */
    public long find(long base, long size);

    /** Gets the virtual memory attributes of <code>this</code>.
     *
     *  @return The virtual memory attributes as an integer.
     */
    public int getVMAttributes();

    /** Gets the virtual memory flags of <code>this</code>.
     *
     *  @return The virtual memory flags as an integer.
     */
    public int getVMFlags();

    /** If configuration is required for memory to fit the attribute
     *  of this object, do the configuration here.
     *
     *  @param base The address of the beginning of the physical memory reagion.
     *  @param vBase The address of the beginning of the virtual memory region.
     *  @param size The size of the memory region.
     *  @throws java.lang.IllegalArgumentException If the base and size do not
     *                                             fall into this type of memory.
     */
    public void initialize(long base, long vBase, long size)
	throws IllegalArgumentException;

    /** Queries the system about the existance of the specified range of physical memory.
     *
     *  @param base The address of the beginning of the memory region.
     *  @param size The size of the memory region.
     *  @return True if all of the memory is present. False if any of the memory has been
     *          removed.
     *  @throws java.lang.IllegalArgumentException If the base and size do not fall into
     *                                             this type of memory.
     */
    public boolean isPresent(long base, long size)
	throws IllegalArgumentException;

    /** Queries the system about the removability of this memory.
     *
     *  @return True if this type of memory is removable.
     */
    public boolean isRemovable();

    /** Arrange for the specified <code>AsyncEventHandler</code> to be called if any
     *  memory in the specified range is inserted.
     *
     *  @param base The physical address of the beginning of the memory region.
     *  @param size The size of the memory region.
     *  @param aeh Run the given handler if any memory in the specified range is removed.
     *  @throws java.lang.IllegalArgumentException If the base and size do not fall into
     *                                             this type of memory.
     */
    public void onInsertion(long base, long size,
			    AsyncEventHandler aeh)
	throws IllegalArgumentException;

    /** Arrange for the specified <code>AsyncEventHandler</code> to be called if any
     *  memory in the specified range is removed.
     *
     *  @param base The physical address of the beginning of the memory region.
     *  @param size The size of the memory region.
     *  @param aeh Run the given handler if any memory in the specified range is removed.
     *  @throws java.lang.IllegalArgumentException If the base and size do not fall into
     *                                             this type of memory.
     */
    public void onRemoval(long base, long size,
			  AsyncEventHandler aeg)
	throws IllegalArgumentException;

    /** Search for virtual memory of the right type. This is important for systems where
     *  attributes are associated with particular ranges of virtual memory.
     *
     *  @param base The address at which to start searching.
     *  @param size The amount of memory to be found.
     *  @return The address where memory was found or -1 if it was not found.
     */
    public long vFind(long base, long size);
}
