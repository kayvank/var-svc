!#/bin/ksh

##
## dump the json artist data
##

mysql \
  -h${DB_URL} \
  -p${DB_PASSWORD} \
  -u${DB_USER} \
 --batch -q -e "select record from video_data" \
 ${DB_NAME} | sed 's/\\\\//g'
