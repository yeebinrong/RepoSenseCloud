FROM maven:3-amazoncorretto-21-debian

WORKDIR /app
ARG PORT
ENV PORT=${PORT}
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Copy the docker-entrypoint.sh file into the Docker image
COPY ./infra/docker-images/rsc-local-backend-image/docker-entrypoint.sh /app/docker-entrypoint.sh

# Install build tools
RUN apt-get update && apt-get install -y fswatch dos2unix

# Convert docker-entrypoint.sh to Unix format
RUN dos2unix /app/docker-entrypoint.sh
RUN chmod 755 /app/docker-entrypoint.sh
RUN ls -l /app/docker-entrypoint.sh

EXPOSE $PORT

ENTRYPOINT bash /app/docker-entrypoint.sh $SERVICE_NAME