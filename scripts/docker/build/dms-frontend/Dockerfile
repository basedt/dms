FROM nginx:stable as docker
ENV DMS_HOME=/opt/app
WORKDIR $DMS_HOME/dms
COPY ./dms-ui/dist ./
COPY ./scripts/docker/build/dms-frontend/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80

CMD ["nginx","-g","daemon off;"]