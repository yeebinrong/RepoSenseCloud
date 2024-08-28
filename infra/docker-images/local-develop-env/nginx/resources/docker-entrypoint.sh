#!/bin/sh

# Substitute environment variables in nginx.conf
envsubst '$NGINX_LOCAL_DEVELOPMENT_API_URL' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Start Nginx
nginx -g 'daemon off;'