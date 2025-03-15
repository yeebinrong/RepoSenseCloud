# Use Amazon Corretto 21 as the base image
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21

# Set the working directory inside the container
WORKDIR /app

# Copy your Java JAR file into the container
COPY target/rsc-batch-jobs-1.0-SNAPSHOT.jar /app/batch-job.jar

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/batch-job.jar"]
