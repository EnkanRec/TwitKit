## 渲染设置
VIEWPORT_WIDTH = 480
DEFAULT_PPI = 144
ZH_FONT = 'Noto Sans CJK SC, Segoe UI Symbol'
JA_FONT = 'Noto Sans CJK JP, Segoe UI Symbol'
JAVASCRIPT_DELAY = 20000

# 监听设置
API_SERVER_HOST = '127.0.0.1'
API_SERVER_PORT = 8221

## 二维码设置
## （以下定位数值对应的均为相对于浏览器渲染时的像素值）

# 二维码宽高
TID_CODE_WIDTH = 10
TID_CODE_HEIGHT = 10

# 二维码位置（设为负数从右/下起）
TID_CODE_POS_X = -2
TID_CODE_POS_Y = -2

# 二维码key（多个实例产生的图在同一账号发布时避免冲突用）
TID_CODE_KEY = 0

## URL设置
EXT_STATIC_BASE_URL = 'https://example.com/timg'
INT_BASE_URL = f'http://127.0.0.1:{API_SERVER_PORT}'

## Fridge API URL
FRIDGE_API_BASE = 'http://127.0.0.1:8220/api'

## Maid API URL
MAID_API_BASE = 'http://127.0.0.1:8222/api'

## 日志设置
LOG_DEBUG = False
LOG_FILE = None
