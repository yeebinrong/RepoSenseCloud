
/*
# IAM Role for Lambda
resource "aws_iam_role" "lambda_execution_role" {
  name               = "lambda_cost_report_execution_role"
  assume_role_policy = jsonencode({
    Version : "2012-10-17"
    Statement : [{
      Action    : "sts:AssumeRole"
      Effect    : "Allow"
      Principal : { Service : "lambda.amazonaws.com" }
    }]
  })
}

# IAM Policy for Lambda
resource "aws_iam_policy" "lambda_policy" {
  name   = "lambda_cost_report_policy"
  policy = jsonencode({
    Version : "2012-10-17"
    Statement : [
      {
        Action   : "logs:CreateLogGroup",
        Effect   : "Allow",
        Resource : "arn:aws:logs:*:*:*"
      },
      {
        Action   : [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Effect   : "Allow",
        Resource : "arn:aws:logs:*:*:log-group:/aws/lambda/*:*"
      },
      {
        Action   : "ce:GetCostAndUsage",
        Effect   : "Allow",
        Resource : "*"
      }
    ]
  })
}

# Attach Policy to Role
resource "aws_iam_role_policy_attachment" "lambda_role_policy_attachment" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = aws_iam_policy.lambda_policy.arn
}

# Lambda Function
resource "aws_lambda_function" "cost_report_lambda" {
  function_name = "cost_report_to_telegram"
  role          = aws_iam_role.lambda_execution_role.arn
  handler       = "sendCostReport.handler" # Ensure this matches your function's entry point
  runtime       = "nodejs18.x"
  timeout       = 30

  # Reference the index.js file directly
  filename = "${path.module}/sendCostReport.zip"
  source_code_hash = filebase64sha256("sendCostReport.zip")

  # Environment Variables for Telegram Bot
  environment {
    variables = {
      TELEGRAM_BOT_TOKEN = "xxx" # Replace with your bot token
      TELEGRAM_CHAT_ID   = "xxx"                          # Replace with your chat ID
    }
  }
}

# EventBridge Rule to Trigger Lambda Daily
resource "aws_cloudwatch_event_rule" "daily_trigger" {
  name                = "daily_lambda_trigger"
  schedule_expression = "rate(1 day)"
}

# EventBridge Target to Trigger Lambda
resource "aws_cloudwatch_event_target" "lambda_target" {
  rule      = aws_cloudwatch_event_rule.daily_trigger.name
  target_id = "lambda_cost_report"
  arn       = aws_lambda_function.cost_report_lambda.arn
}

# Grant EventBridge Permission to Invoke Lambda
resource "aws_lambda_permission" "allow_eventbridge_invoke" {
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.cost_report_lambda.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.daily_trigger.arn
}
*/