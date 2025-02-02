# RepoSenseCloud
On cloud application based on [RepoSense](https://reposense.org/)

## Setup `update-aws-credentials.py`

1. **Ensure you have already added an MFA device to your AWS account**:
   - It should prompt you to add one on login.

2. **Download and install Python** (if not already installed):
   - [Download Python](https://www.python.org/downloads/)

3. **Install boto3 using pip** (if not already installed):
   - If `pip` is accessible:
     ```sh
     pip install boto3
     ```
   - If `pip` is not accessible, use `pip3`:
     ```sh
     pip3 install boto3
     ```
   - If `pip3` is not accessible, use `python3 -m pip`:
     ```sh
     python3 -m pip install boto3
     ```

4. **Configure AWS SSO** using `aws configure sso`:
   - SSO session name (Recommended): `my-sso-session`
   - SSO start URL: `https://hamb-urger.awsapps.com/start`
   - SSO region: `ap-southeast-1`
   - SSO registration scopes: `sso:account:access`
   - Select `DeveloperPermissionSet` role
   - CLI default client Region: `ap-southeast-1`
   - CLI default output format: `None`
   - CLI profile name: `default`

5. **Execute the `update-aws-credentials.py` script** to update your AWS credentials.

6. **Set up an alias** (Highly recommended) to execute the script from any directory:

## Local Development Setup

1. **Copy `.env.sample` to `.env`** and update the values accordingly if necessary.

2. **Run Docker Compose**:
   ```sh
   docker-compose build
   docker-compose up
   ```

3. **Build the rsc-cicd-image**: (TODO future improvement will store this image in cloud)
    ```sh
    cd infra/docker-images/rsc-cicd-image
    docker build -t rsc-cicd-image:common -f Dockerfile .
    ```

4. Ensure AWS credentials are valid by running the `update-aws-credentials.py` from [Setup update-aws-credentials.py](#setup-update-aws-credentialspy)

5. **Run the Docker container in interactive bash mode**:
   - Navigate to the root `ReposenseCloud` project folder.
   - Run the following command:
     - **Windows**:
       ```sh
       docker run -it -v "%cd%":/home/workspace -v "%userprofile%"/.aws:/root/.aws --network="host" rsc-cicd-image:common bash
       ```
     - **Mac**:
       ```sh
       docker run -it -v $PWD:/home/workspace -v $HOME/.aws:/root/.aws --network="host" rsc-cicd-image:common bash
       ```

6. **Deploy the infrastructure**:
   ```sh
   cd workspace
   bash deploy-infra.sh localhost
   ```
   - This script deploys the opentofu state management locally.
   - After the command completes without error, you should be able to view the S3 bucket `rsc-opentofu-state` and DynamoDB table `rsc-opentofu-state-lock` from the LocalStack app (endpoint should be http://localhost:4566)

7. **Deploy OpenTofu for the microservices**
    - Navigate to the opentofu folder (must be running interactive bash mode in the rsc-cicd-image).
    - Run the following commands:
    ```sh
    tofu init  # Downloads the necessary plugins
    tofu plan -var "environment=localhost"  # Checks and ensures no issues with the OpenTofu files
    tofu apply -var "environment=localhost"  # Applies the changes
    ```

8. Hot reload
    - Should be already implemented for `rsc-user-service` when `docker-compose up` is ran

9. For testing backend manually
    - Run `mvn clean install` in backend folder
        - this stores the rsc-common module in local repository so it can be used by other modules
        - If you only have changes in `rsc-common`, you can run it in `backend/rsc-common` folder to speed this up
    - Run `mvn clean package` in the respective module folder to build the jar
        - E.g. running it in `backend/rsc-user-service` will build the jar for user-service in the `target` folder, and it can be run by running
        ```sh
        java -jar target/rsc-user-service-1.0-SNAPSHOT.jar
        ```
        - You should see something like `Started UserServiceApplication in 1.763 seconds (process running for 2.015)`