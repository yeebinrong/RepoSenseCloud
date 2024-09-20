# docker-images is to manage the docker images used in the development / deployment of RepoSenseCloud

## reposensecloud-cicd
This is docker image is used for CICD

Build docker image using following command
```
docker build -t reposensecloud-cicd:latest -f Dockerfile .
```
Run the docker image using following command
```
docker run -it reposensecloud-cicd:latests bash
```