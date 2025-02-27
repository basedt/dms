FROM maven:3.9.6-amazoncorretto-8 as build
ENV DMS_HOME=/opt/app
COPY ./ $DMS_HOME/dms
RUN cd $DMS_HOME/dms && mvn clean package -B -U -T 4 '-Dmaven.test.skip=true'

FROM openjdk:8u342 as docker
ENV DMS_HOME=/opt/app
WORKDIR $DMS_HOME/dms
COPY --from=build $DMS_HOME/dms/dms-api/target/dms-api.jar .
#COPY ../tmp/backend/dms-api.jar .
COPY ./scripts/docker/build/dms-backend/application-docker.yml .
EXPOSE 8080

ENTRYPOINT ["sh","-c","java -jar -Dspring.profiles.active=docker $DMS_HOME/dms/dms-api.jar --spring.config.location=$DMS_HOME/dms/application-docker.yml"]