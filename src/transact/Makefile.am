## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build transaction support.
noinst_LTLIBRARIES = libtransact.la

TRANSACTSRC=  transact.h \
	java_lang_Object.c java_lang_Object.h \
	harpoon_Runtime_Transactions_CommitRecord.c \
	harpoon_Runtime_Transactions_CommitRecord.h
GNUCP_TRANSACTSRC= \
	java_io_FileOutputStream.c java_io_FileOutputStream.h

if TRANSACTIONS
if SUNJDK
libtransact_la_SOURCES = $(TRANSACTSRC)
else # using classpath...
libtransact_la_SOURCES = $(TRANSACTSRC) $(GNUCP_TRANSACTSRC)
endif
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when TRANSACTIONS is set)
EXTRA_DIST = $(TRANSACTSRC) $(GNUCP_TRANSACTSRC)

# just to remind ourselves how it's done... to rebuild java_lang_Object.h, do:
rebuild-object-h:
	jikes -d . -g -cp ~/Harpoon/AWAKE/Code:/usr/share/classpath/glibj.zip Object.java
	gcjh -jni --bootclasspath ~/Harpoon/AWAKE/Code:/usr/share/classpath/glibj.zip java.lang.Object
