
// Import AWS SDK
const { CostExplorer } = require('@aws-sdk/client-cost-explorer');
const https = require('https');

// Telegram Bot Configuration
const TELEGRAM_BOT_TOKEN = 'xxx';// Replace with your Telegram bot token
const TELEGRAM_CHAT_ID = 'xxx'; // Replace with your Telegram chat ID


exports.handler = async (event) => {
  try {
    // Get today's date and calculate the first day of the current month
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const startDate = yesterday.toISOString().split('T')[0]; // Yesterday's date
    const endDate = today.toISOString().split('T')[0]; // Today's date
    const startOfMonthStr = startOfMonth.toISOString().split('T')[0]; // Start of the month

    // Day of the week
    const dayOfWeek = yesterday.toLocaleString('en-US', { weekday: 'long' });

    // Initialize AWS SDK client
    const costExplorer = new CostExplorer({ region: 'us-east-1' });

    // Fetch daily cost (yesterday)
    const dailyCostData = await costExplorer.getCostAndUsage({
      TimePeriod: {
        Start: startDate,
        End: endDate,
      },
      Granularity: 'DAILY',
      Metrics: ['UnblendedCost'],
    });

    const dailyCost = dailyCostData.ResultsByTime[0].Total.UnblendedCost.Amount * 1.35;
   // const currency = dailyCostData.ResultsByTime[0].Total.UnblendedCost.Unit;

    // Fetch monthly cost (start of the month to yesterday)
    const monthlyCostData = await costExplorer.getCostAndUsage({
      TimePeriod: {
        Start: startOfMonthStr,
        End: endDate,
      },
      Granularity: 'MONTHLY',
      Metrics: ['UnblendedCost'],
    });

    const monthlyCost = monthlyCostData.ResultsByTime[0].Total.UnblendedCost.Amount * 1.35;

    // Telegram message content
    const message = `
\u{1F4C8} *AWS Cost Report*
------------------------
*Date:* ${startDate} (${dayOfWeek})
*Daily Cost:* ${dailyCost} SGD
*Monthly Cost:* ${monthlyCost} SGD
`;

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
