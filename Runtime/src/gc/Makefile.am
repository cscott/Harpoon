## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@

noinst_LTLIBRARIES = libgc.la

if SEMI_PRECISE_GC
libgc_la_SOURCES = \
	sp_gc.c
else
if COPYING_GC
libgc_la_SOURCES = \
	precise_gc.c copying.c gc-data.c cp_heap.c deflate_objs.c
else
if MARKSWEEP_GC
libgc_la_SOURCES = \
	precise_gc.c marksweep.c gc-data.c ms_heap.c free_list.c \
	harpoon_Runtime_PreciseGC_WriteBarrier.c \
	harpoon_Runtime_PreciseGC_WriteBarrier.h
else
if GENERATIONAL_GC
libgc_la_SOURCES = \
	precise_gc.c generational.c gc-data.c free_list.c cp_heap.c ms_heap.c \
	deflate_objs.c omit_gc_timer.c \
	harpoon_Runtime_PreciseGC_WriteBarrier.c \
	harpoon_Runtime_PreciseGC_WriteBarrier.h
endif
endif
endif
endif
