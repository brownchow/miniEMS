#!/bin/bash

# Grafana Dashboard 备份脚本
# 用法: ./backup-grafana.sh

BACKUP_DIR="./grafana/provisioning/backups"
DATE=$(date +%Y%m%d-%H%M%S)

mkdir -p $BACKUP_DIR

# 导出所有仪表盘
echo "导出 Grafana 仪表盘..."
DASHBOARDS=$(curl -s -u admin:admin "http://localhost:3000/api/search?type=dash-db" 2>/dev/null)

if [ -z "$DASHBOARDS" ] || [ "$DASHBOARDS" = "[]" ]; then
    echo "没有找到仪表盘"
    exit 0
fi

echo "$DASHBOARDS" | python3 -c "
import sys, json, os

dashboards = json.load(sys.stdin)
backup_dir = '$BACKUP_DIR'
timestamp = '$DATE'

os.makedirs(backup_dir, exist_ok=True)

for d in dashboards:
    uid = d.get('uid')
    title = d.get('title', 'untitled')
    safe_title = ''.join(c if c.isalnum() or c in (' ', '-', '_') else '_' for c in title)
    filename = f'{backup_dir}/{safe_title}-{timestamp}.json'

    import urllib.request
    url = f'http://localhost:3000/api/dashboards/uid/{uid}'
    req = urllib.request.Request(url)
    req.add_header('Authorization', 'Basic YWRtaW46YWRtaW4=')

    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        # 只保存 dashboard 部分
        dashboard = data.get('dashboard', {})
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(dashboard, f, ensure_ascii=False, indent=2)
        print(f'已保存: {filename}')
" 2>/dev/null

echo "备份完成!"
