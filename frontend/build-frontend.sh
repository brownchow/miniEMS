#!/bin/bash
# 前端构建脚本
# 用法: ./build-frontend.sh

set -e

echo "==> 构建前端..."

if [ ! -d "node_modules" ]; then
    echo "==> 安装依赖..."
    npm install
    cd ..
fi

echo "==> 构建生产版本..."
npm run build
cd ..

echo "==> 前端构建完成！"
