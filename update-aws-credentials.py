import os
import json
import subprocess
import boto3
from configparser import ConfigParser

# AWS SSO Profile Name
SSO_PROFILE = "burger"

def run_sso_login(profile):
    """Runs AWS SSO login for the given profile."""
    try:
        subprocess.run(["aws", "sso", "login", "--profile", profile], check=True)
        print(f"✅ Successfully logged in to AWS SSO ({profile})")
    except subprocess.CalledProcessError:
        print("❌ AWS SSO login failed. Please check your SSO configuration.")
        exit(1)

def get_sso_credentials(profile):
    """Retrieves AWS temporary credentials for the given SSO profile."""
    session = boto3.Session(profile_name=profile)
    credentials = session.get_credentials()

    if not credentials:
        print("❌ Failed to retrieve SSO credentials.")
        exit(1)

    return {
        "aws_access_key_id": credentials.access_key,
        "aws_secret_access_key": credentials.secret_key,
        "aws_session_token": credentials.token
    }

def update_aws_credentials(profile, credentials):
    """Updates the AWS credentials file (~/.aws/credentials)."""
    credentials_file = os.path.expanduser("~/.aws/credentials")
    config = ConfigParser()
    
    if os.path.exists(credentials_file):
        config.read(credentials_file)

    if profile not in config:
        config.add_section(profile)

    config[profile]["aws_access_key_id"] = credentials["aws_access_key_id"]
    config[profile]["aws_secret_access_key"] = credentials["aws_secret_access_key"]
    config[profile]["aws_session_token"] = credentials["aws_session_token"]

    with open(credentials_file, "w") as file:
        config.write(file)

    print(f"✅ AWS credentials updated for profile: {profile}")

if __name__ == "__main__":
    run_sso_login(SSO_PROFILE)
    creds = get_sso_credentials(SSO_PROFILE)
    update_aws_credentials(SSO_PROFILE, creds)
