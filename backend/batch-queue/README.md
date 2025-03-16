
## Send test message to the queue

```
aws sqs send-message --queue-url https://sqs.ap-southeast-1.amazonaws.com/904233118848/sqs-lambda-queue --message-body "Hello, SQS2221" --region ap-southeast-1
```