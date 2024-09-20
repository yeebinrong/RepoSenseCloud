# docker-images is to manage the docker images used in the development / deployment of RepoSenseCloud

Build the docker images manually using following command
```
docker build -t {IMAGE_NAME}:{TAG} -f Dockerfile .
```
Run the docker image using following command
```
docker run -it -{IMAGE_NAME}:{TAG} bash
```

## rsc-cicd
This docker image is used for CICD

## rsc-frontend
This docker image is used for local development the frontend

## rsc-backend
This docker image is used for backend services

## rsc-batch
This docker image is used for RepoSense jar execution