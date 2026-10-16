const express = require('express');
const { db } = require('../config/firebase');
const catalog = require('../data/catalog');
const { protect } = require('../middleware/auth');
const { upload } = require('../middleware/upload');
const { resolveImage, deleteImage } = require('../utils/images');
const { normalize } = require('../utils/text');
const { now } = require('../utils/firestore');

const router = express.Router();
const foodsRef = db.collection('foods');

const byNewest = (a, b) => String(b.createdAt).localeCompare(String(a.createdAt));
const SORTS = {
  price_asc: (a, b) => a.price - b.price,
  price_desc: (a, b) => b.price - a.price,
  rating: (a, b) => b.rating - a.rating || b.ratingCount - a.ratingCount,
  popular: (a, b) => b.soldCount - a.soldCount || b.rating - a.rating,
  newest: byNewest,
};
const defaultSort = (a, b) => Number(b.isPopular) - Number(a.isPopular) || byNewest(a, b);

// GET /api/foods?category=&search=&sort=&popular=true&limit=
router.get('/', async (req, res) => {
  const { category, search, sort, popular, limit } = req.query;
  const keyword = search ? normalize(search) : '';
  const foods = (await catalog.getFoods())
    .filter((f) => !category || (f.category && f.category.id === category))
    .filter((f) => popular !== 'true' || f.isPopular)
    .filter((f) => !keyword || f.searchText.includes(keyword))
    .sort(SORTS[sort] || defaultSort)
    .slice(0, Math.min(Number(limit) || 100, 100));
  res.json(foods.map(catalog.publicFood));
});

// GET /api/foods/mine -> các món do user hiện tại đăng
router.get('/mine', protect, async (req, res) => {
  const foods = (await catalog.getFoods()).filter((f) => f.createdBy === req.user.id).sort(byNewest);
  res.json(foods.map(catalog.publicFood));
});

// GET /api/foods/:id
router.get('/:id', async (req, res) => {
  const food = await catalog.getFood(req.params.id);
  if (!food) return res.status(404).json({ message: 'Không tìm thấy món ăn' });
  res.json(catalog.publicFood(food));
});

// Đọc và kiểm tra dữ liệu món ăn từ form. isCreate = true thì bắt buộc tên, giá, danh mục
async function readFoodFields(body, isCreate) {
  const fields = {};
  const text = (v) => (v === undefined ? undefined : String(v).trim());
  const number = (v) => (v === undefined || v === '' ? undefined : Number(v));

  const name = text(body.name);
  if (name !== undefined) {
    if (!name) return { error: 'Vui lòng nhập tên món' };
    fields.name = name.slice(0, 100);
  }
  if (body.description !== undefined) fields.description = text(body.description).slice(0, 2000);

  for (const key of ['price', 'prepTime', 'calories']) {
    const value = number(body[key]);
    if (value === undefined) continue;
    if (!Number.isFinite(value) || value < 0) return { error: 'Giá trị số không hợp lệ' };
    fields[key] = Math.round(value);
  }

  if (body.category) {
    const exists = (await catalog.getCategories()).some((c) => c.id === body.category);
    if (!exists) return { error: 'Danh mục không tồn tại' };
    fields.category = body.category;
  }

  if (isCreate) {
    if (!fields.name) return { error: 'Vui lòng nhập tên món' };
    if (fields.price === undefined) return { error: 'Vui lòng nhập giá' };
    if (!fields.category) return { error: 'Vui lòng chọn danh mục' };
  }
  return { fields };
}

// POST /api/foods  (multipart: name, description, price, category, prepTime, calories, image | imageUrl)
router.post('/', protect, upload.single('image'), async (req, res) => {
  const { fields, error } = await readFoodFields(req.body, true);
  if (error) return res.status(400).json({ message: error });

  const data = {
    description: '',
    prepTime: 15,
    calories: 0,
    ...fields,
    imageUrl: (await resolveImage(req)) || '',
    rating: 4.5,
    ratingCount: 0,
    soldCount: 0,
    isPopular: false,
    createdBy: req.user.id,
    createdAt: now(),
    updatedAt: now(),
  };
  data.searchText = normalize(`${data.name} ${data.description}`);
  const ref = await foodsRef.add(data);
  catalog.invalidate();
  res.status(201).json(catalog.publicFood(await catalog.getFood(ref.id)));
});

async function findOwnedFood(req, res) {
  const food = await catalog.getFood(req.params.id);
  if (!food) {
    res.status(404).json({ message: 'Không tìm thấy món ăn' });
    return null;
  }
  if (food.createdBy !== req.user.id) {
    res.status(403).json({ message: 'Bạn chỉ có thể sửa/xoá món do mình đăng' });
    return null;
  }
  return food;
}

// PUT /api/foods/:id  (multipart, ảnh không bắt buộc)
router.put('/:id', protect, upload.single('image'), async (req, res) => {
  const food = await findOwnedFood(req, res);
  if (!food) return;
  const { fields, error } = await readFoodFields(req.body, false);
  if (error) return res.status(400).json({ message: error });

  const imageUrl = await resolveImage(req);
  if (imageUrl) {
    await deleteImage(food.imageUrl);
    fields.imageUrl = imageUrl;
  }
  const name = fields.name ?? food.name;
  const description = fields.description ?? food.description ?? '';
  await foodsRef.doc(food.id).update({
    ...fields,
    searchText: normalize(`${name} ${description}`),
    updatedAt: now(),
  });
  catalog.invalidate();
  res.json(catalog.publicFood(await catalog.getFood(food.id)));
});

// DELETE /api/foods/:id
router.delete('/:id', protect, async (req, res) => {
  const food = await findOwnedFood(req, res);
  if (!food) return;
  await foodsRef.doc(food.id).delete();
  await deleteImage(food.imageUrl);
  catalog.invalidate();
  res.json({ message: 'Đã xoá món ăn' });
});

module.exports = router;
