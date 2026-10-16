const express = require('express');
const { FieldValue } = require('../config/firebase');
const { users, publicUser, updateUser } = require('../data/users');
const catalog = require('../data/catalog');
const { protect } = require('../middleware/auth');
const { upload } = require('../middleware/upload');
const { resolveImage, deleteImage } = require('../utils/images');

const router = express.Router();
router.use(protect);

// GET /api/users/me
router.get('/me', (req, res) => res.json(publicUser(req.user)));

// PUT /api/users/me  { name, phone, address }
router.put('/me', async (req, res) => {
  const { name, phone, address } = req.body;
  const fields = {};
  if (name !== undefined) {
    if (!String(name).trim()) return res.status(400).json({ message: 'Họ tên không được để trống' });
    fields.name = String(name).trim();
  }
  if (phone !== undefined) fields.phone = String(phone).trim();
  if (address !== undefined) fields.address = String(address).trim();
  res.json(publicUser(await updateUser(req.user.id, fields)));
});

// POST /api/users/me/avatar  (multipart field "image" hoặc JSON { imageUrl: "https://..." })
router.post('/me/avatar', upload.single('image'), async (req, res) => {
  const avatarUrl = await resolveImage(req);
  if (!avatarUrl) return res.status(400).json({ message: 'Vui lòng chọn ảnh hoặc nhập link ảnh' });
  await deleteImage(req.user.avatarUrl);
  res.json(publicUser(await updateUser(req.user.id, { avatarUrl })));
});

// GET /api/users/me/favorites -> danh sách món yêu thích
router.get('/me/favorites', async (req, res) => {
  const foods = await Promise.all((req.user.favorites || []).map((id) => catalog.getFood(id)));
  res.json(foods.filter(Boolean).map(catalog.publicFood));
});

// POST /api/users/me/favorites/:foodId -> bật/tắt yêu thích
router.post('/me/favorites/:foodId', async (req, res) => {
  const { foodId } = req.params;
  if (!(await catalog.getFood(foodId))) return res.status(404).json({ message: 'Không tìm thấy món ăn' });

  const isFavorite = !(req.user.favorites || []).includes(foodId);
  await users.doc(req.user.id).update({
    favorites: isFavorite ? FieldValue.arrayUnion(foodId) : FieldValue.arrayRemove(foodId),
  });
  const updated = (await users.doc(req.user.id).get()).get('favorites') || [];
  res.json({ isFavorite, favorites: updated });
});

module.exports = router;
