module "ddb_job_data" {
  source = "../modules/dynamodb"
  name                        = "rsc-${var.environment}-job-data"
  hash_key                    = "owner"      # Partition key
  range_key                   = "jobId"      # Sort key
  table_class                 = "STANDARD"
  deletion_protection_enabled = false
  attributes = [
    {
      name = "jobId"
      type = "S"
    },
    {
      name = "owner"
      type = "S"
    }
  ]
  tags = {
    Opentofu   = "true"
    Environment = "${var.environment}"
  }
}