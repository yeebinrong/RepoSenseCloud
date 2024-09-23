variable "environment" {
    description = "The environment to deploy to"
    type        = string

    validation {
        condition = contains(["dev", "production"], var.environment)
        error_message = "Invalid environment. Must be one of: dev, production"
    }
}

variable "app_code" {
    type        = string
    default     = "rsc"
    description = "The application code"
}

variable "region" {
  type        = string
  default     = "ap-southeast-1" # Singapore region
  description = "The region to use deploy resources to."
}

variable "state_bucket_name" {
  type        = string
  default     = "rsc-opentofu-state"
  description = "The name of the bucket for tracking state."
}