

const { DynamoDBClient, ScanCommand } = require("@aws-sdk/client-dynamodb");
const { SQSClient, SendMessageCommand } = require("@aws-sdk/client-sqs");
const { unmarshall } = require("@aws-sdk/util-dynamodb");

const dynamo = new DynamoDBClient();
const sqs = new SQSClient();

// Environment variables set in serverless.yml
const TABLE_NAME = process.env.TABLE_NAME;
const QUEUE_URL = process.env.QUEUE_URL;

const main = async () => {
    const now = new Date();
    const fiveMinAgo = new Date(now.getTime() - 5 * 60 * 1000);

    const pad = (n) => String(n).padStart(2, '0');

    const toIsoDateTime = (date, time) => {
        return `${date}T${time}Z`; // Assumes UTC time
    };

    const params = {
        TableName: TABLE_NAME,
        FilterExpression: "#s = :pending",
        ExpressionAttributeNames: {
            "#s": "status"
        },
        ExpressionAttributeValues: {
            ":pending": { S: "Pending" }
        }
    };

    try {
        const command = new ScanCommand(params);
        const data = await dynamo.send(command);
        const tasksToSend = [];

        for (const item of data.Items) {
            const next = item.nextScheduled;
            const task = unmarshall(item); // optional: flatten to JS object
            if (!next?.M?.date?.S || !next?.M?.time?.S) {
                console.log(`⚠️ Task ${task.jobId || "(no id)"} has no next scheduled date/time`);
                continue;
            }

            const scheduledAt = new Date(toIsoDateTime(next.M.date.S, next.M.time.S));
            if (scheduledAt >= fiveMinAgo && scheduledAt < now) {
                tasksToSend.push(task);

                const sqsCommand = new SendMessageCommand({
                    QueueUrl: QUEUE_URL,
                    MessageBody: JSON.stringify(task)
                });

                await sqs.send(sqsCommand);
                console.log(`📤 Sent task ${task.jobId || "(no id)"} to SQS`);
            } else {
                console.log(`⏰ Task ${item.jobId || "(no id)"} is not ready yet, scheduled for ${scheduledAt}`);
            }
        }

        console.log(`✅ Total tasks sent to SQS: ${tasksToSend.length}`);

        return {
            statusCode: 200,
            body: JSON.stringify({ count: tasksToSend.length })
        };
    } catch (err) {
        console.error("❌ Error:", err);
        return {
            statusCode: 500,
            body: JSON.stringify({ error: err.message })
        };
    }
};

module.exports.main = main;