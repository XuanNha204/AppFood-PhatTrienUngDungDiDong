// Tài khoản đăng nhập nằm ở Firebase Authentication (email + mật khẩu),
// thông tin hồ sơ nằm ở Firestore: users/{uid} với uid là id của Firebase Auth
const { db, auth } = require('../config/firebase');
const { now, toJson } = require('../utils/firestore');

const users = db.collection('users');

const PRIVATE_FIELDS = ['resetOtpHash', 'resetOtpExpires', 'resetOtpAttempts'];

function publicUser(user) {
  if (!user) return null;
  const out = { ...user, favorites: user.favorites || [] };
  PRIVATE_FIELDS.forEach((f) => delete out[f]);
  return out;
}

async function getUser(uid) {
  return toJson(await users.doc(uid).get());
}

async function updateUser(uid, fields) {
  await users.doc(uid).update({ ...fields, updatedAt: now() });
  return getUser(uid);
}

// Tạo hồ sơ nếu chưa có (ví dụ tài khoản được thêm tay trên Firebase Console)
async function ensureProfile(uid, extra = {}) {
  const existing = await getUser(uid);
  if (existing) return existing;
  const record = await auth.getUser(uid);
  const profile = {
    name: extra.name || record.displayName || record.email.split('@')[0],
    email: record.email,
    phone: extra.phone || '',
    address: '',
    avatarUrl: '',
    favorites: [],
    createdAt: now(),
    updatedAt: now(),
  };
  await users.doc(uid).set(profile);
  return { id: uid, ...profile };
}

// Kiểm tra email + mật khẩu bằng Firebase Auth REST API (Admin SDK không có hàm này)
async function verifyPassword(email, password) {
  const key = process.env.FIREBASE_API_KEY;
  const res = await fetch(
    `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${key}`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, returnSecureToken: false }),
    }
  );
  const body = await res.json();
  if (res.ok) return body.localId;

  const code = (body.error && body.error.message) || '';
  const err = new Error('Email hoặc mật khẩu không đúng');
  err.status = 401;
  if (code.startsWith('TOO_MANY_ATTEMPTS')) {
    err.message = 'Nhập sai quá nhiều lần, vui lòng thử lại sau ít phút';
    err.status = 429;
  } else if (code === 'USER_DISABLED') {
    err.message = 'Tài khoản đã bị khoá';
    err.status = 403;
  } else if (code.includes('API key') || code.includes('API_KEY')) {
    err.message = 'FIREBASE_API_KEY trong file .env không đúng';
    err.status = 500;
  }
  throw err;
}

module.exports = { users, publicUser, getUser, updateUser, ensureProfile, verifyPassword };
