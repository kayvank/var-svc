#!/bin/bash
##########################################
## run the docker image generated from packaging this project
## must provide image name
## dependency: requires ./.envrc pointing to a suitable env
##########################################

if [[ $# -lt 1 ]]; then 
  echo "usage is $0 dockeriamge" 
  exit -1
fi
##for i in `docker ps -a | grep 'qos' | awk '{print $1}'`;do docker rm -f $i;done
##for i in `docker images | grep 'qos' | awk '{print $3}'`;do docker rmi -f  $i;done

 docker run  \
   -p 9000:9000 \
   -e TOP_TRENDING_URL=${TOP_TRENDING_URL} \
   -e APIV3_URL=${APIV3_URL} \
   -e LIFTIGNITER_URL=${LIFTIGNITER_URL} \
   -e LIFTIGNITER_APIKEY=${LIFTIGNITER_APIKEY} \
   -e PACHINKO_URL=${PACHINKO_URL} \
   -e AUTH_KEY=${AUTH_KEY} \
   -e AUTH_DERIVED_KEY=${AUTH_DERIVED_KEY} \
   -e AUTH_VALIDATION_KEY=${AUTH_VALIDATION_KEY} \
   -e DD_API_KEY=${DD_API_KEY} \
   -e JDBC_URL=${JDBC_URL} \
   -e JDBC_USER=${PRD_JDBC_USER} \
   -e JDBC_PASSWORD=${JDBC_PASSWORD} \
   -e STATSD_TAG_ENV="local,qos-r-02" \
  $1
