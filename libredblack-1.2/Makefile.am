lib_LTLIBRARIES=libredblack.la
include_HEADERS=redblack.h
libredblack_la_SOURCES=redblack.c
libredblack_la_LDFLAGS= -version-info @REDBLACK_LIB_CURRENT@:@REDBLACK_LIB_REVISION@:@REDBLACK_LIB_AGE@
EXTRA_DIST=autogen.sh @PACKAGE@.spec @PACKAGE@.spec.in 
CFLAGS+=

rpms: distcheck rpms-nocheck

rpms-nocheck:
	rpm -ta @PACKAGE@-@VERSION@.tar.gz
	cp /usr/src/redhat/SRPMS/@PACKAGE@-@VERSION@-*.rpm .
	cp /usr/src/redhat/RPMS/*/@PACKAGE@-*@VERSION@-*.*.rpm .
