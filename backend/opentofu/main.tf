#### Declare modules ####

module "rsc-user-service" {
  environment = "${var.environment}"
  source = "./rsc-user-service"
}

module "rsc-job-service" {
  environment = "${var.environment}"
  source = "./rsc-job-service"
}