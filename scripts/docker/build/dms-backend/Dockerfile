FROM amazoncorretto:17.0.14 as docker
ENV DMS_HOME=/opt/app
WORKDIR $DMS_HOME/dms
COPY ./dms-api/target/dms-api.jar .
COPY ./scripts/docker/build/dms-backend/application-docker.yml .
EXPOSE 8080

ENTRYPOINT ["sh","-c","java --add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED -Dspring.profiles.active=docker -jar $DMS_HOME/dms/dms-api.jar --spring.config.location=$DMS_HOME/dms/application-docker.yml "]