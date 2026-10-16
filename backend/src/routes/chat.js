const express = require('express');
const { db } = require('../config/firebase');
const catalog = require('../data/catalog');
const { protect } = require('../middleware/auth');
const { getItems, buildCartResponse } = require('./cart');
const { chatCompletion } = require('../utils/ai');
const { now } = require('../utils/firestore');

const router = express.Router();
router.use(protect);

const MAX_STORED = 60; // số tin nhắn lưu lại cho mỗi người
const CONTEXT_MESSAGES = 12; // số tin nhắn gần nhất gửi kèm cho AI
const MAX_INPUT = 500;
const MAX_FOODS_PER_REPLY = 4;

// Lịch sử chat của mỗi user: chats/{uid} = { messages: [{ role, content, foods: [foodId], createdAt }] }
const chatRef = (uid) => db.collection('chats').doc(uid);

async function getMessages(uid) {
  const doc = await chatRef(uid).get();
  return (doc.exists && doc.get('messages')) || [];
}

const money = (n) => `${Number(n).toLocaleString('vi-VN')}đ`;

// Món ăn được đánh số #1, #2... cho gọn, AI trả về số thứ tự thay vì id dài
async function buildMenu() {
  const foods = await catalog.getFoods();
  const lines = foods.map(
    (f, i) =>
      `#${i + 1} ${f.name} | ${f.category ? f.category.name : 'Khác'} | ${money(f.price)} | ${f.rating}★ | ` +
      `${f.calories || '?'} kcal | ${f.prepTime} phút${f.isPopular ? ' | phổ biến' : ''} | ${(f.description || '').slice(0, 90)}`
  );
  return { foods, text: lines.join('\n') };
}

function timeOfDay() {
  const h = Number(new Date().toLocaleString('en-US', { hour: 'numeric', hour12: false, timeZone: 'Asia/Ho_Chi_Minh' }));
  if (h < 10) return 'buổi sáng';
  if (h < 14) return 'buổi trưa';
  if (h < 18) return 'buổi chiều';
  return 'buổi tối';
}

function systemPrompt(user, menuText, cart) {
  const cartText = cart.items.length
    ? cart.items.map((i) => `${i.quantity} x ${i.food.name}`).join(', ') +
      ` (tạm tính ${money(cart.subtotal)}, ${cart.deliveryFee ? `còn thiếu ${money(cart.freeShipThreshold - cart.subtotal)} để được miễn phí giao hàng` : 'đã được miễn phí giao hàng'})`
    : 'trống';
  return `Bạn là "Food AI", trợ lý đặt món thân thiện của ứng dụng Food App.
Khách hàng: ${user.name}. Bây giờ là ${timeOfDay()}.
Giỏ hàng hiện tại: ${cartText}.
Chính sách: miễn phí giao hàng cho đơn từ ${money(cart.freeShipThreshold)}, dưới mức đó phí giao 15.000đ. Thanh toán khi nhận hàng (COD) hoặc chuyển khoản.

THỰC ĐƠN (chỉ được gợi ý các món trong danh sách này, không bịa món hay giá):
${menuText}

Quy tắc trả lời:
- Luôn trả lời bằng tiếng Việt, ngắn gọn (tối đa khoảng 120 từ), giọng thân thiện, có thể dùng 1-2 emoji.
- Khi nhắc tới món, ghi đúng tên và giá theo thực đơn. Có thể dùng **in đậm** và gạch đầu dòng "- ".
- Nếu gợi ý món cụ thể, kết thúc câu trả lời bằng một dòng riêng theo đúng định dạng: [MON: 3, 12] (số thứ tự món, tối đa ${MAX_FOODS_PER_REPLY} món). Nếu không gợi ý món nào thì không ghi dòng này.
- Khách muốn đặt hàng: hướng dẫn bấm nút + trên thẻ món để thêm vào giỏ, rồi vào tab Giỏ hàng để đặt.
- Câu hỏi không liên quan tới ăn uống hoặc ứng dụng: từ chối lịch sự và hướng về chủ đề món ăn.`;
}

// Tách dòng [MON: ...] khỏi câu trả lời và đổi số thứ tự thành id món
function parseReply(raw, foods) {
  const ids = [];
  const content = raw
    .replace(/\[\s*M[OÓ]N\s*:\s*([#\d,\s]+)\]/gi, (match, list) => {
      list
        .split(/[,\s]+/)
        .map((s) => parseInt(s.replace('#', ''), 10))
        .forEach((n) => {
          const food = foods[n - 1];
          if (food && !ids.includes(food.id)) ids.push(food.id);
        });
      return '';
    })
    .trim();
  return { content: content || 'Đây là vài món mình gợi ý cho bạn 👇', foods: ids.slice(0, MAX_FOODS_PER_REPLY) };
}

// Đổi id món thành thông tin món đầy đủ để app hiển thị thẻ món
async function toResponse(message) {
  const foods = await Promise.all((message.foods || []).map((id) => catalog.getFood(id)));
  return { ...message, foods: foods.filter(Boolean).map(catalog.publicFood) };
}

// GET /api/chat -> lịch sử chat
router.get('/', async (req, res) => {
  const messages = await getMessages(req.user.id);
  res.json({ messages: await Promise.all(messages.map(toResponse)) });
});

// POST /api/chat { message } -> câu trả lời của trợ lý
router.post('/', async (req, res) => {
  const text = String(req.body.message || '').trim();
  if (!text) return res.status(400).json({ message: 'Vui lòng nhập nội dung' });
  if (text.length > MAX_INPUT) return res.status(400).json({ message: `Tin nhắn tối đa ${MAX_INPUT} ký tự` });

  const [history, menu, cart] = await Promise.all([
    getMessages(req.user.id),
    buildMenu(),
    getItems(req.user.id).then((items) => buildCartResponse(req.user.id, items)),
  ]);

  const recent = history.slice(-CONTEXT_MESSAGES).map((m) => ({ role: m.role, content: m.content }));
  const raw = await chatCompletion([
    { role: 'system', content: systemPrompt(req.user, menu.text, cart) },
    ...recent,
    { role: 'user', content: text },
  ]);
  const reply = parseReply(raw, menu.foods);

  const userMessage = { role: 'user', content: text, foods: [], createdAt: now() };
  const botMessage = { role: 'assistant', content: reply.content, foods: reply.foods, createdAt: now() };
  await chatRef(req.user.id).set({
    messages: [...history, userMessage, botMessage].slice(-MAX_STORED),
    updatedAt: now(),
  });

  res.json(await toResponse(botMessage));
});

// DELETE /api/chat -> xoá lịch sử
router.delete('/', async (req, res) => {
  await chatRef(req.user.id).delete();
  res.json({ message: 'Đã xoá cuộc trò chuyện' });
});

module.exports = router;
