const express = require('express');
const { db } = require('../config/firebase');
const catalog = require('../data/catalog');
const { protect } = require('../middleware/auth');
const { upload } = require('../middleware/upload');
const { resolveImage } = require('../utils/images');
const { now } = require('../utils/firestore');

const router = express.Router();

// GET /api/categories -> kèm số lượng món trong mỗi danh mục
router.get('/', async (req, res) => {
  const [categories, foods] = await Promise.all([catalog.getCategories(), catalog.getFoods()]);
  const counts = new Map();
  foods.forEach((f) => f.category && counts.set(f.category.id, (counts.get(f.category.id) || 0) + 1));
  res.json(categories.map((c) => ({ ...c, foodCount: counts.get(c.id) || 0 })));
});

// GET /api/categories/:id
router.get('/:id', async (req, res) => {
  const category = (await catalog.getCategories()).find((c) => c.id === req.params.id);
  if (!category) return res.status(404).json({ message: 'Không tìm thấy danh mục' });
  res.json(category);
});

// POST /api/categories (multipart: name, description, image hoặc imageUrl)
router.post('/', protect, upload.single('image'), async (req, res) => {
  const name = String(req.body.name || '').trim();
  if (!name) return res.status(400).json({ message: 'Vui lòng nhập tên danh mục' });
  const data = {
    name,
    description: req.body.description || '',
    imageUrl: (await resolveImage(req)) || '',
    order: (await catalog.getCategories()).length + 1,
    createdAt: now(),
    updatedAt: now(),
  };
  const ref = await db.collection('categories').add(data);
  catalog.invalidate();
  res.status(201).json({ id: ref.id, ...data });
});

module.exports = router;
