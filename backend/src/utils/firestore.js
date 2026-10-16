const now = () => new Date().toISOString();

// Firestore document -> object JSON trả về cho app (thêm trường id)
function toJson(doc) {
  if (!doc || !doc.exists) return null;
  return { id: doc.id, ...doc.data() };
}

module.exports = { now, toJson };
