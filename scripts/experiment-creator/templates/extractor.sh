#!/bin/bash

# This scripts wraps the experiment results up, 
# as there are many files created we do not need.
# Results are packaged into a tar.gz
# Initial output is kept

# This script is meant to run next to the compose-output
# After all experiments finished 

echo "Starting Result-Extraction"

# Safety Copy
cp -r compose-output result-package

# Delete all non-json files
find result-package -name "*.java" -delete
find result-package -name "*.c2v" -delete
find result-package -name "*.num_examples" -delete

# Cleanup all now-empty folders
find result-package -empty -type d -delete

# Move the logs in
# Only works if they are called "experiments.log"
[ -f "experiments.log" ] && cp experiments.log result-package/experiments.log

# Wrap things up by zipping
tar -czvf result-package.tar.gz result-package

echo "Finished Result-Extraction"