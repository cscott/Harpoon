#!/bin/sh
sources=`find . -name "*.java" -print | sort -u`
words=`sed -nf bin/keywords.sed $sources | sort -u`
classes=`ls doc/*.html | \
	egrep -v "^doc/(AllNames|index|packages|tree).html" | \
	egrep -v "^doc/(images|Package-|API_users_guide.html)" | \
	egrep -v "^doc/(TIMESTAMP|ChangeLog)" | \
	sed -e 's|^doc/||' -e 's/.html$//' | sort -u`
sedc="sed"

for f in $classes; do 
    if echo $words | fgrep -qw "$f" ; then
      sedc=$sedc" -e 's,<[Cc][Oo][Dd][Ee]>"$f"</[Cc][Oo][Dd][Ee]>,"
      sedc=$sedc'<code><a href="'$f'.html">'$f'</a></code>,g'
      sedc=$sedc"'"
    fi
    trail=`echo $f | sed -e 's/[A-Za-z]\+\.//g'`
    if echo $words | fgrep -qw "$trail" ; then
      sedc=$sedc" -e 's,<[Cc][Oo][Dd][Ee]>"$trail"</[Cc][Oo][Dd][Ee]>,"
      sedc=$sedc'<code><a href="'$f'.html">'$trail'</a></code>,g'
      sedc=$sedc"'"
    fi
done

eval "bin/munge doc | $sedc | bin/unmunge"
