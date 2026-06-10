import os
import re

BASE = r'd:\Pro\星球出机小程序 - 副本\frontend\src\pages'

garble_map = {
    # === workorder/process.vue ===
    '浼樺厛绾': '优先级',
    '鐘舵€': '状态',
    '璇锋弿杩扮淮淇\ue1bd柟妗堝拰缁撴灉': '请描述维修方案和结果',
    '璇峰～鍐欑淮淇\ue1bd柟妗': '请填写维修方案',
    '鍘熷洜': '原因',
    
    # === repair/mine.vue ===
    '鎴戠殑鎶ヤ慨': '我的报修',
    '鏆傛棤鎶ヤ慨璁板綍': '暂无报修记录',
    '鍘绘姤淇': '去报修',
    '鏌ョ湅璇︽儏': '查看详情',
    '鎼滅储鎶ヤ慨璁板綍': '搜索报修记录',
    '鍏ㄩ儴': '全部',
    '寰呭鐞': '待处理',
    '宸叉淳鍗': '已派单',
    '澶勭悊涓': '处理中',
    '寰呮帴鍗': '待接单',
    '宸插畬鎴': '已完成',
    '宸查┏鍥': '已驳回',
    '宸插叧闂': '已关闭',
    
    # === repair/progress.vue ===
    '鎶ヤ慨淇℃伅': '报修信息',
    '鏁呴殰绫诲瀷': '故障类型',
    '鎻忚堪': '描述',
    '鎻愪氦鏃堕棿': '提交时间',
    '缁翠慨鍛': '维修员',
    '鐜板満鐓х墖': '现场照片',
    
    # === workorder/hall.vue ===
    '鏆傛棤宸ュ崟': '暂无工单',
}

def fix_broken_tags(content):
    """Fix broken tags where a stray quote appears inside a tag context"""
    fix_tag_pairs = [
        ('优先级"/text>', '优先级</text>'),
        ('状态"/text>', '状态</text>'),
        ('维修员"/text>', '维修员</text>'),
    ]
    for old, new in fix_tag_pairs:
        content = content.replace(old, new)
    return content

def cleanup_pua_garbage(content):
    """Remove PUA (Private Use Area) characters that are clearly garbage from mojibake"""
    # Characters like \ue50d, \ue1bd etc appear as leftover garbage
    content = content.replace('\ue50d', '')
    content = content.replace('\ue1bd', '')
    content = content.replace('\uee86', '')
    content = content.replace('\uee94', '')
    return content

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        raw_content = f.read()
    
    content = raw_content
    
    for old, new in garble_map.items():
        if old in content:
            content = content.replace(old, new)
            print(f'  [{os.path.basename(filepath)}] Replaced garbled -> {new}')
    
    content = fix_broken_tags(content)
    content = cleanup_pua_garbage(content)
    
    if content != raw_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'  -> Saved: {filepath}\n')
    else:
        print(f'  -> No changes needed: {filepath}\n')

files = [
    os.path.join(BASE, 'workorder', 'process.vue'),
    os.path.join(BASE, 'repair', 'mine.vue'),
    os.path.join(BASE, 'repair', 'progress.vue'),
    os.path.join(BASE, 'workorder', 'hall.vue'),
]

for fp in files:
    fix_file(fp)

print('Done!')
