#!/usr/bin/perl
use English;

my @lines; # line array
my @markupBf; # markup array, by line (before)
my @markupAf; # markup array, by line (after)

sub insertBefore {
    my $line = shift(@_);
    my $pos  = shift(@_);
    my $what = shift(@_);
    # get previous contents of this markup place.
    my $prev = ${$markupBf[$line]}{$pos};
    $prev = "" unless defined $prev;
    # and add to front.
    ${$markupBf[$line]}{$pos} = $what . $prev;
}
sub insertAfter {
    my $line = shift(@_);
    my $pos  = shift(@_);
    my $what = shift(@_);
    # get previous contents of this markup place.
    my $prev = ${$markupAf[$line]}{$pos};
    $prev = "" unless defined $prev;
    # and add to end.
    ${$markupAf[$line]}{$pos} = $prev . $what;
}
sub printMerged {
    # for each line...
    for ($i=0; $i<=$#lines; $i++) {
	# first merge markupAf with markupBf
	foreach my $k (keys %{$markupAf[$i]}) {
	    my $prev = ${$markupBf[$i]}{1+$k};
	    $prev="" unless defined $prev;
	    my $what = ${$markupAf[$i]}{$k};
	    die "Ack!" unless defined $what;
	    ${$markupBf[$i]}{1+$k} = $what . $prev;
	}
	# now insert text at the appropriate places in the line.
	my $l = $lines[$i];
	foreach my $loc (sort {$b <=> $a} keys %{$markupBf[$i]}) {
	    $l = substr($l,0,$loc) . ${$markupBf[$i]}{$loc} . substr($l,$loc);
	}
	# and print the result.
	print $l;
    }
}

my $filename = $ARGV[0];
open(INFILE, "<" . $filename) or die("Couldn't open $filename.\n");
@lines=<INFILE>;
close(INFILE);

# go through and substitute for special characters
for ($i=0; $i<=$#lines; $i++) {
    # substitute for &
    for ($j=index($lines[$i],"&"); $j>=0; $j=index($lines[$i],"&",$j+1)) {
	my $l = $lines[$i];
	$lines[$i] = substr($l,0,$j).";".substr($l,$j+1);
        &insertBefore($i,$j,"&amp");
    }
    # substitute for <
    for ($j=index($lines[$i],"<"); $j>=0; $j=index($lines[$i],"<",$j+1)) {
	my $l = $lines[$i];
	$lines[$i] = substr($l,0,$j) . ";" . substr($l,1+$j);
	&insertBefore($i,$j,"&lt");
    }
    # substitute for >
    for ($j=index($lines[$i],">"); $j>=0; $j=index($lines[$i],">",$j+1)) {
	my $l = $lines[$i];
	$lines[$i] = substr($l,0,$j).";".substr($l,$j+1);
        &insertBefore($i,$j,"&gt");
    }
}

# do syntax highlighting.
open(MARKUP, "java harpoon.Tools.Annotation.Main $filename |")
    or die("Couldn't do syntax markup on $filename.\n");
my @markdata=<MARKUP>;
close(MARKUP);
chomp(@markdata); # remove \n from each line.
for ($i=0; $i<=$#markdata; $i+=3) {
    my($rline,$rpos,$lline,$lpos)=split(/\s+/, $markdata[$i], 4);
    &insertBefore($rline-1,$rpos, $markdata[$i+1]);
    &insertAfter($lline-1,$lpos, $markdata[$i+2]);
}

# insert line numbers
for ($i=0; $i<=$#lines; $i++) {
    my $numstr = $i; while(length($numstr)<3) { $numstr=" $numstr"; }
    &insertBefore($i,0,"<A NAME=\"$i\"><FONT color=purple>$numstr</FONT></A> ");
}

# insert header and footer
&insertBefore(0,0,"<HTML><HEAD><TITLE>$filename</TITLE></HEAD>\n".
	          "<BODY BGCOLOR=white><PRE>");
&insertAfter($#lines,-1+length($lines[$#lines]),"</PRE></BODY></HTML>\n");

# print results
printMerged;
