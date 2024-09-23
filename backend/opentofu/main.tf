locals {
  state_dynamodb_table_name = "${var.state_bucket_name}-lock"
}

resource "aws_s3_bucket" "state" {
  bucket        = var.state_bucket_name
  force_destroy = false
}

resource "aws_s3_bucket_ownership_controls" "state" {
  bucket = aws_s3_bucket.state.id
  rule {
    # this setting disables ACLs
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "state" {
  bucket = aws_s3_bucket.state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_dynamodb_table" "lock" {
  name             = local.state_dynamodb_table_name
  billing_mode     = "PAY_PER_REQUEST"
  hash_key         = "LockID"
  stream_enabled   = true
  stream_view_type = "KEYS_ONLY"

  attribute {
    name = "LockID"
    type = "S"
  }

  server_side_encryption {
    enabled = true
  }
}

module "ddb_user_data" {
  source = "./modules/dynamodb"
  name                        = "${var.app_code}-${var.environment}-user-data"
  hash_key                    = "username"
  table_class                 = "STANDARD"
  deletion_protection_enabled = false
  attributes = [
    {
      name = "username"
      type = "S"
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}

module "ddb_job_config" {
  source = "./modules/dynamodb"
  name                        = "${var.app_code}-${var.environment}-job-config"
  hash_key                    = "job_id"
  table_class                 = "STANDARD"
  deletion_protection_enabled = false
  attributes = [
    {
      name = "job_id"
      type = "S"
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}
