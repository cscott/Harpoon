## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = librealtime.la

if REALTIME_JAVA
librealtime_la_SOURCES = MemBlock.h MemBlock.c MemoryArea.c MemoryArea.h \
RTJmalloc.h RTJmalloc.c blockAllocator.h blockAllocator.c heapMemory.h \
heapMemory.c listAllocator.h listAllocator.c objectList.h objectList.c \
RTJconfig.h CTMemory.h CTMemory.c HeapMemory.h HeapMemory.c ImmortalMemory.h \
ImmortalMemory.c ImmortalPhysicalMemory.h ImmortalPhysicalMemory.c \
LTMemory.h LTMemory.c NullMemoryArea.h NullMemoryArea.c ScopedPhysicalMemory.h \
ScopedPhysicalMemory.c VTMemory.h VTMemory.c block.c block.h GCstats.c \
GCstats.h
else
librealtime_la_SOURCES = GCstats.c GCstats.h
endif
