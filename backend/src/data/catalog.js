// Danh mục + món ăn: đọc từ Firestore và cache trong bộ nhớ.
// Dữ liệu nhỏ (vài chục món) nên lọc/sắp xếp ngay trong Node, không cần tạo composite index
// và tiết kiệm lượt đọc của gói Firebase miễn phí (50.000 lượt/ngày).
const { db } = require('../config/firebase');
const { normalize } = require('../utils/text');

const CACHE_TTL_MS = 60 * 1000; // Sửa dữ liệu trực tiếp trên Firebase Console sẽ thấy sau tối đa 1 phút

let cache = null;
let loading = null;

async function load() {
  if (cache && Date.now() - cache.at < CACHE_TTL_MS) return cache;
  if (!loading) {
    loading = Promise.all([db.collection('categories').get(), db.collection('foods').get()])
      .then(([catSnap, foodSnap]) => {
        const categories = catSnap.docs
          .map((d) => ({ id: d.id, ...d.data() }))
          .sort((a, b) => (a.order || 0) - (b.order || 0) || a.name.localeCompare(b.name));
        const catById = new Map(categories.map((c) => [c.id, c]));

        const foods = foodSnap.docs.map((d) => {
          const { searchText, ...data } = d.data();
          const cat = catById.get(data.category);
          return {
            id: d.id,
            ...data,
            category: cat ? { id: cat.id, name: cat.name } : null,
            searchText: searchText || normalize(`${data.name} ${data.description || ''}`),
          };
        });
        cache = { at: Date.now(), categories, foods, foodById: new Map(foods.map((f) => [f.id, f])) };
        return cache;
      })
      .finally(() => {
        loading = null;
      });
  }
  return loading;
}

// Gọi sau khi thêm/sửa/xoá để lần đọc tiếp theo lấy dữ liệu mới
function invalidate() {
  cache = null;
}

// Ẩn trường nội bộ searchText khi trả về cho app
function publicFood(food) {
  if (!food) return null;
  const { searchText, ...rest } = food;
  return rest;
}

async function getCategories() {
  return (await load()).categories;
}

async function getFoods() {
  return (await load()).foods;
}

async function getFood(id) {
  return (await load()).foodById.get(id) || null;
}

module.exports = { getCategories, getFoods, getFood, publicFood, invalidate };
