#!/usr/bin/perl
# add copyright strings to those files missing it.
use File::Basename;
use Time::Local;
require "ctime.pl";

$NL=$/;
undef $/; # always read whole file at once.

$gnugpl1 =
 "// Copyright (C) 2001 ";
$gnugpl2 = "$NL".
 "// Licensed under the terms of the GNU GPL; see COPYING for details.$NL";

sub saneauthors {
    my $author = shift;
    return 'C. Scott Ananian <cananian@alumni.princeton.edu>'
	if $author =~ m/Scott.*Ananian/i or $author =~ m/cananian@/;
    return 'Felix S. Klock II <pnkfelix@mit.edu>'
	if $author =~ m/Felix.*Klock/i or $author =~ m/pnkfelix@/;
    return 'Emmett Witchel <witchel@mit.edu>'
	if $author =~ m/Emmett.*Witchel/i or $author =~ m/witchel@/;
    return 'Duncan Bryce <duncan@lcs.mit.edu>'
	if $author =~ m/Duncan.*Bryce/i or $author =~ m/duncan@/;
    return 'Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>'
	if $author =~ m/Alexandru.*SALCIANU/i or $author =~ m/salcianu@/;
    return 'Andrew Berkheimer <andyb@mit.edu>'
	if $author =~ m/And.*Berkheimer/i or $author =~ m/andyb@/;
    return 'Brian Demsky <bdemsky@mit.edu>'
	if $author =~ m/Brian.*Demsky/i or $author =~ m/bdemsky@/
	    or $author =~ m/[@](bdemsky|kikashi.lcs|windsurf.lcs).mit.edu/i;
    return 'Darko Marinov <marinov@lcs.mit.edu>'
	if $author =~ m/Darko.*Marinov/i or $author =~ m/marinov@/;
    return 'Frederic VIVIEN <vivien@lcs.mit.edu>'
	if $author =~ m/Frederic.*VIVIEN/i or $author =~ m/vivien@/;
    return 'John Whaley <jwhaley@alum.mit.edu>'
	if $author =~ m/John.*Whaley/i or $author =~ m/jwhaley@/;
    return 'Karen K. Zee <kkz@alum.mit.edu>'
	if $author =~ m/Karen.*Zee/i or $author =~ m/kkz(ee)?@/;
    return 'Mark A. Foltz <mfoltz@ai.mit.edu>'
	if $author =~ m/Mark.*Foltz/i or $author =~ m/mfoltz@/;
    return 'Wes Beebee <wbeebee@mit.edu>'
	if $author =~ m/Wes.*Beebee/i or $author =~ m/wbeebee@/;
    return 'Robert Lee <rhlee@mit.edu>'
	if $author =~ m/Robert.*Lee/i or $author =~ m/rhlee@/;
    return 'Bryan Fink <wingman@mit.edu>'
	if $author =~ m/Bryan.*Fink/i or $author =~ m/wingman@/;
    print "UNKNOWN AUTHOR: $author in $f\n";
    return $author;
}


FILE: foreach $f (split(/\s+/,`make list`)) {
    next if ($f =~ /^Test/); # skip test code.
    next if ($f =~ /^Contrib/); # skip code not written by us.
    next if ($f =~ /^NOTES/); # this is just documentation
    next if ($f =~ /^Tools.Annotation/); # separate copyright.
    next if ($f =~ /CSAHack/); # not long for this world.
    # snarf input file.
    open(FH, "< $f") or die "Can't open $f for reading.\n";
    $_ = <FH>;
    close FH;

    # normalize author string.
    s/([@]author\s+)(.*?)(, based on)?$/$1.&saneauthors($2).$3/meg;

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
	unless (/[@]author\s+(.*)$/m) {
	    print "No author found for $f.\n";
	    next;
	}
	$author=$1;
	$author =~ s/, based on.*$//m; # clean up attribution case.
	# add (c) string.
	s|^(\s*package\s+)|$gnugpl1.$author.$gnugpl2.$1|me;
    }

    # normalize author in copyright string.
    s|^(// Copyright \(C\) [0-9]+ )(.*)$|$1.&saneauthors($2)|me;

    # add version string, if missing.
    unless (m/[@]version/) {
	s|^(.*)([@]author\s{1,2})(\s*)(.*)$|
	    "$1$2$3$4\n$1".q/@version /.$3.q/$I/.q/d$/ |me;
    }

    # write back to file.
    open(FH, "> $f") or die "Can't open $f for writing.\n";
    print FH $_;
    close FH;
}
