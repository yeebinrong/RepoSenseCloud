# RepoSenseCloud
On cloud application based on https://reposense.org/

# Local development setup
1. Copy .env.sample to .env and  if necessary update the values accordingly
2. Run `docker-compose build` and `docker-compose up` to run the LocalStack application
3. Build the rsc-cicd-image
    a. Navigate to infra/docker-images/rsc-cicd-image
    b. Run `docker build -t rsc-cicd-image:common -f Dockerfile .` (TODO future improvement will store this image in cloud)
4. Setup dummy AWS credentials using `aws configure` in command prompt, put dummy values for access key id and secret access key
5. Run the image in interactive bash mode using the command
    a. `docker run -it -v "%cd%":/home/workspace -v "%userprofile%"/.aws:/root/.aws rsc-cicd-image:common bash`, this command binds the current working directory to `/home/workspace`, for mac / alternative is to replace "%cd%" with the pwd command or absolute pathing
    b. Navigate to the workspace `cd workspace`
    c. Run `bash deploy-infra.sh localhost`. This script deploys the opentofu state management locally.
    d. After the command completes without error, you should be able to view the S3 bucket `rsc-opentofu-state` and DynamoDB table `rsc-opentofu-state-lock` from the LocalStack app (endpoint should be http://localhost:4566)
6. For deploying OpenTofu for the microservices, there is no script created yet, you can use the following commands while in the `backend/opentofu` folder (must be running interactive bash mode in the `rsc-cicd-image`)
    a. `tofu init` (Downloads the necessary plugins)
    b. `tofu plan -var "environment=localhost"` (Checks and make sures no issues with the opentofu files)
    c. `tofu apply -var "environment=localhost"` (Applies the changes)
7. For testing backend manually
    a. Run `mvn clean install` in backend folder (this stores the rsc-common module in local repository so it can be used by other modules), if you only have changes in `rsc-common`, you can run it in `backend/rsc-common` folder to speed this up
    b. Run `mvn clean package` in the respective module folder to build the jar, e.g. running it in `backend/rsc-user-service` will build the jar for user-service in the `target` folder, and it can be ran by running `java -jar target/rsc-user-service-1.0-SNAPSHOT.jar`
    c. You should see something like `Started UserServiceApplication in 1.763 seconds (process running for 2.015)`
    d. Hot reload not yet implemented so have to rebuild the jar and rerun it everytime.