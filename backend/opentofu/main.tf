#### Declare modules ####

module "rsc-user-service" {
  environment = "${var.environment}"
  source = "./rsc-user-service"
}