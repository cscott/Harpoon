## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
LIBS =

noinst_LTLIBRARIES = libcam.la

# this library will be empty unless we've elected to build camera support.
if CAMERA
CAMSRC =\
		    CaptureHeader.h  DeviceCtl.c  DeviceInfo.c \
		    VideoCapture.c  VideoCapture.h \
	            AltVideo.c AltVideo.h
endif
libcam_la_SOURCES = $(CAMSRC)
