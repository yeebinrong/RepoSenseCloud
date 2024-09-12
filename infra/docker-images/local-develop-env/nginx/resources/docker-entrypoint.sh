#!/bin/sh

# Substitute environment variables in nginx.conf
envsubst '$nginx_local_dev_api_url' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# Start Nginx
nginx -g 'daemon off;'