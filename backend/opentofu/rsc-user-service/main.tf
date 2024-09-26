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
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}