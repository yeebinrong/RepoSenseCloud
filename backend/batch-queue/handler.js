module.exports.processMessage = async (event) => {
    for (const record of event.Records) {
        console.log("Received message:", record.body);
    }
    return;
};
