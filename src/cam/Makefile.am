## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build camera support.
noinst_LTLIBRARIES = libcam.la

CAMSRC =\
		    CaptureHeader.h  DeviceCtl.c  DeviceInfo.c \
		    VideoCapture.c  VideoCapture.h \
	            AltVideo.c AltVideo.h
if CAMERA
libcam_la_SOURCES = $(CAMSRC)
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when CAMERA is set)
EXTRA_DIST = $(CAMSRC)

JAVASRC=AltVideo.java
JARFILE=video.jar
EXTRA_DIST += $(JAVASRC)

# include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JavaRules.make
