#!/usr/bin/perl
use strict;

sub abbrev($classname); # we'll define this later.
sub header(); # return the file header boilerplate
my %parent; # ancestor representation of inheritance tree.
my %toplevel; # set of top-level classes
my %children; # inverse of parent data structure.

foreach my $file (@ARGV) {
    my $clsline=`egrep "class[[:space:]]+[A-Za-z0-9_]+[[:space:]]+extends" $file`;
    chomp $clsline;
    if ($clsline =~ m/class\s+(\w+)\s+extends\s+(\w+)\b/o) {
	$parent{$1} = $2;
    } else {
	#print "$file has no parent!\n";
    }
}
# look for top-level classes used by their children.
foreach my $class (keys %parent) {
    if (!exists $parent{$parent{$class}}) {
	$toplevel{$parent{$class}} = undef;
    }
    push @{ $children{$parent{$class}} }, $class;
}

#print the header of the visitor class
print &header;
# now print the visitor class, using a worklist.
my @worklist;
push @worklist, (reverse (sort (keys %toplevel)));
print "public abstract class IIR_Visitor {\n";
while ($#worklist >= 0) {
    my $class = pop @worklist;
    my $varname = abbrev $class;
    if (!defined $parent{$class}) {
	print "    public abstract void visit($class $varname);\n\n";
    } else {
	print "    public void visit($class $varname) {\n";
	print "        visit( (".$parent{$class}.") $varname);\n";
	print "    }\n";
    }
    # recurse.
    if (exists $children{$class}) {
	push @worklist, (reverse (sort @{ $children{$class} }));
#	unshift @worklist, (sort @{ $children{$class} });
    }
}
print "}\n";

sub abbrev($classname) {
    my $classname = shift(@_);
    $classname =~ s/^IIR/I/;
    $classname =~ s/[a-z_]+//g;
    return lc $classname;
}

sub header() {
    my $date = `date`; chomp $date;
    return <<EOF;
// IIR_Visitor.java, created $date by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian\@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The <code>IIR_Visitor</code> class is part of the implementation of
 * the "visitor" design pattern.
 *
 * \@author  C. Scott Ananian <cananian\@alumni.princeton.edu>
 * \@version \$Id\$
 */
EOF
}
