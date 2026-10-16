const express = require('express');
const { db } = require('../config/firebase');
const catalog = require('../data/catalog');
const { protect } = require('../middleware/auth');
const { now } = require('../utils/firestore');

const router = express.Router();
router.use(protect);

const FREE_SHIP_THRESHOLD = 200000;
const DELIVERY_FEE = 15000;
const MAX_QTY = 99;

// Mỗi user có 1 giỏ hàng: carts/{uid} = { items: [{ food: foodId, quantity }] }
const cartRef = (userId) => db.collection('carts').doc(userId);

async function getItems(userId) {
  const doc = await cartRef(userId).get();
  return (doc.exists && doc.get('items')) || [];
}

async function saveItems(userId, items) {
  await cartRef(userId).set({ items, updatedAt: now() });
}

// Tính toán tổng tiền, loại bỏ món đã bị xoá khỏi hệ thống
async function buildCartResponse(userId, items) {
  const resolved = await Promise.all(
    items.map(async (i) => ({ food: await catalog.getFood(i.food), quantity: i.quantity }))
  );
  const valid = resolved.filter((i) => i.food);
  if (valid.length !== items.length) {
    await saveItems(userId, valid.map((i) => ({ food: i.food.id, quantity: i.quantity })));
  }
  const lines = valid.map((i) => ({
    food: catalog.publicFood(i.food),
    quantity: i.quantity,
    lineTotal: i.food.price * i.quantity,
  }));
  const subtotal = lines.reduce((sum, i) => sum + i.lineTotal, 0);
  const deliveryFee = subtotal === 0 || subtotal >= FREE_SHIP_THRESHOLD ? 0 : DELIVERY_FEE;
  return {
    items: lines,
    totalQuantity: lines.reduce((sum, i) => sum + i.quantity, 0),
    subtotal,
    deliveryFee,
    total: subtotal + deliveryFee,
    freeShipThreshold: FREE_SHIP_THRESHOLD,
  };
}

async function respondWithCart(res, userId, items) {
  res.json(await buildCartResponse(userId, items));
}

// GET /api/cart
router.get('/', async (req, res) => {
  await respondWithCart(res, req.user.id, await getItems(req.user.id));
});

// POST /api/cart/items  { foodId, quantity } -> cộng dồn số lượng
router.post('/items', async (req, res) => {
  const { foodId } = req.body;
  const quantity = Math.max(1, parseInt(req.body.quantity, 10) || 1);
  if (!foodId || !(await catalog.getFood(foodId))) {
    return res.status(404).json({ message: 'Không tìm thấy món ăn' });
  }
  const items = await getItems(req.user.id);
  const item = items.find((i) => i.food === foodId);
  if (item) item.quantity = Math.min(MAX_QTY, item.quantity + quantity);
  else items.push({ food: foodId, quantity: Math.min(MAX_QTY, quantity) });
  await saveItems(req.user.id, items);
  await respondWithCart(res, req.user.id, items);
});

// PUT /api/cart/items/:foodId  { quantity } -> quantity <= 0 sẽ xoá món
router.put('/items/:foodId', async (req, res) => {
  const quantity = parseInt(req.body.quantity, 10);
  if (Number.isNaN(quantity)) return res.status(400).json({ message: 'Số lượng không hợp lệ' });
  let items = await getItems(req.user.id);
  const item = items.find((i) => i.food === req.params.foodId);
  if (!item) return res.status(404).json({ message: 'Món không có trong giỏ hàng' });
  if (quantity <= 0) items = items.filter((i) => i !== item);
  else item.quantity = Math.min(MAX_QTY, quantity);
  await saveItems(req.user.id, items);
  await respondWithCart(res, req.user.id, items);
});

// DELETE /api/cart/items/:foodId
router.delete('/items/:foodId', async (req, res) => {
  const items = (await getItems(req.user.id)).filter((i) => i.food !== req.params.foodId);
  await saveItems(req.user.id, items);
  await respondWithCart(res, req.user.id, items);
});

// DELETE /api/cart -> xoá toàn bộ giỏ
router.delete('/', async (req, res) => {
  await saveItems(req.user.id, []);
  await respondWithCart(res, req.user.id, []);
});

module.exports = { router, getItems, saveItems, buildCartResponse };
