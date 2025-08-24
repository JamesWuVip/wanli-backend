#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复LogUtil参数传递错误的脚本

问题描述：
在AuthService.java中，LogUtil.logUserAction方法的调用存在参数传递错误：
- 第二个参数应该是userId，但传递的是email
- 第三个参数应该是action，但传递的是userId

修复方案：
1. 扫描所有Java文件，查找LogUtil.logUserAction的调用
2. 分析参数传递是否正确
3. 自动修复参数顺序错误

作者：系统维护脚本
创建时间：2025年1月
"""

import os
import re
import glob
import shutil
from datetime import datetime


class LogUtilParamsFixer:
    """LogUtil参数修复器"""
    
    def __init__(self):
        self.fixed_files = []
        self.error_files = []
        self.backup_dir = f"backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        
    def create_backup(self, file_path):
        """创建文件备份"""
        if not os.path.exists(self.backup_dir):
            os.makedirs(self.backup_dir)
        
        backup_path = os.path.join(self.backup_dir, os.path.basename(file_path))
        shutil.copy2(file_path, backup_path)
        print(f"已备份文件: {file_path} -> {backup_path}")
    
    def analyze_logutil_call(self, line):
        """分析LogUtil调用是否有参数错误"""
        # 匹配LogUtil.logUserAction调用
        pattern = r'LogUtil\.logUserAction\s*\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^)]+)\s*\)'
        match = re.search(pattern, line)
        
        if not match:
            return None, None
        
        param1 = match.group(1).strip()
        param2 = match.group(2).strip()
        param3 = match.group(3).strip()
        
        # 检查是否存在参数错误
        # 如果第二个参数看起来像email，第三个参数看起来像userId，则可能有错误
        if ('email' in param2.lower() or '@' in param2) and ('user' in param3.lower() and 'id' in param3.lower()):
            # 修复：交换第二和第三个参数
            fixed_call = f"LogUtil.logUserAction({param1}, {param3}, {param2})"
            return match.group(0), fixed_call
        
        return None, None
    
    def fix_file(self, file_path):
        """修复单个文件"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            lines = content.split('\n')
            modified = False
            fixed_lines = []
            
            for line_num, line in enumerate(lines, 1):
                if 'LogUtil.logUserAction' in line:
                    original_call, fixed_call = self.analyze_logutil_call(line)
                    if original_call and fixed_call:
                        print(f"文件 {file_path} 第 {line_num} 行发现参数错误:")
                        print(f"  原始: {original_call}")
                        print(f"  修复: {fixed_call}")
                        
                        # 替换错误的调用
                        fixed_line = line.replace(original_call, fixed_call)
                        fixed_lines.append(fixed_line)
                        modified = True
                    else:
                        fixed_lines.append(line)
                else:
                    fixed_lines.append(line)
            
            if modified:
                # 创建备份
                self.create_backup(file_path)
                
                # 写入修复后的内容
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write('\n'.join(fixed_lines))
                
                self.fixed_files.append(file_path)
                print(f"✅ 已修复文件: {file_path}")
            
        except Exception as e:
            print(f"❌ 处理文件 {file_path} 时出错: {str(e)}")
            self.error_files.append((file_path, str(e)))
    
    def scan_and_fix(self, java_files):
        """扫描并修复所有Java文件"""
        print(f"开始扫描 {len(java_files)} 个Java文件...")
        
        for file_path in java_files:
            if os.path.exists(file_path):
                self.fix_file(file_path)
        
        # 输出修复结果
        print("\n" + "="*50)
        print("修复结果汇总:")
        print(f"✅ 成功修复文件数: {len(self.fixed_files)}")
        print(f"❌ 处理失败文件数: {len(self.error_files)}")
        
        if self.fixed_files:
            print("\n修复的文件列表:")
            for file_path in self.fixed_files:
                print(f"  - {file_path}")
        
        if self.error_files:
            print("\n处理失败的文件:")
            for file_path, error in self.error_files:
                print(f"  - {file_path}: {error}")
        
        if self.fixed_files:
            print(f"\n备份目录: {self.backup_dir}")
            print("如果修复有问题，可以从备份目录恢复原文件。")


def main():
    """主函数"""
    # 查找所有Java文件
    java_files = glob.glob('/Users/JamesWuVip/Documents/wanli-backend/src/**/*.java', recursive=True)
    
    fixed_files = []
    
    if not java_files:
        print("未找到Java文件")
        return
    
    print(f"找到 {len(java_files)} 个Java文件")
    
    # 创建修复器实例
    fixer = LogUtilParamsFixer()
    
    # 执行修复
    fixer.scan_and_fix(java_files)


if __name__ == "__main__":
    main()