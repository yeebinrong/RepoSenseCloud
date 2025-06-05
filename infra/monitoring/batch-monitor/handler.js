
// Import AWS SDK
const https = require('https');

// Telegram Bot Configuration
const TELEGRAM_BOT_TOKEN = process.env.TELEGRAM_BOT_TOKEN;
const TELEGRAM_CHAT_ID = process.env.TELEGRAM_CHAT_ID;

const REGION = 'ap-southeast-1'; // or process.env.AWS_REGION

exports.handle = async (event) => {
    try {
        const detail = event.detail;

        const jobId = detail.jobId || 'unknown';
        const jobName = detail.jobName || 'unknown';
        const statusReason = detail.statusReason || 'No reason provided';

        // Log stream (from the last attempt)
        const attempts = detail.attempts || [];
        const latestAttempt = attempts.length > 0 ? attempts[attempts.length - 1] : null;
        const logStreamName = latestAttempt?.container?.logStreamName || detail.container?.logStreamName || 'unknown';
        const logGroupName = '/aws/batch/job';
        const encodedLogGroup = encodeURIComponent(logGroupName);
        const encodedLogStream = encodeURIComponent(logStreamName);

        // Construct CloudWatch Logs URL
        const logUrl = `https://${REGION}.console.aws.amazon.com/cloudwatch/home?region=${REGION}#logsV2:log-groups/log-group/${encodedLogGroup}/log-events/${encodedLogStream}`;

        // Extract owner from tags or environment
        const tags = detail.tags || {};
        let owner = tags.Owner || 'Unknown';

        // If environment is available and owner is there
        const envVars = detail.container?.environment || [];
        const envOwner = envVars.find(e => e.name === 'OWNER')?.value;
        if (envOwner) owner = envOwner;

        // Compose message
        const message = `
🔴 *AWS Batch Job Failed*

*Job Name:* ${jobName}  
*Job ID:* ${jobId}  
*Owner:* ${owner}  
*Status Reason:* ${statusReason}

🔗 *Logs:* ${logUrl}
`.trim();

        // Send message to Telegram
        const telegramUrl = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`;

        const requestData = JSON.stringify({
            chat_id: TELEGRAM_CHAT_ID,
            text: message,
            parse_mode: 'Markdown',
        });

        const options = {
            hostname: 'api.telegram.org',
            path: `/bot${TELEGRAM_BOT_TOKEN}/sendMessage`,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(requestData),
            },
        };

        await new Promise((resolve, reject) => {
            const req = https.request(options, (res) => {
                let data = '';

                res.on('data', (chunk) => (data += chunk));
                res.on('end', () => resolve(data));
            });

            req.on('error', (e) => reject(e));
            req.write(requestData);
            req.end();
        });

        console.log('Message sent successfully to Telegram');

        return {
            statusCode: 200,
            body: JSON.stringify({ message: 'Message sent successfully to Telegram' }),
        };
    } catch (error) {
        console.error('Error sending message to Telegram:', error);
        return {
            statusCode: 500,
            body: JSON.stringify({ message: 'Failed to send message to Telegram', error: error.message }),
        };
    }
};
