locals {
  state_dynamodb_table_name = "${var.state_bucket_name}-lock"
}

terraform {
  required_providers {
    aws = {
        source  = "hashicorp/aws"
        version = "~>5.0"
    }
  }

  backend "s3" {
    bucket         = var.state_bucket_name
    # key            = "backend/state.tfstate"
    # key            = "backend/dev.state.tfstate"
    key            = "backend/production.state.tfstate"
    region         = var.region
    dynamodb_table = local.state_dynamodb_table_name
  }
}

provider "aws" {
  region = var.region
}