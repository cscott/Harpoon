## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
EXTRA_DIST = gc
SUBDIRS=

all:
if CONSERVATIVE_GC
# do make in gc subdir.
if CROSS_COMPILING
	@echo
	@echo "  ****** GARBAGE COLLECTOR MUST BE COMPILED NATIVELY. ******"
	@echo
else
	make -C gc CC=$(CC) AS=$(AS) gc.a
endif
#
endif

check:
if CONSERVATIVE_GC
# do 'make test' in gc subdir.
if CROSS_COMPILING
else
	make -C gc CC=$(CC) AS=$(AS) test
endif
#
endif

dist-hook:
	$(RM) -rf `find $(distdir)/gc -name CVS`
	make -C $(distdir)/gc clean
