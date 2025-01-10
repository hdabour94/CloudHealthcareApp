const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const runtimeOpts = {
  region: 'europe-west1',
  timeoutSeconds: 120, // Optional: Increase timeout for long-running functions
  memory: '256MB', // Optional: Allocate more memory for resource-intensive functions
};

exports.sendNotification = functions.https.onCall((data, context) => {
  const token = data.to;
  const title = data.notification.title;
  const body = data.notification.body;

  const message = {
    notification: {
      title: title,
      body: body,
    },
    token: token,
  };

  return admin.messaging().send(message)
    .then((response) => {
      console.log('Successfully sent message:', response);
      return { result: `Message sent to ${token}` };
    })
    .catch((error) => {
      console.error('Error sending message:', error);
      throw new functions.https.HttpsError('internal', 'Error sending message');
    });
});