# Use a base image
FROM node:18.18.2-alpine

# Set the working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy the rest of the application code
COPY . .

# Expose the port your application is running on
EXPOSE 3000

# Start the application
CMD ["npm", "start"]