#!/bin/awk -f
# unmunge.  Expects to find munged files on standard input.
# Pipeline safe.  Don't write output file until everything's read.
function mkdoctmp() {
    "mktemp /var/tmp/unmunge.XXXXXX" | getline doctmp;
    if (length(doctmp)==0) {
      print "unmunge: Can't create temp file, aborting." > "/dev/stderr" ;
      exit 1;
    };
}
function mvdoctmp() {
  if (length(doctmp)!=0) {
    fflush(doctmp); close(doctmp);
    system("mv -f " doctmp " " outf);
  }
}    

/^---- CUT HERE ----/{
    mvdoctmp();
    mkdoctmp();
    getline outf; 
    print outf; 
    # make sure that the right directory exists.
    "dirname " outf | getline outd;
    system("mkdir -p " outd);
    # this line does not go into the output file.
    next;
}
# dump rest of the lines to the right file.
{ gsub(/^- /,"-"); print >> doctmp; } 

END { mvdoctmp() }
