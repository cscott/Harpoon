package javax.realtime;

public final class PhysicalMemoryManager {
    /** The <code>PhysicalMemoryManager</code> is available for use
     *  by the various physical memory accessor objects
     *  (<code>VTPhysicalMemory, LTPhysicalMemory, ImmortalPhysicalMemory,
     *  RawMemoryAccess, RawMemoryFloatAccess</code>) to create objects
     *  of the correct type that are bound to areas of physical memory
     *  with the appropriate characteristics -- or with appropriate
     *  accessor behavior. Esxamples of characteristics that might be
     *  specified are: DMA memory, accessors with byte swapping, etc.
     */

    /** Specify this to identify aligned memory. */
    public static final String ALIGNED = "";

    /** Specify this if byte swapping should be used. */
    public static final String BYTESWAP = "";

    /** Specify this to identify DMA memory. */
    public static final String DMA = "";

    /** Specify this to identify shared memory. */
    public static final String SHARED = "";


    // METHODS IN SPECS

    /** Is the specified range of memory removable? */
    public static boolean isRemovable(long address, long size) {
	// TODO

	return false;
    }

    /** Is any part of the specified range of memory presently removed?
     */
    public static boolean isRemoved(long address, long size) {
	// TODO

	return false;
    }

    /** Register the specified <code>AsyncEventHandler</code> to run
     *  when any memory in the range is added to the system.
     */
    public static void onInsertion(long base, long size,
				   AsyncEventHandler aeh) {
	// TODO
    }

    /** Register the specified <code>AsyncEventHandler</code> to run
     *  when any memory in the range is removed from the system.
     */
    public static void onRemoval(long base, long size,
				 AsyncEventHandler aeh)
	throws IllegalArgumentException {
	// TODO
    }

    /** Register a memory type filter with the physical memory manager.
     */
    public static final void registerFilter(Object name,
					    PhysicalMemoryTypeFilter filter)
	throws DuplicateFilterException, IllegalArgumentException {
	// TODO
    }

    /** Remove the identified filter from the set of registered filters.
     */
    public static final void removeFilter(Object name) {
	// TODO
    }
}
