FROM registry.datapwn.com/talend/miningnix:1.8

RUN apk --update add bash

# copy the script that will init host:port of api rest service
COPY init_docker.sh /init_docker.sh

# override the nginx default conf
ADD nginx.conf /etc/nginx/conf/nginx.conf

# copy source of web-app to web server
COPY build /etc/nginx/html

# this script will init host:port of api service AND start the apache service
CMD ["/init_docker.sh","&","nginx", "-g", "daemon off;"]

