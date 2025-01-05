# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Install Node.js 18 and Git
RUN apt-get update && \
    apt-get install -y curl git && \
    curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean
    

# Set the working directory in the container & take PORT as an argument from the build command
WORKDIR /app
ARG PORT
ENV PORT=${PORT}

# Copy the .jar file to the container
COPY ./backend/rsc-job-service/util/RepoSense.jar /app/RepoSense.jar

# Expose the port your application runs on (if applicable)
EXPOSE $PORT

# Run the .jar file
ENTRYPOINT ["java", "-jar", "RepoSense.jar"]docker run -p 8080:8080 -v "C:/Users/Gwee/Documents/Capstone 4902/output:/app/output" reposense-jar --repos https://github.com/yeebinrong/RepoSenseCloud.git --view --output /app/output