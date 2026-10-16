const express = require('express');
const crypto = require('crypto');
const { auth } = require('../config/firebase');
const { users, publicUser, getUser, ensureProfile, verifyPassword } = require('../data/users');
const { protect, signToken } = require('../middleware/auth');
const { sendOtpMail, isConfigured } = require('../utils/mailer');
const { now } = require('../utils/firestore');

const router = express.Router();

const OTP_TTL_MS = 10 * 60 * 1000;
const OTP_MAX_ATTEMPTS = 5;
const hashOtp = (otp) => crypto.createHash('sha256').update(otp).digest('hex');
const cleanEmail = (email) => String(email || '').toLowerCase().trim();

async function findUidByEmail(email) {
  try {
    return (await auth.getUserByEmail(email)).uid;
  } catch (err) {
    if (err.code === 'auth/user-not-found') return null;
    throw err;
  }
}

// POST /api/auth/register
router.post('/register', async (req, res) => {
  const { name, password, phone } = req.body;
  const email = cleanEmail(req.body.email);
  if (!name || !String(name).trim() || !email || !password) {
    return res.status(400).json({ message: 'Vui lòng nhập đầy đủ họ tên, email và mật khẩu' });
  }
  if (password.length < 6) {
    return res.status(400).json({ message: 'Mật khẩu phải có ít nhất 6 ký tự' });
  }

  let record;
  try {
    record = await auth.createUser({ email, password, displayName: String(name).trim() });
  } catch (err) {
    if (err.code === 'auth/email-already-exists') {
      return res.status(409).json({ message: 'Email đã được sử dụng' });
    }
    if (err.code === 'auth/invalid-email') return res.status(400).json({ message: 'Email không hợp lệ' });
    throw err;
  }

  const user = await ensureProfile(record.uid, { name: String(name).trim(), phone: phone || '' });
  res.status(201).json({ token: signToken(user.id), user: publicUser(user) });
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  const email = cleanEmail(req.body.email);
  const { password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ message: 'Vui lòng nhập email và mật khẩu' });
  }
  const uid = await verifyPassword(email, password);
  const user = await ensureProfile(uid);
  res.json({ token: signToken(uid), user: publicUser(user) });
});

// POST /api/auth/forgot-password  { email } -> gửi OTP 6 số qua email
router.post('/forgot-password', async (req, res) => {
  const email = cleanEmail(req.body.email);
  if (!email) return res.status(400).json({ message: 'Vui lòng nhập email' });

  const uid = await findUidByEmail(email);
  if (!uid) return res.status(404).json({ message: 'Email chưa được đăng ký' });
  const user = await ensureProfile(uid);

  const otp = String(crypto.randomInt(100000, 1000000));
  await users.doc(uid).update({
    resetOtpHash: hashOtp(otp),
    resetOtpExpires: Date.now() + OTP_TTL_MS,
    resetOtpAttempts: 0,
  });

  const sent = await sendOtpMail(user.email, user.name, otp);
  const body = { message: sent ? 'Mã OTP đã được gửi tới email của bạn' : 'Mã OTP đã được tạo (chế độ dev)' };
  // Khi chưa cấu hình SMTP (môi trường dev), trả OTP về để tiện demo
  if (!isConfigured() && process.env.NODE_ENV !== 'production') body.devOtp = otp;
  res.json(body);
});

// POST /api/auth/reset-password  { email, otp, newPassword }
router.post('/reset-password', async (req, res) => {
  const { otp, newPassword } = req.body;
  const email = cleanEmail(req.body.email);
  if (!email || !otp || !newPassword) {
    return res.status(400).json({ message: 'Thiếu email, mã OTP hoặc mật khẩu mới' });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ message: 'Mật khẩu phải có ít nhất 6 ký tự' });
  }

  const uid = await findUidByEmail(email);
  const user = uid ? await getUser(uid) : null;
  if (!user || !user.resetOtpHash) {
    return res.status(400).json({ message: 'Vui lòng yêu cầu mã OTP trước' });
  }
  if (user.resetOtpExpires < Date.now()) {
    return res.status(400).json({ message: 'Mã OTP đã hết hạn, vui lòng gửi lại' });
  }
  if ((user.resetOtpAttempts || 0) >= OTP_MAX_ATTEMPTS) {
    return res.status(429).json({ message: 'Nhập sai quá nhiều lần, vui lòng gửi lại mã mới' });
  }
  if (hashOtp(String(otp).trim()) !== user.resetOtpHash) {
    await users.doc(uid).update({ resetOtpAttempts: (user.resetOtpAttempts || 0) + 1 });
    return res.status(400).json({ message: 'Mã OTP không đúng' });
  }

  await auth.updateUser(uid, { password: newPassword });
  await users.doc(uid).update({
    resetOtpHash: null,
    resetOtpExpires: null,
    resetOtpAttempts: 0,
    updatedAt: now(),
  });
  res.json({ message: 'Đặt lại mật khẩu thành công, vui lòng đăng nhập' });
});

// POST /api/auth/change-password  { currentPassword, newPassword }
router.post('/change-password', protect, async (req, res) => {
  const { currentPassword, newPassword } = req.body;
  if (!currentPassword || !newPassword) {
    return res.status(400).json({ message: 'Vui lòng nhập đầy đủ thông tin' });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ message: 'Mật khẩu mới phải có ít nhất 6 ký tự' });
  }
  try {
    await verifyPassword(req.user.email, currentPassword);
  } catch (err) {
    if (err.status === 401) return res.status(400).json({ message: 'Mật khẩu hiện tại không đúng' });
    throw err;
  }
  await auth.updateUser(req.user.id, { password: newPassword });
  res.json({ message: 'Đổi mật khẩu thành công' });
});

module.exports = router;
