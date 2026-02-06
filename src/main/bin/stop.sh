#!/bin/bash

# MiniEMS 停止脚本

# 查找并杀掉 java 进程
PIDS=$(pgrep -f "ems.jar")

if [ -n "$PIDS" ]; then
    echo "正在停止 MiniEMS (PID: $PIDS)..."
    kill $PIDS 2>/dev/null
    echo "停止完成"
else
    echo "未找到运行中的进程"
fi
