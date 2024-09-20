# docker-images is to manage the docker images used in the development / deployment of RepoSenseCloud

Build the docker images manually using following command
```
docker build -t {IMAGE_NAME}:{TAG} -f Dockerfile .
```

## reposensecloud-cicd
This docker image is used for CICD

## reposensecloud-front
This docker image is used for local development the frontend

## reposensecloud-java8
This docker image is used for backend services

## reposensencloud-java11
This docker image is used for Reposense jar execution