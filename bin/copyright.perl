#!/usr/bin/perl
# add copyright strings to those files missing it.
use File::Basename;
use Time::Local;
require "ctime.pl";

$NL=$/;
undef $/; # always read whole file at once.

$gnugpl1 =
 "// Copyright (C) 1998 ";
$gnugpl2 = "$NL".
 "// Licensed under the terms of the GNU GPL; see COPYING for details.$NL";

FILE: foreach $f (split(/\s+/,`make list`)) {
    next if ($f =~ /^Test/); # skip test code.
    next if ($f =~ /^Contrib/); # skip code not written by us.
    # snarf input file.
    open(FH, "< $f") or die "Can't open $f for reading.\n";
    $_ = <FH>;
    close FH;

    # check for header string (with timestamp) on first line.
    if (! m|^// .*, created\s+|
	||m|^// .*, created\s+by\s+.*$|m) {
	# basename of source file.
	$basename=basename($f);
	# get creation date from RCS log
	open(LOG, "cvs log $f |") or die "Can't open cvs log for $f.\n";
	$cvs = <LOG>;
	close LOG;
	$cvs =~ /^revision 1.1[ \n\r\t]+date: ([^;]+);\s+author: ([^;]+);/m;
	$date=$1; $author=$2;
	($year,$mon,$day,$hour,$min,$sec)=
	    ($date=~m|(\d+)/(\d+)/(\d+)\D+(\d+):(\d+):(\d+)|);
	$time=timegm($sec,$min,$hour,$day,$mon-1,$year-1900);
	$date=ctime($time); $date=~s/$NL$//;
	# add header string.
	if (m|^// .*, created\s+by\s+.*$|m) {
	    s|^(// .*, created\s+)(by\s+.*)$|$1.$date." ".$2|me;
	} else {
	    s|^|"// $basename, created $date by $author$NL"|e;
	}
    }
    # insert copyright string if necessary.
    if (! /GNU GPL/m) {
	# extract author
	die "No author found for $f.\n" unless /\@author\s+(.*)$/m;
	$author=$1;
	$author =~ s/, based on.*$//m; # clean up attribution case.
	# add (c) string.
	s|^(\s*package\s+)|$gnugpl1.$author.$gnugpl2.$1|me;
    }
    # write back to file.
    open(FH, "> $f") or die "Can't open $f for writing.\n";
    print FH $_;
    close FH;
}
