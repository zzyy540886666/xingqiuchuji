import sys, os, re

base = r'd:\Pro\星球出机小程序 - 副本\frontend\src\pages'

files_to_fix = {
    'admin/dashboard.vue': {
        '鎬诲伐鍗?/text>': '总工单</text>',
        '寰呭\u5904鐞?/text>': '待处理</text>',
        '瀹屾垚鐜?/text>': '完成率</text>',
        '閫炬湡宸℃\u68c0': '逾期巡检',
        '宸ュ崟绠＄悊': '工单管理',
        '宸℃\u68c0浠诲姟': '巡检任务',
        '娑堟伅鎻愰啋': '消息提醒',
    },
    'distribution/commission.vue': {
        '浣ｉ噾鏄庣粏': '佣金明细',
        '鏆傛棤浣ｉ噾璁板綍': '暂无佣金记录',
        '璁㈠崟': '订单',
        '淇濇姢鏈?': '保护期',
        '鍙\u7ed3绠?': '可结算',
        '宸茬粨绠?': '已结算',
    },
    'distribution/team.vue': {
        '鎴戠殑鍥㈤槦': '我的团队',
        '涓€绾ф垚鍛?/text>': '一级成员</text>',
        '浜岀骇鎴愬憳': '二级成员',
        '鏆傛棤鍥㈤槦鎴愬憳': '暂无团队成员',
    },
    'inspection/task-list.vue': {
        '鏆傛棤宸℃\u68c0浠诲姟': '暂无巡检任务',
        '寰呭贰妫€': '待巡检',
        '宸℃\u68c0涓?': '巡检中',
        '宸插畬鎴?': '已完成',
        '閫炬湡': '逾期',
        '鎴\u622a姝\u22bc歿': '截止：',
        '寮€濮嬪贰妫€': '开始巡检',
        '宸插紑濮嬪贰妫€': '已开始巡检',
        '鎿嶄綔澶辫触': '操作失败',
    },
    'notifications/index.vue': {
        '娑堟伅涓\u5fc3': '消息中心',
        '鍏ㄩ儴宸茶\uafbb': '全部已读',
        '绯荤粺閫氱煡': '系统通知',
        '璁㈠崟娑堟伅': '订单消息',
        '浜掑姩娑堟伅': '互动消息',
        '鏆傛棤娑堟伅': '暂无消息',
        '鍔犺浇鏇村\u591a': '加载更多',
        '鏌ョ湅璇︽儏': '查看详情',
        '鍏抽棴': '关闭',
        '鍏ㄩ儴': '全部',
        '娌℃湁鏈\uaa0e璇绘秷鎭\u00af': '没有未读消息',
        '宸叉爣璁?': '已标记',
        '鏉℃秷鎭\u00af涓哄凡璇\u00af': '条消息为已读',
        '鎿嶄綔澶辫触': '操作失败',
    },
    'im/conversations.vue': {
        '鏆傛棤娑堟伅': '暂无消息',
    },
}

for rel_path, replacements in files_to_fix.items():
    full_path = os.path.join(base, rel_path)
    if not os.path.exists(full_path):
        print(f'SKIP: {rel_path} not found')
        continue
    
    with open(full_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    changed = False
    for old, new in replacements.items():
        if old in content:
            content = content.replace(old, new)
            print(f'[{rel_path}] Replaced: {repr(old[:20])} -> {new}')
            changed = True
        else:
            # Try to find partial match
            if '?' in old:
                parts = old.split('?')
                found_any = False
                for i, part in enumerate(parts):
                    if part and part in content and '?' in content:
                        # Try broader search
                        pattern = part + '?'
                        if pattern in content:
                            found_any = True
                            break
                if not found_any:
                    print(f'[{rel_path}] NOT FOUND: {repr(old[:30])}')
            else:
                print(f'[{rel_path}] NOT FOUND: {repr(old[:30])}')
    
    if changed:
        # Also fix any remaining broken ?/text> and ?/view> tags
        if '?/text>' in content:
            content = content.replace('?/text>', '</text>')
            print(f'[{rel_path}] Fixed generic ?/text> tags')
        if '?/view>' in content:
            content = content.replace('?/view>', '</view>')
            print(f'[{rel_path}] Fixed generic ?/view> tags')
        
        with open(full_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'[{rel_path}] Saved')
    else:
        print(f'[{rel_path}] No changes')

print('\nAll done!')
