#! /usr/bin/env python

f = open("../processing-rate-stats.txt", "r")
g = open("../receiving-rate-stats.txt", "r")
h = open("../tracking-rate-stats.txt", "r")
out = open("../total-rate-stats.txt", "w")

fnums = map(int, f.readlines())
gnums = map(int, g.readlines())
hnums = map(int, h.readlines())

for i in range(0, min(len(fnums) - 1, len(gnums) - 1, len(hnums) - 1)):
    out.write(str(fnums[i] + gnums[i] + hnums[i]))
    out.write("\n")
out.close()

f = open("../processing-latency-stats.txt", "r")
g = open("../receiving-latency-stats.txt", "r")
h = open("../tracking-latency-stats.txt", "r")
out = open("../total-latency-stats.txt", "w")

fnums = map(int, f.readlines())
gnums = map(int, g.readlines())
hnums = map(int, h.readlines())

for i in range(0, min(len(fnums) - 1, len(gnums) - 1, len(hnums) - 1)):
    out.write(str(fnums[i] + gnums[i] + hnums[i]))
    out.write("\n")
out.close()
