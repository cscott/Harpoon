## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
SUBDIRS=

all:
# do make in gc subdir.
if CROSS_COMPILING
	@echo
	@echo "  ****** GARBAGE COLLECTOR MUST BE COMPILED NATIVELY. ******"
	@echo
else
	make -C gc CC=$(CC) AS=$(AS)
endif
