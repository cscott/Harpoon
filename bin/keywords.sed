#!/bin/sed -n
# extracts keywords within <code>...</code> tags from javadoc.
/<[Cc][Oo][Dd][Ee]>/{
s~</[Cc][Oo][Dd][Ee]>\([^<]\|<[^Cc]\)*<[Cc][Oo][Dd][Ee]>~\
~g
s~^\([^<]\|<[^Cc]\)*<[Cc][Oo][Dd][Ee]>~~
s~</[Cc][Oo][Dd][Ee]>\([^<]\|<[^Cc]\)*$~~
p
}
