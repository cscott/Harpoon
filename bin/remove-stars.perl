#!/usr/bin/perl
use English;

# Remove import *; statements from FLEX source files.

# find jikes version number, and use to pick 'right' dependency flag syntax
`sh -c "jikes --version 2>&1" | grep Version` =~ /^Version\s+(\S+)\s+/;
die "Can't determine jikes version number.\n" unless defined $1;
$jikesversion=$1;
$jikesdepflag=($jikesversion >= 1.10) ? "+DR" : "+M";

# use jikes to compute dependency lists.
$tmpfile=`mktemp /tmp/remove-stars.XXXXXX`; chomp $tmpfile;
`make JIKES_OPT=$jikesdepflag=$tmpfile jikes`;
@dependencies = `cat $tmpfile`;
`rm $tmpfile`;

# make a proper map with the data
$tmpstring = join('',@dependencies);
$tmpstring =~ s/\n\s+/ /g;
# some classes have leading ! symbols.  I think this means that only
# static fields are used.  In any case, get rid of the !s.
$tmpstring =~ s/\s[!]L/ L/g;
# replace slashes with dots in descriptors
do { } while ($tmpstring =~ s:\sL([\w\$\.]+)/([\w\$/]+);: L$1.$2;:g);
$tmpstring =~ s/\sL([\w\$\.]+);/ $1/g; # replace desc with class names
@dependencies = split('\n', $tmpstring); # now it's one line per target

foreach $target (@dependencies) {
    @fields = split(/\s+/, $target);
    $target = shift @fields;
    die "TARGET NOT A FILENAME" if ($target =~ /\..*\.java/);
    die "DEPENDENCY FORMAT ERROR" unless shift @fields eq ":";
    die "TARGETS NOT CLASS NAMES" if (join(' ',@fields) =~ m"/");
    @{$depmap{$target}} = @fields;
}

# get list of input files.
@sourcefiles = `make list`;
@sourcefiles = split(' ', join(' ',@sourcefiles));

# find import * statements in each one.
FILE: foreach $file (@sourcefiles) {
    open(FH, "< $file") or next FILE;
    # find the java source file corresponding to this input file.
    $canonfile = $file;
    $canonfile =~ s/\.[A-Za-z]+$/.java/;
    # look up the dependencies for this file
    @mydeps = @{$depmap{$canonfile}};
    # loop through the lines in the file.
    $linenum=0; $nlinenum=0; $header=0;
    while(<FH>) {
	$linenum++; $nlinenum++;
	if (/^\s*import\s+([A-Za-z_.]+)\.\*\s*;\s*$/) {
	    $impkg = $1; # package being imported.
	    if ($header==0) {
		$header++;
		print "diff -ru $file.orig $file\n";
		print "--- $file.orig\n+++ $file\n";
	    }
	    # build 'real' set of imports.
	    @imps = ();
	    foreach $d (@mydeps) {
		push @imps, ($d) if ($d =~ /^$impkg\.\w+\s*$/);
	    }
	    print "@@ -$linenum,1 +$nlinenum,".scalar(@imps)." @@\n";
	    print "-$_";
	    $nlinenum--;
	    foreach $d (sort @imps) {
		print "+import $d;\n"; $nlinenum++;
	    }
	}
    }
    close FH;
}
