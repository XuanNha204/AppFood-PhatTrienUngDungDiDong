const express = require('express');
const { db, FieldValue } = require('../config/firebase');
const catalog = require('../data/catalog');
const { users } = require('../data/users');
const { protect } = require('../middleware/auth');
const { getItems, saveItems, buildCartResponse } = require('./cart');
const { now, toJson } = require('../utils/firestore');

const router = express.Router();
router.use(protect);

const ordersRef = db.collection('orders');
const PAYMENT_METHODS = ['COD', 'BANKING'];

function generateCode() {
  const d = new Date();
  const date = `${String(d.getFullYear()).slice(2)}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
  return `FD${date}${Math.floor(1000 + Math.random() * 9000)}`;
}

async function findOwnOrder(req, res) {
  const order = toJson(await ordersRef.doc(req.params.id).get());
  if (!order || order.user !== req.user.id) {
    res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
    return null;
  }
  return order;
}

// POST /api/orders  { address, phone, note, paymentMethod } -> tạo đơn từ giỏ hàng
router.post('/', async (req, res) => {
  const address = String(req.body.address || req.user.address || '').trim();
  const phone = String(req.body.phone || req.user.phone || '').trim();
  if (!address) return res.status(400).json({ message: 'Vui lòng nhập địa chỉ giao hàng' });
  if (!phone) return res.status(400).json({ message: 'Vui lòng nhập số điện thoại' });

  const summary = await buildCartResponse(req.user.id, await getItems(req.user.id));
  if (summary.items.length === 0) return res.status(400).json({ message: 'Giỏ hàng đang trống' });

  const order = {
    code: generateCode(),
    user: req.user.id,
    items: summary.items.map((i) => ({
      food: i.food.id,
      name: i.food.name,
      price: i.food.price,
      imageUrl: i.food.imageUrl,
      quantity: i.quantity,
    })),
    subtotal: summary.subtotal,
    deliveryFee: summary.deliveryFee,
    total: summary.total,
    address,
    phone,
    note: String(req.body.note || '').trim(),
    paymentMethod: PAYMENT_METHODS.includes(req.body.paymentMethod) ? req.body.paymentMethod : 'COD',
    status: 'PENDING',
    createdAt: now(),
    updatedAt: now(),
  };

  // Ghi đơn hàng, tăng lượt bán, xoá giỏ hàng trong cùng 1 batch (thành công hoặc thất bại cùng nhau)
  const ref = ordersRef.doc();
  const batch = db.batch();
  batch.set(ref, order);
  summary.items.forEach((i) =>
    batch.update(db.collection('foods').doc(i.food.id), { soldCount: FieldValue.increment(i.quantity) })
  );
  // Lưu lại địa chỉ/sđt cho lần sau nếu user chưa có
  const profile = {};
  if (!req.user.address) profile.address = address;
  if (!req.user.phone) profile.phone = phone;
  if (Object.keys(profile).length) batch.update(users.doc(req.user.id), profile);
  await batch.commit();

  await saveItems(req.user.id, []);
  catalog.invalidate();
  res.status(201).json({ id: ref.id, ...order });
});

// GET /api/orders (mới nhất trước; sắp xếp trong Node để không cần tạo composite index)
router.get('/', async (req, res) => {
  const snap = await ordersRef.where('user', '==', req.user.id).get();
  const orders = snap.docs.map(toJson).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  res.json(orders);
});

// GET /api/orders/:id
router.get('/:id', async (req, res) => {
  const order = await findOwnOrder(req, res);
  if (order) res.json(order);
});

// PUT /api/orders/:id/cancel -> chỉ huỷ được khi đơn đang chờ xác nhận
router.put('/:id/cancel', async (req, res) => {
  const order = await findOwnOrder(req, res);
  if (!order) return;
  if (order.status !== 'PENDING') {
    return res.status(400).json({ message: 'Chỉ có thể huỷ đơn đang chờ xác nhận' });
  }
  const updatedAt = now();
  await ordersRef.doc(order.id).update({ status: 'CANCELLED', updatedAt });
  res.json({ ...order, status: 'CANCELLED', updatedAt });
});

module.exports = router;
