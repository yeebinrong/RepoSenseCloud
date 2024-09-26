locals {
  state_dynamodb_table_name = "${var.state_bucket_name}-lock"
  dynamodb_endpoint = var.environment == "localhost" ? "http://localhost:4566" : null
  s3_endpoint       = var.environment == "localhost" ? "http://localhost:4566" : null
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
    key            = "backend/state.tfstate"
    region         = var.region
    dynamodb_table = local.state_dynamodb_table_name
  }
}

provider "aws" {
  region = var.region
  endpoints {
    dynamodb = local.dynamodb_endpoint
    s3       = local.s3_endpoint
  }
}