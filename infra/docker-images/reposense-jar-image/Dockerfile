# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Install Node.js 18 and Git
RUN apt-get update && \
    apt-get install -y curl git && \
    curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean
    

# Set the working directory in the container 
WORKDIR /app

# # Take PORT as an argument from the build command
# ARG PORT
# ENV PORT=${PORT}

# Copy the .jar file to the container
COPY ./infra/docker-images/reposense-jar-image/RepoSense.jar /app/RepoSense.jar

# Copy entrypoint script file to container
COPY ./infra/docker-images/reposense-jar-image/reposense-docker-entrypoint.sh /app/reposense-docker-entrypoint.sh

# Make the entrypoint script executable
RUN chmod +x /app/reposense-docker-entrypoint.sh

# Run the entrypoint script
ENTRYPOINT ["/app/reposense-docker-entrypoint.sh"]