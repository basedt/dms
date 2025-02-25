FROM nginx:stable
ENV DMS_HOME=/opt/app
WORKDIR $DMS_HOME/dms
COPY ../tmp/frontend ./
COPY ../frontend/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80

CMD ["nginx","-g","daemon off;"]