#!/usr/bin/perl
use English;

# configuration:
my $tabsize = 8;
# end configuration

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

# markup up tabs and unicode characters (remember java pre-processes them)
# leaving character positions correct.
for ($i=0; $i<=$#lines; $i++) {
    my $tabfixup=0;
    while ($lines[$i] =~ m/(\t)|(\\u*[0-9a-fA-F]{4})/g) {
	my $endloc = pos $lines[$i]; # pos is the place match *ended*
	my $sloc = $endloc - (defined($1)?length($1):length($2));
	my $l = $lines[$i]; # convenience
	if (defined $1) { # found a tab
	    my $nspaces = $tabsize-(($sloc-$tabfixup) % $tabsize);
            $tabfixup = $endloc; # next char on a tab boundary now.
            $lines[$i] = substr($l,0,$sloc)." ".substr($l,$endloc);#insert 1 sp
            insertAfter($i,$sloc," ") while --$nspaces > 0;#insert nspaces-1 sp
        }
        if (defined $2) { # unicode character (joy, joy)
	    # compress to a single char in our representation.
	    my $strlen = $endloc-$sloc;
	    $lines[$i] = substr($l,0,$sloc+1).substr($l,$endloc);
            insertAfter($i,$sloc,substr($l,$sloc+1,$strlen-1));
            $tabfixup-=$strlen-1;
            $endloc=$sloc+1; # compress compress
	}
        pos($lines[$i]) = $endloc; # make things perfectly clear to perl
    }
}

# go through and substitute for special characters
for ($i=0; $i<=$#lines; $i++) {
    while ($lines[$i] =~ m/(&)|(<)|(>)/g) {
	my $loc = -1 + pos $lines[$i];
	$lines[$i] = substr($lines[$i],0,$loc).";".substr($lines[$i],$loc+1);
        &insertBefore($i,$loc,"&amp") if defined $1;
	&insertBefore($i,$loc,"&lt")  if defined $2;
        &insertBefore($i,$loc,"&gt")  if defined $3;
        pos($lines[$i]) = 1 + $loc; # make things perfectly clear to perl
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

# some URL and email address hackery
for ($i=0; $i<=$#lines; $i++) {
    while ($lines[$i] =~
	   m"(http://[/A-Za-z0-9_#,.?&=%:;+-]+)|(\w+@[A-Za-z0-9._]+)"g){
	my $endloc = pos $lines[$i];
	my $startloc=$endloc - (defined($1)?length($1):length($2));
        &insertBefore($i,$startloc,"<A HREF=\"$1\">") if defined $1;
        &insertBefore($i,$startloc,"<A HREF=\"mailto:$2\">") if defined $2;
        &insertAfter($i,$endloc-1,"</A>");
    }
}

# insert line numbers
for ($i=0; $i<=$#lines; $i++) {
    my $numstr = 1+$i; while(length($numstr)<3) { $numstr=" $numstr"; }
    &insertBefore($i,0,"<A NAME=\"$i\"><FONT color=purple>$numstr</FONT></A> ");
}

# insert header and footer
&insertBefore(0,0,"<HTML><HEAD><TITLE>$filename</TITLE></HEAD>\n".
	          "<BODY BGCOLOR=\"azure\"><PRE>");
&insertAfter($#lines,-1+length($lines[$#lines]),"</PRE></BODY></HTML>\n");

# print results
printMerged;
