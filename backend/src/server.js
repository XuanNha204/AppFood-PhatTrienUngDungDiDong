require('dotenv').config({ quiet: true });
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const os = require('os');

const { db, projectId } = require('./config/firebase');
const { notFound, errorHandler } = require('./middleware/error');

const app = express();

app.use(cors());
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(morgan('dev'));

app.get('/', (req, res) => res.json({ name: 'Food App API', status: 'ok' }));
app.use('/images', require('./routes/images'));
app.use('/api/auth', require('./routes/auth'));
app.use('/api/users', require('./routes/users'));
app.use('/api/categories', require('./routes/categories'));
app.use('/api/foods', require('./routes/foods'));
app.use('/api/cart', require('./routes/cart').router);
app.use('/api/orders', require('./routes/orders'));
app.use('/api/banners', require('./routes/banners'));
app.use('/api/chat', require('./routes/chat'));

app.use(notFound);
app.use(errorHandler);

const PORT = Number(process.env.PORT) || 3000;

function lanAddresses() {
  return Object.values(os.networkInterfaces())
    .flat()
    .filter((i) => i && i.family === 'IPv4' && !i.internal)
    .map((i) => i.address);
}

(async () => {
  if (!process.env.JWT_SECRET) {
    console.error('✖ Thiếu JWT_SECRET trong file .env');
    process.exit(1);
  }
  if (!process.env.FIREBASE_API_KEY) {
    console.error('✖ Thiếu FIREBASE_API_KEY trong file .env');
    process.exit(1);
  }
  try {
    // Đọc thử 1 document để kiểm tra kết nối Firestore ngay khi khởi động
    await db.collection('categories').limit(1).get();
    console.log(`✔ Firebase connected: ${projectId}`);
  } catch (err) {
    console.error('✖ Không kết nối được Firestore:', err.message);
    console.error('  Kiểm tra đã bật Firestore Database trong Firebase Console chưa');
    process.exit(1);
  }
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`✔ API chạy tại http://localhost:${PORT}`);
    console.log(`  • Android Emulator: http://10.0.2.2:${PORT}/`);
    lanAddresses().forEach((ip) => console.log(`  • Điện thoại thật (cùng Wi-Fi): http://${ip}:${PORT}/`));
  });
})();
