#!/usr/bin/perl

sub abbrev($classname); # we'll define this later.

foreach my $file (@ARGV) {
    $clsline=`egrep "class[[:space:]]+[A-Za-z0-9_]+[[:space:]]+extends" $file`;
    chomp $clsline;
    if ($clsline =~ m/class\s+(\w+)\s+extends\s+(\w+)\b/o) {
	$parent{$1} = $2;
    } else {
	#print "$file has no parent!\n";
    }
}
# look for top-level classes used by their children.
foreach $class (values %parent) {
    if (!exists $parent{$class}) {
	$parent{$class} = undef;
    }
}
# now print the visitor class
print "public abstract class IIR_Visitor {\n";
foreach $class (sort (keys %parent)) {
    my $varname = abbrev $class;
    if (!defined $parent{$class}) {
	print "    public abstract void visit($class $varname);\n\n";
    } else {
	print "    public void visit($class $varname) {\n";
	print "        visit( (".$parent{$class}.") $varname);\n";
	print "    }\n";
    }
}
print "}\n";

sub abbrev($classname) {
    my $classname = shift(@_);
    $classname =~ s/^IIR/I/;
    $classname =~ s/[a-z_]+//g;
    return lc $classname;
}
