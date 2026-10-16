const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');

const KEY_PATH = path.resolve(__dirname, '..', '..', process.env.FIREBASE_KEY_FILE || 'serviceAccountKey.json');

if (!fs.existsSync(KEY_PATH)) {
  console.error(`✖ Không tìm thấy file ${KEY_PATH}`);
  console.error('  Tải tại Firebase Console > Project settings > Service accounts > Generate new private key');
  process.exit(1);
}

const serviceAccount = require(KEY_PATH);
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

const db = admin.firestore();
db.settings({ ignoreUndefinedProperties: true });

module.exports = {
  admin,
  db,
  auth: admin.auth(),
  FieldValue: admin.firestore.FieldValue,
  projectId: serviceAccount.project_id,
};
