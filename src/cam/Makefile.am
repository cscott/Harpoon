## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build camera support.
if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libcam.la
else
noinst_LIBRARIES = libcam.a
endif

CAMSRC =\
		    CaptureHeader.h  DeviceCtl.c  DeviceInfo.c \
		    VideoCapture.c  VideoCapture.h \
	            AltVideo.c AltVideo.h IPaqVideo.h IPaqVideo.c
if CAMERA
libcam_la_SOURCES = $(CAMSRC)
else
libcam_la_SOURCES = empty.c
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when CAMERA is set)
EXTRA_DIST = $(CAMSRC)


libcam_a_SOURCES = $(libcam_la_SOURCES)


JAVASRC=AltVideo.java IPaqVideo.java
JARFILE=video.jar
EXTRA_DIST += $(JAVASRC)

# include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
