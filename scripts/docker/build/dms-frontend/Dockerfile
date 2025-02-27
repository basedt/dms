FROM maven:3.9.6-amazoncorretto-8 as build
ENV DMS_HOME=/opt/app
COPY ./ $DMS_HOME/dms
RUN cd $DMS_HOME/dms && mvn clean package -B -U -T 4 '-Dmaven.test.skip=true' -pl dms-ui

FROM nginx:stable as docker
ENV DMS_HOME=/opt/app
WORKDIR $DMS_HOME/dms
#COPY ../tmp/frontend ./
COPY --from=build $DMS_HOME/dms/dms-ui/dist/* ./
COPY ./scripts/docker/build/dms-frontend/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80

CMD ["nginx","-g","daemon off;"]