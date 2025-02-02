# RepoSenseCloud
On cloud application based on https://reposense.org/

# Setup update-aws-credentials.py
1. Ensure you have already added a MFA device to your AWS account, it should prompt to add on login.
2. (Can skip if already installed) Download and install python from python.org
3. (Can skip if already installed) Install boto3 using pip `pip install boto3`

   - If `pip` is not accessible, use `pip3`:

     ```sh
     pip3 install boto3
     ```

   - If `pip3` is not accessible, use `python3 -m pip`:

     ```sh
     python3 -m pip install boto3
     ```

4. Configure AWS SSO using `aws configure sso`

    - SSO session name (Recommended): `my-sso-session`
    - SSO start URL [None]: `https://hamb-urger.awsapps.com/start`
    - SSO region: `ap-southeast-1`
    - SSO registration scopes: `sso:account:access`
    - Select `DeveloperPermissionSet` role
    - CLI default client Region: `ap-southeast-1`
    - CLI default output format: `None`
    - CLI profile name: `default`

4. Execute the `update-aws-credentials.py` python script to update your aws credentials
5. (Highly recommended) You can use `alias` (available for both windows and mac) to setup shortcut to execute this script from any directory

# Local development setup
1. Copy .env.sample to .env and  if necessary update the values accordingly
2. Run `docker-compose build` and `docker-compose up` to run the LocalStack application
3. Build the rsc-cicd-image
    a. Navigate to infra/docker-images/rsc-cicd-image
    b. Run `docker build -t rsc-cicd-image:common -f Dockerfile .` (TODO future improvement will store this image in cloud)
4. Ensure AWS credentials are valid by running the `update-aws-credentials.py` from [Setup update-aws-credentials.py](#setup-update-aws-credentialspy)
5. CD to the root ReposenseCloud project folder and run the image in interactive bash mode using the command

    a.
        Windows - `docker run -it -v "%cd%":/home/workspace -v "%userprofile%"/.aws:/root/.aws --network="host" rsc-cicd-image:common bash`, this command binds the current working directory to docker container's `/home/workspace`
        Mac - `docker run -it -v $PWD:/home/workspace -v $HOME/.aws:/root/.aws --network="host" rsc-cicd-image:common bash`

    b. Navigate to the workspace `cd workspace`

    c. Run `bash deploy-infra.sh localhost`. This script deploys the opentofu state management locally.

    d. After the command completes without error, you should be able to view the S3 bucket `rsc-opentofu-state` and DynamoDB table `rsc-opentofu-state-lock` from the LocalStack app (endpoint should be http://localhost:4566)

6. For deploying OpenTofu for the microservices, there is no script created yet, you can use the following commands while in the `backend/opentofu` folder (must be running interactive bash mode in the `rsc-cicd-image`)

    a. `tofu init` (Downloads the necessary plugins)

    b. `tofu plan -var "environment=localhost"` (Checks and make sures no issues with the opentofu files)

    c. `tofu apply -var "environment=localhost"` (Applies the changes)

9. Hot reload

    a. Should be already implemented for rsc-user-service when docker-compose up is ran

8. For testing backend manually

    a. Run `mvn clean install` in backend folder (this stores the rsc-common module in local repository so it can be used by other modules), if you only have changes in `rsc-common`, you can run it in `backend/rsc-common` folder to speed this up

    b. Run `mvn clean package` in the respective module folder to build the jar, e.g. running it in `backend/rsc-user-service` will build the jar for user-service in the `target` folder, and it can be run by running `java -jar target/rsc-user-service-1.0-SNAPSHOT.jar`

    c. You should see something like `Started UserServiceApplication in 1.763 seconds (process running for 2.015)`