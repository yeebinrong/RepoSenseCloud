# Use the official Amazon Corretto 21 base image with the specific version
FROM amazoncorretto:21.0.4-al2

# Set the working directory
WORKDIR /app/$JAR_FILE_NAME

ARG PORT
ARG JAR_FILE_NAME
ENV PORT=${PORT}
ENV JAR_FILE_NAME=${JAR_FILE_NAME}
ENV FOLDER_NAME=${FOLDER_NAME}

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto

# Add JAVA_HOME to PATH
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify the installation
RUN java -version && mvn -v

EXPOSE $PORT


# Set the default command to run when the container starts
# CMD java -jar target/$JAR_FILE_NAME