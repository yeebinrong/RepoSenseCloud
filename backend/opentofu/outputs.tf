output "state_bucket_arn" {
  value = aws_s3_bucket.state.arn
}

output "state_dynamodb_table_arn" {
  value = aws_dynamodb_table.lock.arn
}