package javax.realtime;

public interface PhysicalMemoryTypeFilter {

    int VMAttributes = 0;
    int VMFlags = 0;

    boolean removable = false;
    
    // METHODS IN SPECS

    /** Does the specified range of memory contain any of thie type? */
    public boolean contains(long base, long size);

    /** Search for memory of the right type. */
    public long find(long base, long size);

    /** Return the virtual memory attributes of this type of memory. */
    public int getVMAttributes();

    /** Return the virtual memory flags of this type of memory. */
    public int getVMFlags();

    /** If configuration is required for memory to fit the attribute
     *  of this object, do the configuration here.
     */
    public void initialize(long base, long vBase, long size)
	throws IllegalArgumentException;

    /** Checks if all of the specified range of physical memory is
     *  present in the system. If any of it has been removed, false
     *  false is returned.
     */
    public boolean isPresent(long base, long size)
	throws IllegalArgumentException;

    /** If this type of memory is removable, return true */
    public boolean isRemovable();

    /** Arrange for the specified <code>AsyncEventHandler</code> to
     *  be called if any memory in the specified range is inserted.
     */
    public void onInsertion(long base, long size,
			    AsyncEventHandler aeh)
	throws IllegalArgumentException;

    /** Arrange for the specified <code>AsyncEventHandler</code> to
     *  be called if any memory in the specified range is removed.
     */
    public void onRemoval(long base, long size,
			  AsyncEventHandler aeg)
	throws IllegalArgumentException;

    /** Search for virtual memory of the right type. This is important
     *  for systems where attributes are associated with particular
     *  ranges of virtual memory.
     */
    public long vFind(long base, long size);
}
