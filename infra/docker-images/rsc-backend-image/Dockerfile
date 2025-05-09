# Use the official Amazon Corretto 21 base image with the specific version
FROM amazoncorretto:21.0.4-al2

# Set the working directory
WORKDIR /app

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
# Add JAVA_HOME to PATH
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify the installation
RUN java -version

# Install dos2unix using yum
RUN yum update -y && \
    yum install -y dos2unix unzip curl && \
    yum clean all

# Install AWS CLI v2
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf aws awscliv2.zip

# Verify AWS CLI version
RUN aws --version

# Expose ports for microservices
EXPOSE 3001
EXPOSE 3002
EXPOSE 3003

RUN ls -l

# Copy entrypoint script
COPY ./infra/docker-images/rsc-backend-image/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
RUN dos2unix /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]