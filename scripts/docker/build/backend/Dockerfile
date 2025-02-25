FROM openjdk:8u342

ENV DMS_HOME=/opt/app

WORKDIR $DMS_HOME/dms
COPY ../tmp/backend/dms-api.jar .
COPY ../backend/application-docker.yml .
EXPOSE 8080

ENTRYPOINT ["sh","-c","java -jar -Dspring.profiles.active=docker $DMS_HOME/dms/dms-api.jar --spring.config.location=$DMS_HOME/dms/application-docker.yml"]