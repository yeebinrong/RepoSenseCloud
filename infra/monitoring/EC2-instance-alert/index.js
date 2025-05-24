const { EC2Client, DescribeInstancesCommand } = require("@aws-sdk/client-ec2");

const ec2 = new EC2Client({ region: "ap-southeast-1" });

// Telegram Bot Configuration
const TELEGRAM_BOT_TOKEN = 'xxx';// Replace with your Telegram bot token
const TELEGRAM_CHAT_ID = 'xxx'; // Replace with your Telegram chat ID

async function sendTelegramMessage(message) {
    const telegramApiUrl = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`;
    const payload = {
        chat_id: TELEGRAM_CHAT_ID,
        text: message,
        parse_mode: 'HTML'
    };

    try {
        const response = await fetch(telegramApiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            throw new Error(`Telegram API error: ${response.statusText}`);
        }

        const jsonResponse = await response.json();
        console.log('Telegram response:', jsonResponse); 
        return true;       
    } catch (error) {
        console.error('Error sending message to Telegram:', error);
        return false;
    }
}

exports.handler = async (event) => {
    let message = '';
    let statusCode = 200;

    try {
        const data = await ec2.send(new DescribeInstancesCommand({}));

        const instanceInfo = [];
        data.Reservations.forEach(reservation => {
            reservation.Instances.forEach(instance => {
                const instanceId = instance.InstanceId;
                const instanceType = instance.InstanceType;
                const state = instance.State.Name;
                const launchTime = instance.LaunchTime.toISOString().replace(/T/, ' ').replace(/\..+/, '') + ' UTC';

                let instanceName = 'N/A';
                if (instance.Tags){
                    const nameTag = instance.Tags.find(tag => tag.Key === 'Name');
                    if (nameTag) {
                        instanceName = nameTag.Value;
                    }
                }

                instanceInfo.push({
                    Name: instanceName,
                    ID: instanceId,
                    Type: instanceType,
                    State: state,
                    LaunchTime: launchTime,
                });
            })
        })

        if (instanceInfo.length === 0) {
            message = "No EC2 instances found in your account.";
        } else {
            message = "<b>Daily EC2 Instance Report:</b>\n";
            message += "<b>EC2 Dashboard:</b> https://ap-southeast-1.console.aws.amazon.com/ec2/home?region=ap-southeast-1#Home:\n\n";
            instanceInfo.forEach((inst, index) => {
                message += `<b>${index + 1}. Name:</b> ${inst.Name}\n`;
                message += `  <b>ID:</b> <code>${inst.ID}</code>\n`;
                message += `  <b>Type:</b> ${inst.Type}\n`;
                message += `  <b>State:</b> ${inst.State}\n`;
                message += `  <b>Launch Time:</b> ${inst.LaunchTime}\n`;
                message += "\n";
            });
        }

        await sendTelegramMessage(message);
        console.log('EC2 instance report sent to Telegram successfully!');

    } catch (error) {
        console.error(`Error generating EC2 report: ${error.message}`);
        message = `🚨 <b>ERROR:</b> Error generating EC2 report: ${error.message}`;
        await sendTelegramMessage(message);
        statusCode = 500;
    }

    return {
        statusCode: statusCode,
        body: JSON.stringify(message),
    };
};