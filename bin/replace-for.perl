#!/usr/bin/perl

unshift( @ARGV, '-' ) unless @ARGV;
my $file = shift( @ARGV );
open( ARG, "<$file" ) || die( "$0: can't open $file for reading ($!)\n" );

$ju = "(?: java \\s*[.]\\s* util \\s*[.]\\s* )?";
$fju = "(?: final\\s+)? $ju";
sub typecast1 {
    my $ty=shift;
    my $var=shift;
    return "$ty $var" if ($ty eq "java.lang.Object" || $ty eq "Object");
    return "Object $var"."O";
}
sub typecast2 {
    my $ty=shift;
    my $var=shift;
    my $nl=shift;
    return "" if ($ty eq "java.lang.Object" || $ty eq "Object");
    return "$ty $var = ($ty) $var"."O".$nl;
}

while((!$isEOF) && defined($line = <ARG>)) {
    # append 5 lines to pattern space if 'for' or 'Iterator' is on line.
    my $glom=0;
    $glom+=5 if $line =~ m/for|Iterator/s;
    for (my $i=0; $i<$glom; $i++) {
	my $nextline = <ARG>;
	if (defined($nextline)) {
	    $glom+=5 if $nextline =~ m/for|Iterator/s;
	    $line = "$line$nextline";
	} else {
	    $isEOF=1;
	    $glom=0;
	}
    }
    # now match:  this is non-parameterized version.
    $line =~ s/
        (for \s* \( ) # $1 is the 'for' part
	\s* $fju Iterator \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* = # $2 is the iter name
	# allow for an optional 'Arrays.asList(' in $3
        \s* ($ju Arrays \s* [.] \s* asList \s* \( \s* )?
	# grab the expression, taking off a closing paren if $3 matched
        ([^;]*[^; ]) \s* (?(3)\)) # (trimmed?) expression in $4
        \s* [.] \s* iterator \s* \( \s* \) \s* [;] # .iterator();
        \s* \2 \s* [.] \s* hasNext \s* \( \s* \) \s* [;] # it.hasNext()
	\s* ( \) \s* \{ \s* ) # $5 = whitespace and a brace; next line
	# now item type name ($6) and item variable name ($7)
        ([A-Za-z_][A-Za-z0-9_.]*) \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* =
	# typecast to type name
        \s* \( \s* \6 \s* \)
	# it.next()
        \s* \2 \s* [.] \s* next \s* \( \s* \) \s* ( [;] \s* ) # eol ($8)
	     /$1.&typecast1($6,$7)." : $4$5".&typecast2($6,$7,$8)/eogx;
    # this is the retarded 'while loop' non-parameterized version.
    $line =~ s/
	$fju Iterator \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* = # $1 is the iter name
	# allow for an optional 'Arrays.asList(' in $2
        \s* ($ju Arrays \s* [.] \s* asList \s* \( \s* )?
	# grab the expression, taking off a closing paren if $2 matched
        ([^;]*[^; ]) \s* (?(2)\)) # (trimmed?) expression in $3
        \s* [.] \s* iterator \s* \( \s* \) \s* [;] # .iterator();
	# while (it.hasNext()
	\s* while \s* \( \s* \1 \s* [.] \s* hasNext \s* \( \s* \) \s*
	\s* ( \) \s* \{ \s* ) # $4 = whitespace and a brace; next line
	# now item type name ($5) and item variable name ($6)
        ([A-Za-z_][A-Za-z0-9_.]*) \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* =
	# typecast to type name
        \s* \( \s* \5 \s* \)
	# it.next()
        \s* \1 \s* [.] \s* next \s* \( \s* \) \s* ( [;] \s* ) # eol ($7)
	     /"for (".&typecast1($5,$6)." : $3$4".&typecast2($5,$6,$7)/eogx;
    # this is the parameterized version
    $line =~ s/
	(for \s* \( ) # $1 is the 'for' part
	\s* $fju Iterator \s* [<] \s* (?: [?] \s+ extends \s+ )? (.*) [>] # $2 is the element type name
	\s* ([A-Za-z_][A-Za-z0-9_]*) \s* = # $3 is the iterator name
	# allow for an optional 'Arrays.asList(' in $4
	\s* ($ju Arrays \s* [.] \s* asList \s* \( \s* )?
	# grab the expression, taking off a closing paren if $4 matched
	([^;]*[^; ]) \s* (?(4)\)) # (trimmed?) expression in $5
	\s* [.] \s* iterator \s* \( \s* \) \s* [;] # .iterator();
	\s* \3 \s* [.] \s* hasNext \s* \( \s* \) \s* [;] # it.hasNext()
	\s* ( \) \s* \{ \s* ) # $6 = whitespace and a brace; next line
	# now item type name and item variable name ($7)
	\2 \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* =
	# it.next()
	\s* \3 \s* [.] \s* next \s* \( \s* \) \s* [;] \s*
	/$1$2 $7 : $5$6/ogx;
    # non-parameterized version, find use of 'new ArrayIterator'
    $line =~ s/
        (for \s* \( ) # $1 is the 'for' part
	\s* $fju Iterator \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* = # $2 is the iter name
	\s* new \s+ ArrayIterator \s* \( # new ArrayIterator(
        \s* ([^;]*[^; ]) # expression in $3
	\s* \) \s* [;] # close ArrayIterator instantiation.
        \s* \2 \s* [.] \s* hasNext \s* \( \s* \) \s* [;] # it.hasNext()
	\s* ( \) \s* \{ \s* ) # $4 = whitespace and a brace; next line
	# now item type name ($5) and item variable name ($6)
        ([A-Za-z_][A-Za-z0-9_.]*) \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* =
	# typecast to type name
        \s* \( \s* \5 \s* \)
	# it.next()
        \s* \2 \s* [.] \s* next \s* \( \s* \) \s* ( [;] \s* ) # eol ($7)
	     /$1.&typecast1($5,$6)." : $3$4".&typecast2($5,$6,$7)/eogx;
    # parameterized version of 'new ArrayIterator' pattern.
    $line =~ s/
	(for \s* \( ) # $1 is the 'for' part
	\s* $fju Iterator \s* [<] (.*) [>] # $2 is the element type name
	\s* ([A-Za-z_][A-Za-z0-9_]*) \s* = # $3 is the iterator name
	\s* new \s+ ArrayIterator \s* [<] \2 [>] \s* \( # new ArrayIterator(
        \s* ([^;]*[^; ]) # expression in $4
	\s* \) \s* [;] # close ArrayIterator instantiation.
	\s* \3 \s* [.] \s* hasNext \s* \( \s* \) \s* [;] # it.hasNext()
	\s* ( \) \s* \{ \s* ) # $5 = whitespace and a brace; next line
	# now item type name and item variable name ($6)
	\2 \s+ ([A-Za-z_][A-Za-z0-9_]*) \s* =
	# it.next()
	\s* \3 \s* [.] \s* next \s* \( \s* \) \s* [;] \s*
	/$1$2 $6 : $4$5/ogx;
    print $line;
}
close( ARG );
