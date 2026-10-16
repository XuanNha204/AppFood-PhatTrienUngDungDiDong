# Script sinh icon vector / shape / animation cho project (chạy 1 lần: python tools/gen_res.py)
import os

R = 'app/src/main/res'
for d in ('drawable', 'anim'):
    os.makedirs(f'{R}/{d}', exist_ok=True)

ICONS = {
    'ic_home': 'M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z',
    'ic_favorite': 'M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z',
    'ic_favorite_border': 'M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3 4.42,3 2,5.42 2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32C18.6,15.36 22,12.28 22,8.5 22,5.42 19.58,3 16.5,3zM12.1,18.55l-0.1,0.1 -0.1,-0.1C7.14,14.24 4,11.39 4,8.5 4,6.5 5.5,5 7.5,5c1.54,0 3.04,0.99 3.57,2.36h1.87C13.46,5.99 14.96,5 16.5,5c2,0 3.5,1.5 3.5,3.5 0,2.89 -3.14,5.74 -7.9,10.05z',
    'ic_cart': 'M7,18c-1.1,0 -1.99,0.9 -1.99,2S5.9,22 7,22s2,-0.9 2,-2 -0.9,-2 -2,-2zM1,2v2h2l3.6,7.59 -1.35,2.45c-0.16,0.28 -0.25,0.61 -0.25,0.96 0,1.1 0.9,2 2,2h12v-2H7.42c-0.14,0 -0.25,-0.11 -0.25,-0.25l0.03,-0.12 0.9,-1.63h7.45c0.75,0 1.41,-0.41 1.75,-1.03l3.58,-6.49c0.08,-0.14 0.12,-0.31 0.12,-0.48 0,-0.55 -0.45,-1 -1,-1H5.21l-0.94,-2H1zM17,18c-1.1,0 -1.99,0.9 -1.99,2s0.89,2 1.99,2 2,-0.9 2,-2 -0.9,-2 -2,-2z',
    'ic_person': 'M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z',
    'ic_search': 'M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z',
    'ic_add': 'M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z',
    'ic_remove': 'M19,13H5v-2h14v2z',
    'ic_star': 'M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z',
    'ic_arrow_back': 'M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z',
    'ic_camera': 'M12,12m-3.2,0a3.2,3.2 0,1 1,6.4 0a3.2,3.2 0,1 1,-6.4 0M9,2L7.17,4H4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2H9zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z',
    'ic_image': 'M21,19V5c0,-1.1 -0.9,-2 -2,-2H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2zM8.5,13.5l2.5,3.01L14.5,12l4.5,6H5l3.5,-4.5z',
    'ic_logout': 'M17,7l-1.41,1.41L18.17,11H8v2h10.17l-2.58,2.58L17,17l5,-5zM4,5h8V3H4c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h8v-2H4V5z',
    'ic_edit': 'M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z',
    'ic_lock': 'M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1v2z',
    'ic_receipt': 'M18,17H6v-2h12v2zM18,13H6v-2h12v2zM18,9H6V7h12v2zM3,22l1.5,-1.5L6,22l1.5,-1.5L9,22l1.5,-1.5L12,22l1.5,-1.5L15,22l1.5,-1.5L18,22l1.5,-1.5L21,22V2l-1.5,1.5L18,2l-1.5,1.5L15,2l-1.5,1.5L12,2l-1.5,1.5L9,2 7.5,3.5 6,2 4.5,3.5 3,2v20z',
    'ic_dark_mode': 'M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z',
    'ic_email': 'M20,4H4c-1.1,0 -1.99,0.9 -1.99,2L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2zM20,8l-8,5 -8,-5V6l8,5 8,-5v2z',
    'ic_phone': 'M6.62,10.79c1.44,2.83 3.76,5.14 6.59,6.59l2.2,-2.2c0.27,-0.27 0.67,-0.36 1.02,-0.24 1.12,0.37 2.33,0.57 3.57,0.57 0.55,0 1,0.45 1,1V20c0,0.55 -0.45,1 -1,1 -9.39,0 -17,-7.61 -17,-17 0,-0.55 0.45,-1 1,-1h3.5c0.55,0 1,0.45 1,1 0,1.25 0.2,2.45 0.57,3.57 0.11,0.35 0.03,0.74 -0.25,1.02l-2.2,2.2z',
    'ic_location': 'M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7zM12,11.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z',
    'ic_delete': 'M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z',
    'ic_restaurant': 'M11,9H9V2H7v7H5V2H3v7c0,2.12 1.66,3.84 3.75,3.97V22h2.5v-9.03C11.34,12.84 13,11.12 13,9V2h-2v7zM16,6v8h2.5v8H21V2c-2.76,0 -5,2.24 -5,4z',
    'ic_check_circle': 'M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z',
    'ic_chevron_right': 'M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z',
    'ic_time': 'M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z',
    'ic_fire': 'M13.5,0.67s0.74,2.65 0.74,4.8c0,2.06 -1.35,3.73 -3.41,3.73 -2.07,0 -3.63,-1.67 -3.63,-3.73l0.03,-0.36C5.21,7.51 4,10.62 4,14c0,4.42 3.58,8 8,8s8,-3.58 8,-8C20,8.61 17.41,3.8 13.5,0.67zM11.71,19c-1.78,0 -3.22,-1.4 -3.22,-3.14 0,-1.62 1.05,-2.76 2.81,-3.12 1.77,-0.36 3.6,-1.21 4.62,-2.58 0.39,1.29 0.59,2.65 0.59,4.04 0,2.65 -2.15,4.8 -4.8,4.8z',
    'ic_payment': 'M20,4H4c-1.11,0 -1.99,0.89 -1.99,2L2,18c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V6c0,-1.11 -0.89,-2 -2,-2zM20,18H4v-6h16v6zM20,8H4V6h16v2z',
    'ic_note': 'M3,18h12v-2H3v2zM3,6v2h18V6H3zM3,13h18v-2H3v2z',
    'ic_close': 'M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z',
    'ic_shipping': 'M20,8h-3V4H3c-1.1,0 -2,0.9 -2,2v11h2c0,1.66 1.34,3 3,3s3,-1.34 3,-3h6c0,1.66 1.34,3 3,3s3,-1.34 3,-3h2v-5l-3,-4zM6,18.5c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5zM19.5,9.5l1.96,2.5H17V9.5h2.5zM18,18.5c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z',
    'ic_category': 'M12,2l-5.5,9h11zM17.5,13c-2.49,0 -4.5,2.01 -4.5,4.5s2.01,4.5 4.5,4.5 4.5,-2.01 4.5,-4.5 -2.01,-4.5 -4.5,-4.5zM3,21.5h8v-8H3z',
    'ic_money': 'M11.8,10.9c-2.27,-0.59 -3,-1.2 -3,-2.15 0,-1.09 1.01,-1.85 2.7,-1.85 1.78,0 2.44,0.85 2.5,2.1h2.21c-0.07,-1.72 -1.12,-3.3 -3.21,-3.81V3h-3v2.16c-1.94,0.42 -3.5,1.68 -3.5,3.61 0,2.31 1.91,3.46 4.7,4.13 2.5,0.6 3,1.48 3,2.41 0,0.69 -0.49,1.79 -2.7,1.79 -2.060,0 -2.87,-0.92 -2.98,-2.1h-2.2c0.12,2.19 1.76,3.42 3.68,3.83V21h3v-2.15c1.95,-0.37 3.5,-1.5 3.5,-3.55 0,-2.84 -2.43,-3.81 -4.7,-4.4z',
}

VECTOR = '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:tint="?attr/colorControlNormal"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="{path}" />
</vector>
'''

NS = 'xmlns:android="http://schemas.android.com/apk/res/android"'
SHAPES = {
    'bg_circle_primary': ('oval', '<solid android:color="@color/primary" />'),
    'bg_circle_white': ('oval', '<solid android:color="@color/bg_card" />'),
    'bg_circle_light': ('oval', '<solid android:color="@color/primary_light" />'),
    'bg_input_rounded': ('rectangle', '<solid android:color="@color/bg_input" />\n    <corners android:radius="16dp" />'),
    'bg_card_rounded': ('rectangle', '<solid android:color="@color/bg_card" />\n    <corners android:radius="20dp" />'),
    'bg_top_rounded': ('rectangle', '<solid android:color="@color/bg_screen" />\n    <corners android:topLeftRadius="28dp" android:topRightRadius="28dp" />'),
    'bg_badge_primary': ('rectangle', '<solid android:color="@color/primary" />\n    <corners android:radius="12dp" />'),
    'bg_chip_light': ('rectangle', '<solid android:color="@color/primary_light" />\n    <corners android:radius="12dp" />'),
    'bg_stepper': ('rectangle', '<solid android:color="@color/bg_input" />\n    <corners android:radius="22dp" />'),
    'bg_image_placeholder': ('rectangle', '<solid android:color="@color/shimmer_base" />'),
    'bg_shimmer_block': ('rectangle', '<solid android:color="@color/shimmer_base" />\n    <corners android:radius="8dp" />'),
    'bg_sheet_handle': ('rectangle', '<solid android:color="@color/divider" />\n    <corners android:radius="3dp" />'),
    'bg_dot_active': ('rectangle', '<solid android:color="@color/primary" />\n    <corners android:radius="4dp" />\n    <size android:width="20dp" android:height="8dp" />'),
    'bg_dot_inactive': ('rectangle', '<solid android:color="@color/divider" />\n    <corners android:radius="4dp" />\n    <size android:width="8dp" android:height="8dp" />'),
    'bg_banner_gradient': ('rectangle', '<gradient android:angle="0" android:startColor="#E6000000" android:centerColor="#80000000" android:endColor="#00000000" />'),
    'bg_detail_gradient': ('rectangle', '<gradient android:angle="270" android:startColor="#99000000" android:endColor="#00000000" />'),
    'bg_auth_header': ('rectangle', '<gradient android:angle="315" android:startColor="#FF8A50" android:endColor="@color/primary_dark" />\n    <corners android:bottomLeftRadius="40dp" android:bottomRightRadius="40dp" />'),
    'bg_splash_logo': ('oval', '<solid android:color="#FFFFFF" />'),
    'bg_status_chip': ('rectangle', '<solid android:color="#FFFFFF" />\n    <corners android:radius="10dp" />'),
}

ANIMS = {
    'slide_in_right': '<translate android:duration="280" android:fromXDelta="100%" android:toXDelta="0" android:interpolator="@android:anim/decelerate_interpolator" />',
    'slide_out_left': '<translate android:duration="280" android:fromXDelta="0" android:toXDelta="-30%" android:interpolator="@android:anim/decelerate_interpolator" />',
    'slide_in_left': '<translate android:duration="280" android:fromXDelta="-30%" android:toXDelta="0" android:interpolator="@android:anim/decelerate_interpolator" />',
    'slide_out_right': '<translate android:duration="280" android:fromXDelta="0" android:toXDelta="100%" android:interpolator="@android:anim/decelerate_interpolator" />',
    'item_fall_down': '<translate android:duration="400" android:fromYDelta="-12%" android:toYDelta="0" android:interpolator="@android:anim/decelerate_interpolator" />\n    <alpha android:duration="400" android:fromAlpha="0" android:toAlpha="1" />\n    <scale android:duration="400" android:fromXScale="105%" android:fromYScale="105%" android:pivotX="50%" android:pivotY="50%" android:toXScale="100%" android:toYScale="100%" />',
    'fade_in_up': '<translate android:duration="600" android:fromYDelta="40%" android:toYDelta="0" android:interpolator="@android:anim/decelerate_interpolator" />\n    <alpha android:duration="600" android:fromAlpha="0" android:toAlpha="1" />',
}


def write(path, text):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)


for name, path in ICONS.items():
    write(f'{R}/drawable/{name}.xml', VECTOR.format(path=path))

for name, (shape, body) in SHAPES.items():
    write(f'{R}/drawable/{name}.xml',
          f'<?xml version="1.0" encoding="utf-8"?>\n<shape {NS}\n    android:shape="{shape}">\n    {body}\n</shape>\n')

for name, body in ANIMS.items():
    write(f'{R}/anim/{name}.xml', f'<?xml version="1.0" encoding="utf-8"?>\n<set {NS}>\n    {body}\n</set>\n')

write(f'{R}/anim/layout_fall_down.xml',
      f'<?xml version="1.0" encoding="utf-8"?>\n<layoutAnimation {NS}\n    android:animation="@anim/item_fall_down"\n    android:animationOrder="normal"\n    android:delay="15%" />\n')
write(f'{R}/anim/grid_fall_down.xml',
      f'<?xml version="1.0" encoding="utf-8"?>\n<gridLayoutAnimation {NS}\n    android:animation="@anim/item_fall_down"\n    android:animationOrder="normal"\n    android:columnDelay="15%"\n    android:direction="top_to_bottom"\n    android:rowDelay="15%" />\n')

print(f'Generated {len(ICONS)} icons, {len(SHAPES)} shapes, {len(ANIMS) + 2} animations')
