# Use the official Nginx image as the base image
FROM nginx:1.27.0

# Copy resources
COPY resources/nginx.conf.template /etc/nginx/nginx.conf.template
COPY resources/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x entrypoint.sh

# Expose port 80
EXPOSE 80

# Start Nginx
ENTRYPOINT ["/docker-entrypoint.sh"]