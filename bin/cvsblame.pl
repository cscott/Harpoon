#!/usr/bin/perl --
# -*- Mode: perl; indent-tabs-mode: nil -*-
#
# The contents of this file are subject to the Netscape Public License
# Version 1.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.mozilla.org/NPL/
#
# Software distributed under the License is distributed on an "AS IS"
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# License for the specific language governing rights and limitations
# under the License.
#
# The Original Code is the Bonsai CVS tool.
#
# The Initial Developer of the Original Code is Netscape Communications
# Corporation. Portions created by Netscape are Copyright (C) 1998
# Netscape Communications Corporation. All Rights Reserved.

##############################################################################
#
# cvsblame.pl - Shamelessly adapted from Scott Furman's cvsblame script
#                 by Steve Lamm (slamm@netscape.com)
#               Restored to its original glory by C. Scott Ananian
#             - Annotate each line of a CVS file with its author,
#               revision #, date, etc.
# 
##############################################################################

use Time::Local qw(timegm);         # timestamps
use POSIX qw(strftime);             # human-readable dates
use Getopt::Std;		    # standard command-line processing

$debug = 0;
$opt_m = 0 unless (defined($opt_m));

# Extract base part of this script's name
($progname = $0) =~ /([^\/]+)$/;

sub cvsblame_init {
    # Use default formatting options if none supplied
    if (!$opt_A && !$opt_a && !$opt_d && !$opt_v) {
        $opt_a = 1;
        $opt_v = 1;
    }

    $time = time;
    $SECS_PER_DAY = 60 * 60 * 24;

    # Timestamp threshold at which annotations begin to occur, if -m option present.
    $opt_m_timestamp = $time - $opt_m * $SECS_PER_DAY;
}

# Generic traversal of a CVS tree.  Invoke callback function for
# individual directories that contain CVS files.
sub traverse_cvs_tree {
    my ($dir, $callback, $nlink) = @_;
    my ($dev, $ino, $mode, $subcount);

    # Get $nlink for top-level directory
    ($dev, $ino, $mode, $nlink) = stat($dir) unless $nlink;

    # Read directory
    opendir(DIR, $dir) || die "Can't open $dir\n";
    my (@filenames) = readdir(DIR);
    closedir(DIR);

    return if ! -d "$dir/CVS";

    &{$callback}($dir);

    # This dir has subdirs
    if ($nlink != 2) {
        $subcount = $nlink - 2; # Number of subdirectories
        for (@filenames) {
            last if $subcount == 0;
            next if $_ eq '.';
            next if $_ eq '..';
            next if $_ eq 'CVS';
            $name = "$dir/$_";

            ($dev, $ino, $mode, $nlink) = lstat($name);
            next unless -d _;
            if (-x _ && -r _) {
                print STDERR "$progname: Entering $name\n";
                &traverse_cvs_tree($name, $callback, $nlink);
            } else {
                warn("Couldn't chdir to $name");
            }
            --$subcount;
        }
    }
}

# object encapsulation.
{
    # RevEntry object holds cvsblame information for a single line.
    package RevEntry;
    sub new {
        my ($proto, $rcsobj,$revision) = @_;
        my $class = ref($proto) || $proto;
        my $self = {
            RCSOBJ => $rcsobj, REVISION => $revision,
        };
        bless ($self, $class);
        return $self;
    }
    sub _get_or_set_ { # convenience
        my $field = shift; my $self = shift;
        if (@_) { $self->{$field} = shift; }
        return $self->{$field};
    }
    # field accessors.
    sub rcsobj   { &_get_or_set_("RCSOBJ", @_); }
    sub revision { &_get_or_set_("REVISION", @_); }
    # useful properties.
    sub author   {
        my $self = shift;
        return $self->rcsobj->{REVISION_AUTHOR}->{$self->revision};
    }
    sub pathname {
        my $self = shift;
        return $self->rcsobj->{PATHNAME};
    }
    sub ctime {
        my $self = shift;
        return $self->rcsobj->{REVISION_CTIME}->{$self->revision};
    }
    sub age {
        my $self = shift;
        return $self->rcsobj->{REVISION_AGE}->{$self->revision};
    }
    sub timestamp {
        my $self = shift;
        return $self->rcsobj->{TIMESTAMP}->{$self->revision};
    }
}

# Consume one token from the already opened RCSFILE filehandle.
# Unescape string tokens, if necessary.
sub get_token {
    # Erase all-whitespace lines.
    $line_buffer = '' unless (defined($line_buffer));
    while ($line_buffer =~ /^$/) {
        die ("Unexpected EOF") if eof(RCSFILE);
        $line_buffer = <RCSFILE>;
        $line_buffer =~ s/^\s+//; # Erase leading whitespace
    }
    
    # A string of non-whitespace characters is a token ...
    return $1 if ($line_buffer =~ s/^([^;@][^;\s]*)\s*//o);

    # ...and so is a single semicolon ...
    return ';' if ($line_buffer =~ s/^;\s*//o);

    # ...or an RCS-encoded string that starts with an @ character.
    $line_buffer =~ s/^@([^@]*)//o;
    $token = $1;

    # Detect single @ character used to close RCS-encoded string.
    while ($line_buffer !~ /@/o || # Short-circuit optimization
           $line_buffer !~ /([^@]|^)@([^@]|$)/o) {
        $token .= $line_buffer;
        die ("Unexpected EOF") if eof(RCSFILE);
        $line_buffer = <RCSFILE>;
    }
           
    # Retain the remainder of the line after the terminating @ character.
    $i = rindex($line_buffer, '@');
    $token .= substr($line_buffer, 0, $i);
    $line_buffer = substr($line_buffer, $i + 1);

    # Undo escape-coding of @ characters.
    $token =~ s/@@/@/og;
        
    # Digest any extra blank lines.
    while (($line_buffer =~ /^$/) && !eof(RCSFILE)) {
	$line_buffer = <RCSFILE>;
    }
    return $token;
}

# Consume a token from RCS filehandle and ensure that it matches
# the given string constant.
sub match_token {
    my ($match) = @_;

    my ($token) = &get_token;
    die "Unexpected parsing error in RCS file.\n",
        "Expected token: $match, but saw: $token\n"
            if ($token ne $match);
}

# Push RCS token back into the input buffer.
sub unget_token {
    my ($token) = @_;
    $line_buffer = $token . " " . $line_buffer;
}

# Parses "administrative" header of RCS files, setting these fields in
# the supplied $rcsobj:
# 
# $head_revision           -- Revision for which cleartext is stored
# $principal_branch
# $file_description
# %revision_symbolic_name  -- mapping from numerical revision # to symbolic tag
# %tag_revision            -- mapping from symbolic tag to numerical revision #
#
sub parse_rcs_admin {
    my ($rcsobj) = shift;
    my ($token, $tag);

    # Undefine variables, because we may have already read another RCS file
    undef $rcsobj->{TAG_REVISION};
    undef $rcsobj->{REVISION_SYMBOLIC_NAME};

    while (1) {
        # Read initial token at beginning of line
        $token = &get_token(RCSFILE);

        # We're done once we reach the description of the RCS tree
        if ($token =~ /^\d/o) {
            &unget_token($token);
            return;
        }
        
#       print "token: $token\n";

        if ($token eq "head") {
            $rcsobj->{HEAD_REVISION} = &get_token;
            $rcsobj->{TAG_REVISION}->{HEAD} = $rcsobj->{HEAD_REVISION};
            &match_token(';');         # Eat semicolon
        } elsif ($token eq "branch") {
            $rcsobj->{PRINCIPAL_BRANCH} = &get_token;
            &match_token(';');         # Eat semicolon
        } elsif ($token eq "symbols") {

            # Create an associate array that maps from tag name to
            # revision number and vice-versa.
            while (($tag = &get_token) ne ';') {
                my ($tag_name, $tag_revision) = split(':', $tag);
                
                $rcsobj->{TAG_REVISION}->{$tag_name} = $tag_revision;
                $rcsobj->{REVISION_SYMBOLIC_NAME}->{$tag_revision} = $tag_name;
            }
        } elsif ($token eq "comment") {
            $rcsobj->{FILE_DESCRIPTION} = &get_token;
            &get_token;         # Eat semicolon

        # Ignore all these other fields - We don't care about them.         
        } elsif (($token eq "locks")  ||
                 ($token eq "strict") ||
                 ($token eq "expand") ||
                 ($token eq "access")) {
            (1) while (&get_token ne ';');
        } else {
            warn ("Unexpected RCS token: $token\n");
        }
    }

    die "Unexpected EOF";
}

# Construct associative arrays that represent the topology of the RCS tree
# and other arrays that contain info about individual revisions.
#
# Associative arrays are created for the following fields in the
# supplied $rcsobj; all arrays are keyed by revision_number.
#   %revision_date     -- e.g. "96.02.23.00.21.52"
#   %timestamp         -- seconds since 12:00 AM, Jan 1, 1970 GMT
#   %revision_author   -- e.g. "tom"
#   %revision_branches -- descendant branch revisions, separated by spaces,
#                         e.g. "1.21.4.1 1.21.2.6.1"
#   %prev_revision     -- revision number of previous *ancestor* in RCS tree.
#                         Traversal of this array occurs in the direction
#                         of the primordial (1.1) revision.
#   %prev_delta        -- revision number of previous revision which forms the
#                         basis for the edit commands in this revision.
#                         This causes the tree to be traversed towards the
#                         trunk when on a branch, and towards the latest trunk
#                         revision when on the trunk.
#   %next_delta        -- revision number of next "delta".  Inverts %prev_delta.
#
# Also creates %last_revision, keyed by a branch revision number, which
# indicates the latest revision on a given branch,
#   e.g. $last_revision{"1.2.8"} == 1.2.8.5
#
sub parse_rcs_tree {
    my ($rcsobj) = shift;
    my ($revision, $date, $author, $branches, $next);
    my ($branch, $is_trunk_revision, $token);

    # Undefine variables, because we may have already read another RCS file
    undef $rcsobj->{TIMESTAMP};
    undef $rcsobj->{REVISION_AGE};
    undef $rcsobj->{REVISION_AUTHOR};
    undef $rcsobj->{REVISION_BRANCHES};
    undef $rcsobj->{REVISION_CTIME};
    undef $rcsobj->{REVISION_DATE};
    undef $rcsobj->{REVISION_STATE};
    undef $rcsobj->{PREV_REVISION};
    undef $rcsobj->{PREV_DELTA};
    undef $rcsobj->{NEXT_DELTA};
    undef $rcsobj->{LAST_REVISION};

    while (1) {
        $revision = &get_token;

        # End of RCS tree description ?
        if ($revision eq 'desc') {
            &unget_token($revision);
            last;
        }

        $is_trunk_revision = ($revision =~ /^[0-9]+\.[0-9]+$/);
        
        $rcsobj->{TAG_REVISION}->{$revision} = $revision;
        ($branch) = $revision =~ /(.*)\.[0-9]+/o;
        $rcsobj->{LAST_REVISION}->{$branch} = $revision;

        # Parse date
        &match_token('date');
        $date = &get_token;
        $rcsobj->{REVISION_DATE}->{$revision} = $date;
        &match_token(';');

        # Convert date into timestamp
        my @date_fields = reverse(split(/\./, $date));
        $date_fields[4]--;      # Month ranges from 0-11, not 1-12
        $rcsobj->{TIMESTAMP}->{$revision} = timegm(@date_fields);

        # Pretty print the date string
        my $formated_date = strftime("%d %b %Y %H:%M",
                                     localtime($rcsobj->{TIMESTAMP}->{$revision}));
        $rcsobj->{REVISION_CTIME}->{$revision} = $formated_date;

        # Save age
        $rcsobj->{REVISION_AGE}->{$revision} =
            ($time - $rcsobj->{TIMESTAMP}->{$revision}) / $SECS_PER_DAY;

        # Parse author
        &match_token('author');
        $author = &get_token;
        $rcsobj->{REVISION_AUTHOR}->{$revision} = $author;
        &match_token(';');

        # Parse state;
        &match_token('state');
        $rcsobj->{REVISION_STATE}->{$revision} = &get_token;
        &match_token(';');

        # Parse branches
        &match_token('branches');
        $branches = '';
        while (($token = &get_token) ne ';') {
            $rcsobj->{PREV_REVISION}->{$token} = $revision;
            $rcsobj->{PREV_DELTA}->{$token} = $revision;
            $branches .= "$token ";
        }
        $rcsobj->{REVISION_BRANCHES}->{$revision} = $branches;

        # Parse revision of next delta in chain
        &match_token('next');
        $next = '';
        if (($token = &get_token) ne ';') {
            $next = $token;
            &get_token;         # Eat semicolon
            $rcsobj->{NEXT_DELTA}->{$revision} = $next;
            $rcsobj->{PREV_DELTA}->{$next} = $revision;
            if ($is_trunk_revision) {
                $rcsobj->{PREV_REVISION}->{$revision} = $next;
            } else {
                $rcsobj->{PREV_REVISION}->{$next} = $revision;
            }
        }

        if ($debug >= 3) {
            print "revision = $revision\n";
            print "date     = $date\n";
            print "author   = $author\n";
            print "branches = $branches\n";
            print "next     = $next\n\n";
        }
    }
    return undef;
}

# sets the $rcs_file_description field in the supplied $rcsobj.
sub parse_rcs_description {
    my $rcsobj = shift;
    &match_token('desc');
    $rcsobj->{RCS_FILE_DESCRIPTION} = &get_token;
}

# Construct associative arrays containing info about individual revisions.
#
# Associative arrays are created for the following fields in $rcsobj; all
# arrays are keyed by revision number.
#   %revision_log        -- log message
#   %revision_deltatext  -- Either the complete text of the revision,
#                           in the case of the head revision, or the
#                           encoded delta between this revision and another.
#                           The delta is either with respect to the successor
#                           revision if this revision is on the trunk or
#                           relative to its immediate predecessor if this
#                           revision is on a branch.
sub parse_rcs_deltatext {
    my $rcsobj = shift;
    undef $rcsobj->{REVISION_LOG};
    undef $rcsobj->{REVISION_DELTATEXT};

    while (!eof(RCSFILE)) {
        my $revision = &get_token;
        print "Reading delta for revision: $revision\n" if ($debug >= 3);
        &match_token('log');
        $rcsobj->{REVISION_LOG}->{$revision} = &get_token;
        &match_token('text');
        $rcsobj->{REVISION_DELTATEXT}->{$revision} = &get_token;
    }
}

# Reads and parses complete RCS file from already-opened RCSFILE descriptor.
sub parse_rcs_file {
    my $rcsobj = {};
    print "Reading RCS admin...\n" if ($debug >= 2);
    &parse_rcs_admin($rcsobj);
    print "Reading RCS revision tree topology...\n" if ($debug >= 2);
    &parse_rcs_tree($rcsobj);
    print "Reading RCS file description...\n" if ($debug >= 2);
    &parse_rcs_description($rcsobj);
    print "Reading RCS revision deltas...\n" if ($debug >= 2);
    &parse_rcs_deltatext($rcsobj);
    print "Done reading RCS file...\n" if ($debug >= 2);
    $rcsobj;
}

# Construct an ordered list of ancestor revisions to the given
# revision, starting with the immediate ancestor and going back
# to either the optionally specified revision or the primordial
# revision (1.1).
#
# Note: The generated path does not traverse the tree the same way
#       that the individual revision deltas do.  In particular,
#       the path traverses the tree "backwards" on branches.

sub ancestor_revisions {
    my ($rcsobj, $revision, $stop_at) = @_;
    my (@ancestors);
     
    while ($revision) {
        push(@ancestors, $revision);
        last if defined $stop_at && $revision eq $stop_at;
        $revision = $rcsobj->{PREV_REVISION}->{$revision};
    }

    return @ancestors;
}

# Extract the given revision from the digested RCS file.
# (Essentially the equivalent of cvs up -rXXX)
sub extract_revision {
    my ($rcsobj, $revision) = @_;
    my (@path);

    # Compute path through tree of revision deltas to most recent trunk revision
    while ($revision) {
        push(@path, $revision);
        $revision = $rcsobj->{PREV_DELTA}->{$revision};
    }
    @path = reverse(@path);
    # Get rid of head revision
    my $head_revision = $rcsobj->{HEAD_REVISION};
    die "Inconsistent path" unless shift @path eq $head_revision;

    # Get complete contents of head revision
    my (@text) = split(/^/, $rcsobj->{REVISION_DELTATEXT}->{$head_revision});

    # Iterate, applying deltas to previous revision
    foreach my $revision (@path) {
        $adjust = 0;
        my @diffs = split(/^/, $rcsobj->{REVISION_DELTATEXT}->{$revision});

        my $add_lines_remaining=0;
        foreach my $command (@diffs) {
            if ($add_lines_remaining > 0) {
                # Insertion lines from a prior "a" command.
                splice(@text, $start_line + $adjust,
                       0, $command);
                $add_lines_remaining--;
                $adjust++;
            } elsif ($command =~ /^d(\d+)\s(\d+)/) {
                # "d" - Delete command
                ($start_line, $count) = ($1, $2);
                splice(@text, $start_line + $adjust - 1, $count);
                $adjust -= $count;
            } elsif ($command =~ /^a(\d+)\s(\d+)/) {
                # "a" - Add command
                ($start_line, $count) = ($1, $2);
                $add_lines_remaining = $count;
            } else {
                die "Error parsing diff commands";
            }
        }
    }

    return @text;
}

# open a cvs file, possibly using a remote repository access method.
# currently only :ext: is supported, not any of the :?server: methods.
sub open_cvs_file {
    local *HANDLE = shift;
    my $pathname = shift;

    # A file that exists only on the branch, not on the trunk, is found
    # in the Attic subdir.
    my $atticname;
    if ($pathname =~ m:^(.*)/([^/]*)$:) {
        $atticname = $1 . '/Attic/' . $2;
    } else {
        $atticname = 'Attic/' . $pathname;
    }
    # assume name is correct (file not in Attic)
    $rcs_truename{$pathname}=$pathname;
    # Try remote repository access methods, if applicable.
    if ($pathname =~ m/^:([^:]*):(?:([^:@]*)@)?([^:@]*):(.*)$/) {
        my ($method,$user,$host,$path) = ($1, $2, $3, $4);
        if ($method eq "ext") { # currently the only supported protocol.
            my $rsh = $ENV{'CVS_RSH'};
            $rsh = "rsh" unless defined $rsh;
            $user = "-l $user" if defined $user;
            my $saveattic = $atticname;
            $atticname =~ s/^:[^:]*:[^:]*://;
            my $cmd="if [ -r $path ]; then echo foo; cat $path; else echo Attic; cat $atticname; fi";
            $cmd = "$rsh $host $user \"sh -c '$cmd'\"";
            open(HANDLE, $cmd." |") or return undef;
            my $firstline = <HANDLE>;
            $rcs_truename{$pathname}=$saveattic if $firstline =~ m/Attic/;
            return 1;
        }
        # remote access protocol not supported. <sigh>
        return undef;
    }
    # local access.
    return open(HANDLE, "< $pathname") if (-r $pathname);
    $rcs_truename{$pathname}=$atticname;
    return open(HANDLE, "< $atticname"); # fall back to attic.
}

# create a simple revision map given the map for a previous revision.
# no funky MERGE or RENAME stuff is done.
sub simple_rev_map {
    # args in:  $want_rev - desired revision
    #           $have_rev - revision I have
    #           @have_map - revision map for the revision I have
    # args out: @want_map - revision map for the desired revision
    my ($rcsobj, $want_rev, $have_rev, @have_map) = @_;
    my $skip = 0;

    die "improper arguments" unless defined $want_rev && defined $have_rev;

    # Play the delta edit commands *backwards* from the supplied
    # revision forward, but rather than applying the deltas to the text of
    # each revision, apply the changes to an array of revision numbers.
    # This creates a "revision map" -- an array where each element
    # represents a line of text in the given revision but contains only
    # the revision number in which the line was introduced rather than
    # the line text itself.
    #
    # Note: These are backward deltas for revisions on the trunk and
    # forward deltas for branch revisions.

    my @ancestors = &ancestor_revisions($rcsobj, $want_rev, $have_rev);
    # delete bits of revision tree to implement @REVERT@
    for (my $i=0; $i <= $#ancestors; $i++) {
        my $r = $ancestors[$i];
        next if $opt_T; # ignore REVERT tags
        next unless ($rcsobj->{REVISION_LOG}->{$r} =~
                     m/[@]REVERT:\s*([^@]+)\s*[@]/);
        my ($pat, $revertrev) = split(/\s+/, $1);
        # ignore directive unless well formed.
        next unless defined $pat && defined $revertrev;
        # ignore directive unless the filename pattern matches
        next unless ($rcsobj->{PATHNAME} =~ m/$pat/);
        # now find the revision to revert to
        my $j;
        for ($j=$i+1; $j <= $#ancestors; $j++) {
            last if $ancestors[$j] eq $revertrev;
        }
        next unless $j>=0; # oops, couldn't find the revertrev.
        my $is_trunk_revision_i = ($r =~ /^[0-9]+\.[0-9]+$/);
        my $is_trunk_revision_j = ($revertrev =~ /^[0-9]+\.[0-9]+$/);
        if ($is_trunk_revision_i && $is_trunk_revision_j) {
            # remove (i,j] chunk from ancestors list
            splice(@ancestors,$i+1,$j-$i); next;
            # XXX: note a @REVERT@ in $revertrev itself will be missed. =(
        }
        if ((!$is_trunk_revision_i) && (!$is_trunk_revision_j)) {
            # remove [i,j) chunk from ancestors list
            splice(@ancestors,$i,$j-$i); redo;
            # a @REVERT@ in $revertrev is handled correctly in this case.
        }
    }
    my $last_rev = pop @ancestors;             # Remove $have_rev
    foreach my $revision (reverse @ancestors) {
        my $is_trunk_revision = ($revision =~ /^[0-9]+\.[0-9]+$/);

        if ($is_trunk_revision) {
            my @diffs = split(/^/, $rcsobj->{REVISION_DELTATEXT}->{$last_rev});

            # Revisions on the trunk specify deltas that transform a
            # revision into an earlier revision, so invert the translation
            # of the 'diff' commands.
            foreach my $command (@diffs) {
                if ($skip > 0) {
                    $skip--;
                } else {
                    if ($command =~ /^d(\d+)\s(\d+)$/) { # Delete command
                        my ($start_line, $count) = ($1, $2);

                        $#temp = -1;
                        while ($count--) {
                            push(@temp, RevEntry->new($rcsobj, $revision));
                        }
                        splice(@have_map, $start_line - 1, 0, @temp);
                    } elsif ($command =~ /^a(\d+)\s(\d+)$/) { # Add command
                        my ($start_line, $count) = ($1, $2);
                        splice(@have_map, $start_line, $count);
                        $skip = $count;
                    } else {
                        die "Error parsing diff commands";
                    }
                }
            }
        } else {
            # Revisions on a branch are arranged backwards from those on
            # the trunk.  They specify deltas that transform a revision
            # into a later revision.
            my $adjust = 0;
            my @diffs = split(/^/, $rcsobj->{REVISION_DELTATEXT}->{$revision});
            foreach my $command (@diffs) {
                if ($skip > 0) {
                    $skip--;
                } else {
                    if ($command =~ /^d(\d+)\s(\d+)$/) { # Delete command
                        my ($start_line, $count) = ($1, $2);
                        splice(@have_map, $start_line + $adjust - 1, $count);
                        $adjust -= $count;
                    } elsif ($command =~ /^a(\d+)\s(\d+)$/) { # Add command
                        my ($start_line, $count) = ($1, $2);

                        $skip = $count;
                        $#temp = -1;
                        while ($count--) {
                            push(@temp, RevEntry->new($rcsobj, $revision));
                        }
                        splice(@have_map, $start_line + $adjust, 0, @temp);
                        $adjust += $skip;
                    } else {
                        die "Error parsing diff commands";
                    }
                }
            }
        }
        $last_rev = $revision;
    }    

    # return new have_map
    @have_map;
}

sub cvs_make_rev_map {
    my ($rcsobj, $rcs_pathname, $revision) = @_;
    my @revision_map;

    # The primordial revision is not always 1.1!  Go find it.
    my $primordial = $revision;
    while (exists($rcsobj->{PREV_REVISION}->{$primordial}) &&
           $rcsobj->{PREV_REVISION}->{$primordial} ne "") {
        $primordial = $rcsobj->{PREV_REVISION}->{$primordial};
    }

    # Figure out how many lines were in the primordial, i.e. version 1.1,
    # check-in.
    my $line_count = 0;
    my @tmp = &extract_revision($rcsobj, $primordial);
    $line_count = @tmp;
    # Create initial revision map for primordial version.
    while ($line_count--) {
        push(@revision_map, RevEntry->new($rcsobj, $primordial));
    }

    # now, crawl from primordial map forward, jumping from merge to
    # merge, creating a "revision map" -- an array where each element
    # represents a line of text in the given revision but contains only
    # the revision number in which the line was introduced rather than
    # the line text itself.

    # build ancestor list all the way from $revision down to $primordial
    my @ancestors = &ancestor_revisions($rcsobj, $revision);

    my $last_rev = $primordial; my @last_map = @revision_map;
    # find last RENAME or SPLIT log commend, and fork off.
    my $n;
    for ($n=0; $n <= $#ancestors; $n++) { # foreach r in @ancestors...
        my $r = $ancestors[$n];
        next if $opt_R; # ignore RENAME tags
        next unless ($rcsobj->{REVISION_LOG}->{$r} =~
                     m/[@](?:RENAME|SPLIT):\s*([^@]+)\s*[@]/);
        # ok, this is a renaming.  extract from and to patterns
        my ($frm_pat, $to_pat) = split(/\s+/, $1);
        # ignore directive if @RENAME@ tag is not well formed.
        next unless defined $frm_pat && defined $to_pat;
        # ignore directive if $rcs_pathname doesn't contain $topat
        # (typically, this means that this is the file being removed,
        #  not the file being added)
        next unless ($rcs_pathname =~ m/$to_pat/);
        my $frm_pathname = $rcs_pathname;
        $frm_pathname =~ s/$to_pat/$frm_pat/;
        # open and parse this CVS file (skip directive on error)
        next if !open_cvs_file(\*RCSFILE, $frm_pathname);
        my $frmobj = &parse_rcs_file();
        $frmobj->{PATHNAME} = $frm_pathname;
        close(RCSFILE);
        # find which revision to use: we'll start at the revision
        # applicable at the time and branch this check-in occured,
        # then move backwards until we get to a non-dead check-in.
        #  a) first, find a symbolic name for the branch we're on.
        $r =~ m/(\d+(?:\.\d+\.\d+)*)\.\d+/;
        my $branchnum = $1;
        my $branchsym = $rcsobj->{REVISION_SYMBOLIC_NAME}->{$branchnum};
        if (!defined $branchsym) { # try a 'magic' branch number.
            $branchnum =~ s/(\.\d+)$/.0$1/;
            $branchsym = $rcsobj->{REVISION_SYMBOLIC_NAME}->{$branchnum};
        }
        #  b) use the HEAD branch if all else fails.
        $branchsym = "HEAD" if !defined $branchsym;
        #  c) map this branch tag to the appropriate revision in $frm
        my $frmtag = &map_tag_to_revision($frmobj, $branchsym,
                                          $rcsobj->{TIMESTAMP}->{$r});
        die "no such tag: $branchsym\n" if $frmtag eq '';
        #  d) now move back until we find a non-dead check-in
        while ($frmobj->{REVISION_STATE}->{$frmtag} eq "dead") {
            $frmtag = $frmobj->{PREV_REVISION}->{$frmtag};
        }
        next unless defined $frmtag; # give up, if appropriate
        # recurse to get rev_map of frm file.
        my @frm_map = &cvs_make_rev_map($frmobj, $frm_pathname, $frmtag);
        # now get a diff to bring the map up to present
        my @fr_lines = &extract_revision($frmobj, $frmtag);
        my @to_lines = &extract_revision($rcsobj, $r);
        my @diffs = &diff_lines(\@fr_lines, \@to_lines);
        # apply these diffs to @frm_map to account for mods during move.
        my $adjust = 0;
        # for each command...
        for (my $i = 0; $i <= $#diffs; $i++) {
            my $command = $diffs[$i];
            if ($command =~ /^d(\d+)\s(\d+)$/) { # Delete command
                my ($start_line, $count) = ($1, $2);
                splice(@frm_map, $start_line + $adjust - 1, $count);
                $adjust -= $count;
            } elsif ($command =~ /^a(\d+)\s(\d+)$/) { # Add command
                my ($start_line, $count) = ($1, $2);
                $skip = $count; $i+=$count;
                my @temp; $#temp = -1;
                while ($count--) {
                    push(@temp, RevEntry->new($rcsobj, $r));
                }
                splice(@frm_map, $start_line + $adjust, 0, @temp);
                $adjust += $skip;
            } else {
                die "Error parsing diff commands";
            }
        }
        die "Inconsistent state" unless $#frm_map == $#to_lines;
        # ok, done.
        $last_rev = $r; @last_map = @frm_map;
        last; # stop looking for RENAMEs.
    }
    # deal with MERGEs
    my %seen;
    for ($n--; $n >= 0; $n--) { # foreach r in reverse @ancestors...
        my $r = $ancestors[$n];
        $seen{$r}=1;
        if (!$opt_M &&
            $rcsobj->{REVISION_LOG}->{$r} =~ m/[@]MERGE:\s*([^@]+)\s*[@]/) {
            my $from_rev = &map_tag_to_revision($rcsobj, $1,
                                                $rcsobj->{TIMESTAMP}->{$r});
            next if $from_rev eq ''; # no such revision!
            # find common point.
            my $com=$from_rev;
            while (!$seen{$com} ) {
                $com = $rcsobj->{PREV_REVISION}->{$com};
            }
            # create map for common point.
            my @com_map = &simple_rev_map($rcsobj, $com,$last_rev,@last_map);
            # create maps for both sides of merge
            my @main_map = &simple_rev_map($rcsobj, $r,       $com,@com_map);
            my @merge_map= &simple_rev_map($rcsobj, $from_rev,$com,@com_map);
            # incrementally update @merge_map to revision $r.
            my @main_lines = &extract_revision($rcsobj, $r);
            my @merge_lines= &extract_revision($rcsobj, $from_rev);
            my @diffs = &diff_lines(\@merge_lines, \@main_lines);
            # apply these diffs to @merge_map to make it parallel to 
            # @main_map (stolen from &update_with_local_mods)
            my $adjust = 0;
            # for each command...
            for (my $i=0; $i <= $#diffs; $i++) {
                my $command = $diffs[$i];
                if ($command =~ /^d(\d+)\s(\d+)$/) { # Delete command
                    my ($start_line, $count) = ($1, $2);
                    splice(@merge_map, $start_line + $adjust - 1, $count);
                    $adjust -= $count;
                } elsif ($command =~ /^a(\d+)\s(\d+)$/) { # Add command
                    my ($start_line, $count) = ($1, $2);
                    $skip = $count; $i+=$count;
                    my @temp; $#temp = -1;
                    while ($count--) {
                        push(@temp, RevEntry->new($rcsobj, $r));
                    }
                    splice(@merge_map, $start_line + $adjust, 0, @temp);
                    $adjust += $skip;
                } else {
                    die "Error parsing diff commands";
                }
            }
            die "Inconsistent state" unless $#merge_map == $#main_map;
            # do the merge
            for (my $i=0; $i<$#main_map; $i++) {
                $main_map[$i] = $merge_map[$i]
                    if $main_map[$i]->rcsobj==$rcsobj &&
                        $main_map[$i]->revision eq $r;
            }
            # assign results
            @last_map = @main_map;
            $last_rev = $r;
        }
    }
    # bring from last merge up to present
    @revision_map = &simple_rev_map($rcsobj, $revision,$last_rev,@last_map);

    return @revision_map;
}

# map a tag to a numerical revision number. The tag can be a symbolic
# branch tag, a symbolic revision tag, or an ordinary numerical revision
# number.  Branch tags can be further qualified by a time stamp, which means
# 'find `the revision current on this branch at the given time'.
sub map_tag_to_revision {
    my ($rcsobj, $tag_or_rev, $timestamp) = @_;
    # get numerical revision # for (possibly symbolic) tag
    my $tagnum = $rcsobj->{TAG_REVISION}->{$tag_or_rev};
    # if it's a branch tag, we have to look up by date...
    # branch tags have an odd number of dot-separated integers; sometimes
    # they have a 'magic' zero in the second rightmost position.
    if ($tagnum =~ m/^((?:\d+\.\d+\.)*)(?:0\.)?(\d+)$/) {
        $tagnum = $1.$2; # remove 'magic' zero if it exists.
        my $last_rev = $rcsobj->{LAST_REVISION}->{$tagnum};
        # if timestamp given, then work backwards toward branch root.
        while (defined($timestamp) && defined($last_rev) &&
               $rcsobj->{TIMESTAMP}->{$last_rev} > $timestamp) {
            $last_rev = $rcsobj->{PREV_REVISION}->{$last_rev};
        }
        return $last_rev if defined $last_rev;
        # if no revisions on branch, return branch point.
        $tagnum =~ s/\.\d+$//; # strip 'branch' component.
    }
    return $tagnum;
}

sub parse_cvs_file {
    my ($pathname, $rcs_pathname, $checked_out_revision) = @_;
    my $revision;

    # Args in:  $opt_r - requested revision
    #           $opt_m - time since modified
    # Args out: @revision_map
    #           $revision
    #           %timestamp
    #           (%revision_deltatext)

    @revision_map = ();
    #CheckHidden($rcs_pathname); # CSA: mozilla used this to hide stuff.

    die "$progname: error: This file appeared to be under CVS control, " . 
        "but the RCS file is inaccessible.\n(Couldn't open '$rcs_pathname')\n"
            if !open_cvs_file (\*RCSFILE, $rcs_pathname);
    my $rcsobj = &parse_rcs_file();
    $rcsobj->{PATHNAME} = $rcs_pathname;
    close(RCSFILE);

    if (!defined($opt_r)) {
        # Use the checked-out revision.
        $revision = $checked_out_revision;
    } elsif ($opt_r eq '' || $opt_r eq 'HEAD') {
        # Explicitly specified topmost revision in tree
        $revision = $rcsobj->{HEAD_REVISION};
    } else {
        # Symbolic tag or specific revision number specified.
        $revision = &map_tag_to_revision($rcsobj, $opt_r);
        die "$progname: error: -r: No such revision: $opt_r\n"
            if ($revision eq '');
    }

    @revision_map = &cvs_make_rev_map($rcsobj, $rcs_pathname, $revision);

    ($rcsobj, $revision);
}

# Read CVS/Entries, CVS/Repository, and CVS/Root files.
#
# Creates these associative arrays, keyed by the CVS file pathname
#
#  %cvs_revision         -- Revision # present in working directory
#  %cvs_date
#  %cvs_sticky_revision  -- Sticky tag, if any
#  
#  Also, creates %cvs_files, keyed by the directory path, which contains
#  a backslash-separated list of the files under CVS control in the directory
#
#  Finally, creates %repository which contains a path to the repository
#  directory, keyed by directory path.  The remote repository access
#  mode (if any) is read from CVS/Root and prepended to the entry in
#  %repository.
sub read_cvs_entries
{
    my ($directory) = @_;
    my ($filename, $rev, $date, $idunno, $sticky, $pathname);

    $cvsdir = $directory . '/CVS';

    #CheckHidden($cvsdir); # CSA: mozilla used this to hide stuff

    return if (! -d $cvsdir);

    return if !open(ENTRIES, "< $cvsdir/Entries");
    
    while(<ENTRIES>) {
        chomp;
        # ENTRIES STARTING WITH 'D' ARE DIRECTORIES, NOT FILES.
        next unless ($_ =~ m:^/:);
        ($filename, $rev, $date, $idunno, $sticky) = split("/", substr($_, 1));
        ($pathname) = $directory . "/" . $filename;
        $cvs_revision{$pathname} = $rev;
        $cvs_date{$pathname} = $date;
        $cvs_sticky_revision{$pathname} = $sticky;
        $cvs_files{$directory} .= "$filename\\";
    }
    close(ENTRIES);

    return if !open(REPOSITORY, "< $cvsdir/Repository");
    $repository = <REPOSITORY>;
    chomp($repository);
    close(REPOSITORY);
    $repository{$directory} = $repository;

    # CSA: deal with remote repository and partial Repository paths.
    return if !open(CVSROOT, "< $cvsdir/Root");
    $cvsroot = <CVSROOT>;
    chomp($cvsroot);
    close(CVSROOT);
    if ($repository =~ m|^/|) { # repository path is absolute
        if ($cvsroot =~ m/^(:[^:]*:[^:]*:)/) { # remote repository?
            # prepend just the access method to the repository.
            $repository{$directory} = $1 . $repository;
        }
    } else { # if the repository path is relative, prepend the CVSROOT.
        $repository{$directory} = "$cvsroot/$repository";
    }
}

# Given path to file in CVS working directory, compute path to RCS
# repository file.  Cache that info for future use.
# CSA: also return checked-out revision, while we're at it.

sub rcs_pathname_and_revision {
    ($pathname) = @_;

    if ($pathname =~ m@/@) {
        ($directory,$filename) = $pathname =~ m@(.*)/([^/]+)$@;
    } else {
        ($directory,$filename) = ('.',$pathname);
        $pathname = "./" . $pathname;
    }
    if (!defined($repository{$directory})) {
        &read_cvs_entries($directory);
    }
       
    my $checked_out_revision = $cvs_revision{$pathname};
    if (!defined($checked_out_revision)) {
        die "$progname: error: File '$pathname' does not appear to be under" .
            " CVS control.\n"
    }

    print STDERR "file: $filename\n" if $debug;
    my ($rcs_path) = $repository{$directory} . '/' . $filename . ',v';
    return ($rcs_path,
            $checked_out_revision);
}

# make a temporary file containing the contents of an array.
# return the name of the file.
sub temp_file_with_contents {
    my @lines = @_;
    my $tmp1 = `mktemp /tmp/cvsblame.XXXXXX`; chomp $tmp1;
    open(TMP1, "> $tmp1") or die "Couldn't open temporary file $tmp1";
    foreach my $line (@lines) {
        print TMP1 $line;
    }
    close(TMP1);
    return $tmp1;
}
# get a minimal diff of the contents of two arrays.
sub diff_lines {
    my ($ref1, $ref2) = @_;
    my $tmp1 = &temp_file_with_contents(@{$ref1});
    my $tmp2 = &temp_file_with_contents(@{$ref2});
    open(DIFF, "diff -n $tmp1 $tmp2 |") or die "Couldn't start diff";
    my @results = <DIFF>;
    close(DIFF);
    unlink $tmp1, $tmp2 or die "Couldn't remove temporary files.";
    return @results;
}

sub update_with_local_mods {
    my ($rcsobj, $pathname, @text) = @_;
    my @diffs;
    return @text if !open(CVSDIFF, "cvs -flnq diff -n $pathname |");
    @diffs = <CVSDIFF>;
    close(CVSDIFF);
    # stolen from parse_cvs_file and tweaked for my nefarious purposes [CSA].
    my $adjust = 0;  my $i = 0;
    $i++ until $i > $#diffs || $diffs[$i] =~ /^diff\s/; # skip header info.
    for ($i++; $i <= $#diffs; $i++) {
        my $command = $diffs[$i];
        if ($command =~ /^d(\d+)\s(\d+)$/) { # Delete command
            my ($start_line, $count) = ($1, $2);
            splice(@revision_map, $start_line + $adjust - 1, $count);
            splice(@text, $start_line + $adjust - 1, $count);
            $adjust -= $count;
        } elsif ($command =~ /^a(\d+)\s(\d+)$/) { # Add command
            my ($start_line, $count) = ($1, $2);
            $skip = $count;
            my @temp1; my @temp2; $#temp1 = -1; $#temp2 = -1;
            while ($count--) {
                push(@temp1, RevEntry->new($rcsobj, "LOCAL"));
                push(@temp2, $diffs[++$i]);
            }
            splice(@revision_map, $start_line + $adjust, 0, @temp1);
            splice(@text,         $start_line + $adjust, 0, @temp2);
            $adjust += $skip;
        } else {
            die "Error parsing diff commands";
        }
    }
    # annotate 'author' of this revision...
    my $whoami = `whoami`; chomp($whoami);
    $rcsobj->{REVISION_AUTHOR}->{"LOCAL"} = $whoami;
    # ok, done.
    return @text;
}

sub show_annotated_cvs_file {
    my ($pathname) = @_;
    my (@output) = ();

    my ($rcs_pathname,$checked_out_rev)=&rcs_pathname_and_revision($pathname);
    my ($rcsobj, $revision) = &parse_cvs_file($pathname,$rcs_pathname,$checked_out_rev);

    @text = &extract_revision($rcsobj, $revision);
    die "$progname: Internal consistency error" if ($#text != $#revision_map);

    # update with local modifications unless a) a (presumably non-local)
    # revision was explicitly specified on the command-line, or b) the user
    # asked us not to (with the -n option)
    @text = &update_with_local_mods($rcsobj, $pathname, @text)
        unless (defined($opt_r) || $opt_n);

    # Set total width of line annotation.
    # Warning: field widths here must match format strings below.
    $annotation_width = 0;
    $annotation_width +=  8 if $opt_a; # author
    $annotation_width +=  7 if $opt_v; # revision
    $annotation_width +=  6 if $opt_A; # age
    $annotation_width += 12 if $opt_d; # date
    $blank_annotation = ' ' x $annotation_width;

    if ($multiple_files_on_command_line) {
        print "\n", "=" x (83 + $annotation_width);
        print "\n$progname: Listing file: $pathname\n"
    }

    # Print each line of the revision, preceded by its annotation.
    $line = 0;
    my $suffix_cnt = 0; my %suffixes; $suffixes{$rcsobj}="";
    foreach $reventry (@revision_map) {
        $text = $text[$line++];
        $annotation = '';

        # Annotate with revision author
        $annotation .= sprintf("%-8s", $reventry->author) if $opt_a;

        # Annotate with revision number
        my $r = $reventry->revision;
        $suffixes{$reventry->rcsobj} = chr(ord('a')+$suffix_cnt++)
            if !exists $suffixes{$reventry->rcsobj};
        $r .= $suffixes{$reventry->rcsobj};
        $annotation .= sprintf(" %-6s", $r) if $opt_v;

        # Date annotation
        $annotation .= " ".$reventry->ctime if $opt_d;

        # Age annotation ?
        $annotation .= sprintf(" (%3s)", int($reventry->age)) if $opt_A;

        # URL annotation.
        my $partialpath = substr($rcs_truename{$reventry->pathname},
                                 length($cvsroot));
        $partialpath =~ s/,v$//;
        my $rev2 = $reventry->revision;
        my $rev1 = $reventry->rcsobj->{PREV_REVISION}->{$rev2};
        $rev1 = $rev2 unless defined $rev1;
        $rev1 = $rev2 = $checked_out_rev if $rev2 eq "LOCAL";
        $annotation .= " $opt_u$partialpath.diff?".
            "r1=$rev1&r2=$rev2" if defined $opt_u;

        # -m (if-modified-since) annotion ?
        if ($opt_m && ($reventry->timestamp < $opt_m_timestamp)) {
            $annotation = $blank_annotation;
        }

        # Suppress annotation of whitespace lines, if requested;
        $annotation = $blank_annotation if $opt_w && ($text =~ /^\s*$/);

       printf "%4d ", $line if $opt_l;
       print "$annotation ";
       print $opt_q ? "\n" : $text;
#        push(@output, sprintf("%4d ", $line)) if $opt_l;
#        push(@output, "$annotation - $text");
    }
#    @output;
}

sub usage {
    die
"$progname: usage: [options] [file|dir]...\n",
"   Options:\n",
"      -r <revision>      Specify CVS revision of file to display\n",
"                         <revision> can be any of:\n",
"                           + numeric tag, e.g. 1.23,\n",
"                           + symbolic branch or revision tag, e.g. CHEDDAR,\n",
"                           + HEAD keyword (most recent revision on trunk)\n",
"      -a                 Annotate lines with author (username)\n",
"      -A                 Annotate lines with age, in days\n",
"      -v                 Annotate lines with revision number\n", 
"      -d                 Annotate lines with date, in local time zone\n",
"      -l                 Annotate lines with line number\n",
"      -w                 Don't annotate all-whitespace lines\n",
"      -m <# days>        Only annotate lines modified within last <# days>\n",
"      -q                 Suppress original text (just print annotation)\n",
"      -n                 Don't show local modifications\n",
"      -M                 Don't follow \@MERGE@ tags in logs\n",
"      -R                 Don't follow \@RENAME@ tags in logs\n",
"      -T                 Don't follow \@REVERT@ tags in logs\n",
"      -u <base url>      Output a URL on each line for markup programs\n",
"      -h                 Print help (this message)\n\n",
"   (-a -v assumed, if none of -a, -v, -A, -d supplied)\n"
;
}

# suppress -w warnings.
undef $opt_M; undef $opt_h; undef $opt_l; undef $opt_n;
undef $opt_q; undef $opt_w; undef $opt_R; undef $opt_T;
&usage if (!&getopts('r:m:Aadhlvwqnu:MRT'));
&usage if ($opt_h);             # help option

$multiple_files_on_command_line = 1 if ($#ARGV != 0);

&cvsblame_init;

sub annotate_cvs_directory
{
    my ($dir) = @_;
    &read_cvs_entries($dir);
    foreach $file (split(/\\/, $cvs_files{$dir})) {
        &show_annotated_cvs_file("$dir/$file");
    }
}
 
# No files on command-line ?  Use current directory.
push(@ARGV, '.') if ($#ARGV == -1);
 
# Iterate over files/directories on command-line
while ($#ARGV >= 0) {
    $pathname = shift @ARGV;
    # Is it a directory ?
    if (-d $pathname) {
        $multiple_files_on_command_line = 1;
        &traverse_cvs_tree($pathname, \&annotate_cvs_directory);

    # No, it must be a file.
    } else {
        &show_annotated_cvs_file($pathname);
    }
}
