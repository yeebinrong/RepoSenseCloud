FROM maven:3-amazoncorretto-21-alpine

WORKDIR /app
ARG PORT
ENV PORT=${PORT}
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Copy the docker-entrypoint.sh file into the Docker image
COPY ./infra/docker-images/rsc-local-backend-image/docker-entrypoint.sh /app/docker-entrypoint.sh

# Install dependencies for building fswatch
RUN apk update && apk add --no-cache \
    build-base \
    cmake \
    inotify-tools-dev \
    git \
    wget \
    dos2unix

# Clone fswatch from GitHub and build it
RUN git clone https://github.com/emcrisostomo/fswatch.git /tmp/fswatch && \
    cd /tmp/fswatch && \
    cmake . && \
    make && \
    make install && \
    rm -rf /tmp/fswatch

# Convert docker-entrypoint.sh to Unix format
RUN dos2unix /app/docker-entrypoint.sh
RUN chmod 755 /app/docker-entrypoint.sh
RUN ls -l /app/docker-entrypoint.sh

EXPOSE $PORT

ENTRYPOINT bash /app/docker-entrypoint.sh $SERVICE_NAME