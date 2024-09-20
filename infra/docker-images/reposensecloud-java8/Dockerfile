# Use the official Amazon Corretto 8 base image with the specific version
FROM amazoncorretto:8u322-al2

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto

# Add JAVA_HOME to PATH
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify the installation
RUN java -version

# Set the default command to run when the container starts
CMD ["bash"]