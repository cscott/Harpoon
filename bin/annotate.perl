#!/usr/bin/perl
eval 'exec /usr/bin/perl -S $0 ${1+"$@"}'
    if $running_under_some_shell;
                        # this emulates #! processing on NIH machines.
                        # (remove #! line above if indigestible)
#--------------------------------------
use English;
#use strict 'vars';

# annotate javadoc with hyperlinks to classes in <code> blocks.

# first create canonical class lists, with urls.

sub jdk11classes {
    my @classes=split(' ', `ls doc/*.html | \
        egrep -v "^doc/(AllNames|index|packages|tree).html" | \
        egrep -v "^doc/(images|Package-|API_users_guide.html)" | \
        egrep -v "^doc/(TIMESTAMP|ChangeLog)" | \
        sed -e 's|^doc/||' -e 's/.html\$//' | sort -u`);
    die if $? != 0;
    my %cls2url;
    foreach my $cls (@classes) {
	$cls2url{$cls} = "./" . $cls . ".html";
    }
    return %cls2url;
}

sub jdk12classes {
    my $loc = shift(@_);
    my @raw; my $baseurl;
    if ($loc =~ m/^http:/ ) {
	$loc =~ s/package-list$//; $loc =~ s|/*$||;
	my $url="$loc/allclasses-frame.html";
	@raw=`lynx -source $url`;
	die "Fetch of $url unsuccessful" if join("\n",@raw)=~/[Nn]ot [Ff]ound/;
	$baseurl="$loc/";
    } else {
	my $file="$loc/allclasses-frame.html";
	die "$file not found" if ! -f $file;
	@raw=`cat $file`;
	$baseurl="./";
    }
    my %cls2url;
    foreach my $line (@raw) {
	if ($line =~ m|^.*A HREF.*\"([A-Za-z_0-9/]*/[A-Za-z0-9_.]*)\.html.*$|){
	    my $url=$1;
	    my $cls=$1; $cls =~ s|/|.|g;
	    $cls2url{$cls} = $baseurl . $url . ".html";
	}
    }
    return %cls2url;
}

sub fuzzhash {
    my %all = @_;
    my %cls2url;
    foreach my $key (keys %all) {
	$cls2url{$key}=$all{$key};
	my $fuzz=$key;
	while ($fuzz=~/\./) {
	    $fuzz =~ s/^[A-Za-z0-9_]*\.//;
	    $cls2url{$fuzz}=$all{$key};
	}
    }
    return %cls2url;
}

# ok, main routine.
my %all; # master fullclass->url map.

# get local classes; support either jdk 1.1 or 1.2
if ( ! -f "doc/package-list") { # jdk 1.1
    %all = ( %all, jdk11classes());
} else { # jdk 1.2
    %all = ( %all, jdk12classes("doc"));
}

# process command-line options (link, mainly)
while ($ARGV[0] =~ /^-/) {
    $_ = shift;
    if (/^-link$/) {
	%all = ( %all, jdk12classes(shift));
    } elsif (/^-u$/) {
	$cvsweb = shift;
    } else {
	die "Unrecognized switch: $_\n";
    }
}

# fuzz out to allow class abbreviations to be used.
%cls2url = fuzzhash(%all);

# grok the code, replacing the right stuff.
sub annotate {
    my $phrase = shift(@_);
    my $coded = "<CODE>" . $phrase . "</CODE>";
    if (exists $cls2url{$phrase}) {
	my $url = $cls2url{$phrase};
	$url =~ s|^./|$currentbase|;
	return "<A HREF=\"" . $url . "\">" . $coded . "</A>";
    } else {
	return $coded;
    }
}
sub srcdoc {
    my $phrase = shift(@_);
    return $phrase unless exists $cls2url{$phrase};
    my $url = $cls2url{$phrase};
    $url =~ s|^./|$currentbase."../srcdoc/"|e;
    return "<A HREF=\"" . $url . "\">" . $phrase . ".java</A>";
}
sub cvslog {
    my $file = shift(@_);
    my $version = shift(@_);
    return $version unless defined $cvsweb and exists $cls2url{$file};
    my $url = $cls2url{$file};
    return $version unless $url =~ m|^./harpoon/(.*)\.html$|;
    $url = "$cvsweb/Code/$1.java#rev$version";
    return "<A HREF=\"" . $url . "\">" . $version . "</A>";
}

$currentbase="./";
while (<>) {
    if (m|\"([./]*)stylesheet.css\"|) { $currentbase=$1; }
    s|<code>([A-Za-z0-9_.]*)</code>|annotate($1)|egi;
    s|(\$[I][d][:] )([A-Za-z0-9_]+).java(,v )([0-9.]+)( .*?\$)|
	$1.&srcdoc($2).$3.&cvslog($2,$4).$5|egi;
    print;
}
