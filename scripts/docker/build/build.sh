#!/bin/bash

if [ $# -le 0 ]; then
  echo "please enter version parameter. eg: v1.0.0-arm64 、v1.0.0-amd64"
  exit 1
fi

CUR_DIR=$(pwd)
cd ../../../
DMS_HOME=$(pwd)
##build project
mvn clean package -DskipTests
## build backend docker image
mkdir -p $CUR_DIR/tmp/backend
cp $DMS_HOME/dms-api/target/dms-api.jar $CUR_DIR/tmp/backend
cd $CUR_DIR
docker build -f ./backend/Dockerfile -t dms-backend:$1 .
## build frontend docker image
mkdir -p $CUR_DIR/tmp/frontend
cp -r $DMS_HOME/dms-ui/dist/* $CUR_DIR/tmp/frontend
cd $CUR_DIR
docker build -f ./frontend/Dockerfile -t dms-frontend:$1 .

rm -rf $CUR_DIR/tmp