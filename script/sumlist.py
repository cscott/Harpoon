#! /usr/bin/env python

f = open("../processing-stats.txt", "r")
g = open("../receiving-stats.txt", "r")
h = open("../tracking-stats.txt", "r")
out = open("../total-stats.txt", "w")

fnums = map(int, f.readlines())
gnums = map(int, g.readlines())
hnums = map(int, h.readlines())

for i in range(0, min(len(fnums) - 1, len(gnums) - 1, len(hnums) - 1)):
    out.write(str(fnums[i] + gnums[i] + hnums[i]))
    out.write("\n")
