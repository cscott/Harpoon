## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build servo support.
if PRODUCE_LA_LIBS
noinst_LTLIBRARIES = libservo.la
else
noinst_LIBRARIES = libservo.a
endif

SERVOSRC =\
		IPaqServoController.c IPaqServoController.h
if SERVO
libservo_la_SOURCES = $(SERVOSRC)
else
libservo_la_SOURCES = empty.c
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when SERVO is set)
EXTRA_DIST = $(SERVOSRC)


libservo_a_SOURCES = $(libservo_la_SOURCES)


JAVASRC=IPaqServoController.java
JARFILE=servo.jar
EXTRA_DIST += $(JAVASRC)

# include rules to make $(JARFILE) from $(JAVASRC)
include $(top_srcdir)/JarRules.make
