## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = librealtime.la


if REALTIME_JAVA_STUBS
librealtime_la_SOURCES = RTJstubs.c GCstats.h GCstats.c NoHeapRealtimeThread.h \
NoHeapRealtimeThread.c
else
if REALTIME_JAVA_THREADS
librealtime_la_SOURCES = MemBlock.h MemBlock.c MemoryArea.c MemoryArea.h \
RTJmalloc.h RTJmalloc.c RTJconfig.h CTMemory.h CTMemory.c HeapMemory.h \
HeapMemory.c ImmortalMemory.h ImmortalMemory.c ImmortalPhysicalMemory.h \
ImmortalPhysicalMemory.c LTMemory.h LTMemory.c NullMemoryArea.h \
NullMemoryArea.c ScopedPhysicalMemory.h ScopedPhysicalMemory.c VTMemory.h \
VTMemory.c block.c block.h GCstats.c GCstats.h RawMemory.h RawMemory.c \
RTJfinalize.h RTJfinalize.c RealtimeSystem.h RefCountArea.h RefCountArea.c \
RealtimeSystem.c NoHeapRealtimeThread.h NoHeapRealtimeThread.c \
linkedListAllocator.h linkedListAllocator.c refCountAllocator.h \
refCountAllocator.c threads.c POSIXSignalHandler.h POSIXSignalHandler.c
else
librealtime_la_SOURCES = MemBlock.h MemBlock.c MemoryArea.c MemoryArea.h \
RTJmalloc.h RTJmalloc.c RTJconfig.h CTMemory.h CTMemory.c HeapMemory.h \
HeapMemory.c ImmortalMemory.h ImmortalMemory.c ImmortalPhysicalMemory.h \
ImmortalPhysicalMemory.c LTMemory.h LTMemory.c NullMemoryArea.h \
NullMemoryArea.c ScopedPhysicalMemory.h ScopedPhysicalMemory.c VTMemory.h \
VTMemory.c block.c block.h GCstats.c GCstats.h RawMemory.h RawMemory.c \
RTJfinalize.h RTJfinalize.c RealtimeSystem.h RealtimeSystem.c \
NoHeapRealtimeThread.h NoHeapRealtimeThread.c linkedListAllocator.h \
linkedListAllocator.c RefCountArea.h RefCountArea.c refCountAllocator.h \
refCountAllocator.c POSIXSignalHandler.h POSIXSignalHandler.c
endif
endif

