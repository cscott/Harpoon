#This particular bit of brilliance is due to Darko.
# if you're running tcsh, type 'source bin/brilliance.tcsh' and make
# your life easier.  Re-source this file as needed; ie, after you add
# new classes.  

# You may want to alias 'makejava' to 'make java; source bin/brilliance.tcsh'
# and 'makejikes' to 'make jikes; source bin/brilliance.tcsh'

set harpoon_classes=`find ~/Harpoon/Code/harpoon/ -type f -name "*.class" | sed -e 's|^.*Harpoon/Code/||g' -e 's|.class$||' -e 's|/|.|g'`
complete java 'p/*/$harpoon_classes/'
