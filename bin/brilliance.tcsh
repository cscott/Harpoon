#This particular bit of brilliance is due to Darko.
# if you're running tcsh, type 'source bin/brilliance.tcsh' and make
# your life easier.  Re-source this file as needed; ie, after you add
# new classes.  

# You may want to alias 'makejava' to 'make java; source bin/brilliance.tcsh'
# and 'makejikes' to 'make jikes; source bin/brilliance.tcsh'

set lib_classes=`unzip -l /usr/local/jdk/lib/classes.zip | cut -b28- | grep .class | sed -e 's/.class//g' -e 's|/|.|g'`
set collection_classes=`unzip -l /usr/local/collections/collections.jar | cut -b28- | grep .class | sed -e 's/.class//g' -e 's|/|.|g'`
set harpoon_classes=`find ~/Harpoon/Code/harpoon/ -type f -name "*.class" | sed -e 's|^.*Harpoon/Code/||g' -e 's|.class$||' -e 's|/|.|g'`
set all_classes = ($harpoon_classes $lib_classes $collection_classes)
complete java 'p/*/$all_classes/'
complete javap 'p/*/$all_classes/'
complete jdb 'p/*/$all_classes/'
