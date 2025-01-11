
// Import AWS SDK
const { CostExplorer } = require('@aws-sdk/client-cost-explorer');
const https = require('https');

// Telegram Bot Configuration
const TELEGRAM_BOT_TOKEN = '8138670411:AAEZVRNO0RSVGrRwVm5uFTH4829Ys0IXmbU'; // Replace with your Telegram bot token
const TELEGRAM_CHAT_ID = '-1002362374639'; // Replace with your Telegram chat ID

const handler = async (event) => {
  try {
    // Get yesterday's date range
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    const startDate = yesterday.toISOString().split('T')[0];
    const endDate = today.toISOString().split('T')[0];

    // Initialize AWS SDK client
    const costExplorer = new CostExplorer({ region: 'ap-southeast-1' });

    // Fetch daily cost from AWS Cost Explorer
    const costData = await costExplorer.getCostAndUsage({
      TimePeriod: {
        Start: startDate,
        End: endDate,
      },
      Granularity: 'DAILY',
      Metrics: ['UnblendedCost'],
    });

    const dailyCost = costData.ResultsByTime[0].Total.UnblendedCost.Amount;
    const currency = costData.ResultsByTime[0].Total.UnblendedCost.Unit;

    // Telegram message content
    const message = `\u{1F4C8} *AWS Daily Cost Report*

*Date:* ${startDate}
*Cost:* ${dailyCost} ${currency}`;

    // Send message to Telegram using HTTPS
    const telegramUrl = `/bot${TELEGRAM_BOT_TOKEN}/sendMessage`;
    const postData = JSON.stringify({
      chat_id: TELEGRAM_CHAT_ID,
      text: message,
      parse_mode: 'Markdown',
    });

    const options = {
      hostname: 'api.telegram.org',
      port: 443,
      path: telegramUrl,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': postData.length,
      },
    };

    await new Promise((resolve, reject) => {
      const req = https.request(options, (res) => {
        let data = '';

        res.on('data', (chunk) => {
          data += chunk;
        });

        res.on('end', () => {
          if (res.statusCode === 200) {
            console.log('Message sent successfully to Telegram');
            resolve();
          } else {
            console.error('Failed to send message:', data);
            reject(new Error(data));
          }
        });
      });

      req.on('error', (error) => {
        console.error('Error with request:', error);
        reject(error);
      });

      req.write(postData);
      req.end();
    });

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

module.exports = {
  handler,
};