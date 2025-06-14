# Create user-data DynamoDB table to track user data
module "ddb_user_data" {
  source = "../modules/dynamodb"
  name                        = "rsc-${var.environment}-user-data"
  hash_key                    = "id"
  table_class                 = "STANDARD"
  deletion_protection_enabled = false
  attributes = [
    {
      name = "id"
      type = "S"
    },
    {
      name = "userName"
      type = "S"
    }
  ]
  global_secondary_indexes = [
    {
      name            = "UserNameIndex"
      hash_key        = "userName"
      projection_type = "ALL"
    },
    {
      name            = "EmailIndex"
      hash_key        = "email"
      projection_type = "ALL"
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}

# Create reset-password-data DynamoDB table to store password reset tokens
module "ddb_reset_password_data" {
  source                        = "../modules/dynamodb"
  name                          = "rsc-${var.environment}-reset-password-data"
  hash_key                      = "email"
  table_class                   = "STANDARD"
  deletion_protection_enabled   = false
  attributes = [
    {
      name = "email"
      type = "S"
    },
    {
      name = "token"
      type = "S"
    },
    {
      name = "expiresAt"
      type = "N"
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}