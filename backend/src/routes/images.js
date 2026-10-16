const express = require('express');
const { db } = require('../config/firebase');

const router = express.Router();

// GET /images/:id -> trả về file ảnh đã lưu trong Firestore
router.get('/:id', async (req, res) => {
  const doc = await db.collection('images').doc(req.params.id).get();
  if (!doc.exists) return res.status(404).json({ message: 'Không tìm thấy ảnh' });
  const { data, contentType } = doc.data();
  // Mỗi ảnh có id riêng và không bao giờ bị sửa -> cho phép cache lâu dài
  res.set('Cache-Control', 'public, max-age=31536000, immutable');
  res.type(contentType || 'image/jpeg').send(Buffer.from(data));
});

module.exports = router;
