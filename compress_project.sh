#!/bin/bash
PROJECT_PATH=$1
NEW_PROJECT_PATH=$2

tar -cjf $NEW_PROJECT_PATH -C $PROJECT_PATH .
rm -rf $PROJECT_PATH