filename=$1
echo $filename
for s in \
  $(objdump -h $filename | awk '{ print $2 }' | sort -u | grep "^\.") ; do
  echo -n "$s: "
  (echo "ibase=16" ; \
   objdump -h $filename | fgrep $s | tr '[a-z]' '[A-Z]' |\
   awk '{ print "a+=",$3 }' ; \
   echo "a") | bc
done
