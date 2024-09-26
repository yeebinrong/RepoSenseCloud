terraform {
  required_providers {
    aws = {
        source  = "hashicorp/aws"
        version = "~>5.0"
    }
  }

  # backend "s3" {
  #   bucket         = var.state_bucket_name
  #   key            = "state.tfstate"
  #   region         = var.region
  #   dynamodb_table = local.state_dynamodb_table_name
  # }
}

provider "aws" {
  region = var.region
  s3_use_path_style = true
  endpoints {
    dynamodb = local.dynamodb_endpoint
    s3       = local.s3_endpoint
  }
}