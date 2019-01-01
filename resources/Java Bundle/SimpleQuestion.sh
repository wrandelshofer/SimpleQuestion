#!/bin/tcsh 


# get the script directory
# ------------------------
if ("$0" != "tcsh") then
  set scriptdir=`dirname "$0"` # the script was run from this location
else
  set scriptdir=`dirname "$_[2]"` # the script was run from this location
endif

cd "$scriptdir"

# Start the program
# -----------------
java -jar SimpleQuestion.jar