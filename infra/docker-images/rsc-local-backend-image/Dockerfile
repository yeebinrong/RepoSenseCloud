FROM maven:3-amazoncorretto-21-alpine

WORKDIR /app
ARG PORT
ENV PORT=${PORT}

# Copy the docker-entrypoint.sh file into the Docker image
COPY ./infra/docker-images/rsc-local-backend-image/docker-entrypoint.sh /app/docker-entrypoint.sh

RUN apk add inotify-tools
RUN chmod 755 /app/docker-entrypoint.sh

RUN ls -l /app/docker-entrypoint.sh

EXPOSE $PORT

ENTRYPOINT ["/app/docker-entrypoint.sh"]