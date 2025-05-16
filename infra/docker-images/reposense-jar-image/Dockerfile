# Use an official OpenJDK runtime as a parent image
# Use Java 11 as RepoSense is only compatible up to Java 11
FROM openjdk:11-jre-slim

# Install Node.js 18 and Git & awscli
# Use Node 18 as RepoSense supports up to Node.js 18
RUN apt-get update && \
    apt-get install -y curl unzip git && \
    curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    # Install AWS CLI v2 for amd64 architecture
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws && \
    apt-get clean && \
    # Install dos2unix to convert line endings
    apt-get install -y dos2unix


# Set the working directory in the container 
WORKDIR /app

# # Take PORT as an argument from the build command
# ARG PORT
# ENV PORT=${PORT}

# Copy entrypoint script file to container
COPY ./infra/docker-images/reposense-jar-image/reposense-docker-entrypoint.sh /app/reposense-docker-entrypoint.sh
# Make the entrypoint script executable
RUN chmod +x /app/reposense-docker-entrypoint.sh
RUN dos2unix /app/reposense-docker-entrypoint.sh

# Download the RepoSense v3.0 jar
RUN curl -L -o /app/RepoSense.jar https://github.com/reposense/RepoSense/releases/download/v3.0/RepoSense.jar

# Run the entrypoint script
ENTRYPOINT ["/app/reposense-docker-entrypoint.sh"]