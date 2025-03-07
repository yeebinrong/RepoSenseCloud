FROM maven:3-amazoncorretto-21-alpine

WORKDIR /app
ARG PORT
ARG SERVICE_NAME
ENV PORT=${PORT}
ENV SERVICE_NAME=${SERVICE_NAME}

# Copy the docker-entrypoint.sh file into the Docker image
COPY ./infra/docker-images/rsc-local-backend-image/docker-entrypoint.sh /app/docker-entrypoint.sh

RUN apk add inotify-tools
RUN chmod 755 /app/docker-entrypoint.sh

# Install required packages and fix line endings and permissions
RUN apk add --no-cache inotify-tools dos2unix \
    && dos2unix /app/docker-entrypoint.sh \
    && chmod +x /app/docker-entrypoint.sh \
    && ls -la /app/docker-entrypoint.sh

EXPOSE $PORT

ENTRYPOINT bash /app/docker-entrypoint.sh ${SERVICE_NAME}