#!/usr/bin/perl

$debug = 0;

sub formatassert {
    local($assert, $expr) = @_;
    print "FORMAT /$assert/-/$expr/\n" if $debug;
    local($first, $second) = &splittuple($expr);
    $first="($first)" if ($first=~m/:[^\)]*$/s);
    $second="($second)" if ($second=~m/:[^\)]*$/s);
    return "assert $first;" unless $second ne "";
    return "assert $first : $second;";
}

# input is XY(Z(ASD)(X<S)) , foo.
# we want to find first ',' which is *not* in a parenthesized expression.

# Given '(X)Y' returns '(X)' and 'Y', even if X has further nested parens.
sub chompparen {
    my $str = shift @_;
    my $result = "(";
    $str =~ s/^\(//s;
    # until we find the close paren...
    while ($str !~ m/^\)/s && $str ne "") {
	print "1 /$result/-/$str/\n" if $debug;
	# look for open paren or string
	# remove all chars up to ( or ) or " or '
	$result .= $1 if $str =~ s/^([^\(\"\'\)]+)//s;
	# remove a parenthesized blob, if that's what we've got next.
	print "2 /$result/-/$str/\n" if $debug;
	if ($str =~ m/^\(/s) {
	    my ($a, $b) = &chompparen($str);
	    $result.=$a; $str=$b;
	}
	print "3 /$result/-/$str/\n" if $debug;
	# remove a string, if that's what we've got next.
	if ($str =~ m/^[\"\']/s) {
	    my ($a, $b) = &chompstring($str);
	    $result.=$a; $str=$b;
	}
	print "4 /$result/-/$str/\n" if $debug;
    }
    # done!
    $str =~ s/^\)//s;
    $result .=")";
    return ($result, $str);
}

# given "foo"bar returns "foo" and bar, even if foo has escapes.
# also does 'f'oo.
sub chompstring {
    my $str = shift @_;
    $str =~ m/^((\"([^\"\\]|(\\.))*\")|(\'([^\'\\]|(\\.))\'))(.*)$/s;
    return ($1, $8);
}

# X, Z; returns X and Z --- even if X and Z are parenthesized or
# contain strings.
sub splittuple {
    my $str = shift @_;
    my $result = "";
    while ($str !~ m/^,/s && $str ne "") {
	print "A /$result/-/$str/\n" if $debug;
	# remove all chars up to ( or , or " or '
	$result .= $1 if $str =~ s/^([^,\(\"\']+)//s;
	print "B /$result/-/$str/\n" if $debug;
	# remove a parenthesized blob, if that's what we've got next.
	if ($str =~ m/^\(/s) {
	    my ($a, $b) = &chompparen($str);
	    $result.=$a; $str=$b;
	}
	print "C /$result/-/$str/\n" if $debug;
	# remove a string, if that's what we've got next.
	if ($str =~ m/^[\"\']/s) {
	    my ($a, $b) = &chompstring($str);
	    $result.=$a; $str=$b;
	}
	print "D /$result/-/$str/\n" if $debug;
    }
    # done!
    $str =~ s/^,\s*//s; $str =~ s/\s+$//s;
    $result =~ s/^\s+//s; $result =~ s/\s+$//s;
    return ($result, $str);
}

while (<>) {
    # replace old assert with assert
    s/\b(harpoon\.Util\.)?Util\.ASSERT\b/assert/g;
    # make sure we've got the whole statement. (a little bit of a hack)
    while (/\bassert\s*(\(((\"([^\"\\]|(\\.))*\")|(\'([^\'\\]|(\\.))\')|[^;\"\'])*)?$/s) {
	$_ .= <>;
    }
    # re-format the parens.
    s/\b(assert\s*)\((((\"([^\"\\]|(\\.))*\")|(\'([^\'\\]|(\\.))\')|[^;\"\'])*)\)\s*;/&formatassert($1,$2)/se;
    print "X" if $debug;
    print $_;
}
