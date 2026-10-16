const express = require('express');
const catalog = require('../data/catalog');

const router = express.Router();

// Banner khuyến mãi hiển thị ở trang chủ (gắn với danh mục để bấm vào xem món)
const BANNERS = [
  {
    title: 'Freeship đơn từ 200K',
    subtitle: 'Áp dụng cho mọi món ăn',
    imageUrl: 'https://www.themealdb.com/images/media/meals/utxryw1511721587.jpg',
    categoryName: 'Bữa sáng',
  },
  {
    title: 'Hải sản tươi ngon',
    subtitle: 'Giảm đến 30% hôm nay',
    imageUrl: 'https://www.themealdb.com/images/media/meals/1548772327.jpg',
    categoryName: 'Hải sản',
  },
  {
    title: 'Ngọt ngào cuối tuần',
    subtitle: 'Tráng miệng chỉ từ 25K',
    imageUrl: 'https://www.themealdb.com/images/media/meals/xvsurr1511719182.jpg',
    categoryName: 'Tráng miệng',
  },
];

router.get('/', async (req, res) => {
  const byName = new Map((await catalog.getCategories()).map((c) => [c.name, c.id]));
  res.json(BANNERS.map((b, i) => ({ id: String(i + 1), ...b, categoryId: byName.get(b.categoryName) || null })));
});

module.exports = router;
