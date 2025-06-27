variable "environment" {
    description = "The environment to deploy to"
    type        = string

    validation {
        condition = contains(["localhost", "dev", "production"], var.environment)
        error_message = "Invalid environment. Must be one of: localhost, dev, production"
    }
}

variable "region" {
  type        = string
  default     = "ap-southeast-1" # Singapore region
  description = "The region to use deploy resources to."
}