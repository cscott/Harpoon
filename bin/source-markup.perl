#!/usr/bin/perl
# Source-markup.perl --- (c) 1999 C. Scott Ananian
# Licensed under the terms of the GNU GPL; see COPYING for details.
# $Id: source-markup.perl,v 1.2 2002-02-25 21:09:48 cananian Exp $

use English;
use Getopt::Std;

# configuration:

# tab size.  self-explanatory, no?
my $tabsize = 8;
# set $cvsblame to the full path to the cvsblame script if it is not
# in the same directory as this script.
undef $cvsblame; # full path to cvsblame.pl
# set alt_cvsblame non-zero to try alternate layout for cvsblame information.
undef $alt_cvsblame;

# end configuration

$progname = $0; # name of this script.
($cvsblame=$0) =~ s/([^\/]+)$/cvsblame.pl/ unless defined $cvsblame;

sub usage {
    die
"$progname: usage: [options] filename\n",
"   Options:\n",
"      -c           Don't try to annotate with CVS information\n",
"      -j           Don't try to syntax-color Java files\n",
"      -u <baseurl> Link CVS blame information to log browser CGI\n",
"      -h           Print help (this message)\n\n"
    ;
}

&usage if (!&getopts('cju:h'));
&usage if $opt_h; # help option
&usage if $#ARGV!=0; # too few or too many options.

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

my $isjava = ($filename=~m/\.java$/i);

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
        if (defined $2 && $isjava) { # unicode character (joy, joy)
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
if ($isjava && !$opt_j) {
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
}

# some URL and email address hackery
for ($i=0; $i<=$#lines; $i++) {
    while ($lines[$i] =~
	   m"(http://[/~A-Za-z0-9_#,.?&=%:+-]+)|(\w+@[A-Za-z0-9._]+)"g){
	my $endloc = pos $lines[$i];
	my $startloc=$endloc - (defined($1)?length($1):length($2));
        # don't include trailing period (if any) in HREF or email address
        $endloc-- if substr($lines[$i],$endloc-1,1) eq ".";
        my $match = substr($lines[$i],$startloc,$endloc-$startloc);
        &insertBefore($i,$startloc,"<A HREF=\"$match\">") if defined $1;
        &insertBefore($i,$startloc,"<A HREF=\"mailto:$match\">") if defined $2;
        &insertAfter($i,$endloc-1,"</A>");
    }
    while (defined($opt_u) && $lines[$i] =~ m/([\$]Id[^\$]*[\$])/g) {
	my $endloc = pos $lines[$i];
	my $startloc = $endloc - length($1);
	&insertBefore($i, $startloc, "<A HREF=\"$opt_u/Code/$filename\">");
	&insertAfter ($i, $endloc-1, "</A>");
    }
}

# cvsblame annotation (unless user doesn't want it)
if (!$opt_c) {
    my $optstr = "-u $opt_u" if defined $opt_u;
    open(CVSBLAME, "$cvsblame -qav $optstr $filename |")
	or die("Couldn't start cvsblame.\n");
    my @a; my @r; my @u; my $alen=0; my $rlen=0;
    while (<CVSBLAME>) {
	my ($author, $rev, $url) = m/(\S+)\s+(\S+)(?:\s+(\S+))?/;
	push(@a,$author); push(@r,$rev); push(@u,$url);
	$alen=length $author if $alen < length $author; 
	$rlen=length $rev    if $rlen < length $rev;
    }
    close(CVSBLAME);
    # only add cvsblame information if it is valid...
    if ($#lines==$#a && $#a==$#r) {
	my $laststr; my $color=1;
	for ($i=0; $i<=$#lines; $i++) {
	    # output properly padded string.
	    my $annstr=sprintf("%-*s %-*s",$alen,$a[$i],$rlen,$r[$i]);
	    # boldface if a local modification, else italics.
	    $annstr = ( $r[$i]=~m/LOCAL/ ) ?
		"<B>$annstr</B>" : "<I>$annstr</I>";
	    # alternate colors when source changes.
	    if ($i==0 || $annstr ne $laststr) {
		$laststr=$annstr; $color=!$color;
	    }
	    my $colorstr=$color?"darkorange":"darkorchid";
	    $annstr="<FONT COLOR=$colorstr>$annstr</FONT>";
	    # link to log browser
	    my $diffurl = $u[$i];
	    my $logurl = $diffurl; $logurl=~s/.diff.*?$/"#rev".$r[$i]/e;
	    $annstr = !$alt_cvsblame ?
		"<A HREF=\"$diffurl\">$annstr</A>" :
		"$annstr <A HREF=\"$diffurl\">D</A> <A HREF=\"$logurl\">L</A>"
		    if defined $opt_u && !($r[$i]=~m/LOCAL/);
	    # don't hyperlink spaces (looks ugly)
            $annstr =~ s|(\s+)(</([^>]*></)*[^>]*>)|$2$1|i;
	    # okay, make the annotation at the beginning of the line.
	    &insertBefore($i,0, "<FONT SIZE=-1>$annstr </FONT>");
	}
    }
}

# insert line numbers
for ($i=0; $i<=$#lines; $i++) {
    my $numstr = 1+$i;
    while(length($numstr)<length(1+$#lines)) { $numstr=" $numstr"; }
    &insertBefore($i,0,"<A NAME=\"$i\"><FONT color=saddlebrown SIZE=-1>$numstr</FONT></A> ");
}

# insert header and footer
&insertBefore(0,0,"<HTML><HEAD><TITLE>$filename</TITLE></HEAD>\n".
	          "<BODY BGCOLOR=\"azure\"><PRE>");
&insertAfter($#lines,-1+length($lines[$#lines]),"</PRE></BODY></HTML>\n");

# print results
printMerged;
