## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = libjavanet.la

SOURCELIST = \
	java_net_InetAddressImpl.c java_net_InetAddressImpl.h \
	java_net_PlainDatagramSocketImpl.c java_net_DatagramPlainSocketImpl.h \
	java_net_PlainSocketImpl.c java_net_PlainSocketImpl.h \
	java_net_SocketInputStream.c java_net_SocketInputStream.h \
	java_net_SocketOutputStream.c java_net_SocketOutputStream.h
EXTRA_DIST = $(SOURCELIST) # ensure that these are in the distribution.

if BUILD_JAVA_NET
libjavanet_la_SOURCES = $(SOURCELIST)
else
libjavanet_la_SOURCES = # empty library for non-networking platforms.
endif
