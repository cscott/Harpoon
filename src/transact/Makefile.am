## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

# this library will be empty unless we've elected to build transaction support.
noinst_LTLIBRARIES = libtransact.la

TRANSACTSRC=  transact.h \
	preproc.h flags.h objinfo.h \
	atomic.h atomic-impl.h \
	proto.h proto-impl.h \
	readwrite.h readwrite.c \
	versions.h versions.c \
	vhash.h vhash.c \
	transjni.h transjni.c jni-array.h jni-field.h jni-invoke.h \
	harpoon_Runtime_Transactions_ImplHelper.c \
	harpoon_Runtime_Transactions_ImplHelper.h \
	harpoon_Runtime_Transactions_CommitRecord.c \
	harpoon_Runtime_Transactions_CommitRecord.h
OTHERSRC = readwrite-impl.c versions-impl.c vhash-impl.c transjni-impl.c
# GNUCP_TRANSACTSRC should contain files to support transactional
# versions of native methods in GNU classpath.  At the moment, there are
# none.
GNUCP_TRANSACTSRC=

if TRANSACTIONS
if SUNJDK
libtransact_la_SOURCES = $(TRANSACTSRC)
else # using classpath...
libtransact_la_SOURCES = $(TRANSACTSRC) $(GNUCP_TRANSACTSRC)
endif
endif
# automake won't allow EXTRA_DIST to be conditionally defined, so we
# define it even when it's not necessary (when TRANSACTIONS is set)
EXTRA_DIST = $(TRANSACTSRC) $(GNUCP_TRANSACTSRC) $(OTHERSRC)

mkflags_SOURCES = mkflags.c
noinst_PROGRAMS = mkflags
flags.h: mkflags
	./$< > $@

# just to remind ourselves how it's done... to rebuild java_lang_Object.h, do:
rebuild-object-h:
	jikes -d . -g -cp ~/Harpoon/AWAKE/Code:/usr/share/classpath/glibj.zip Object.java
	gcjh -jni --bootclasspath ~/Harpoon/AWAKE/Code:/usr/share/classpath/glibj.zip java.lang.Object
