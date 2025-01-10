
// {onRequest} = require("firebase-functions/v2/https");
//const logger = require("firebase-functions/logger");

//const functions = require('firebase-functions');
//const admin = require('firebase-admin');

const functions = require('firebase-functions');
const admin = require('firebase-admin');


admin.initializeApp();

exports.sendNotification = functions.region('europe-west1').https.onCall((data, context) => {
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
      return {result: `Message sent to ${token}`};
    })
    .catch((error) => {
      console.error('Error sending message:', error);
      throw new functions.https.HttpsError('internal', 'Error sending message');
    });
});