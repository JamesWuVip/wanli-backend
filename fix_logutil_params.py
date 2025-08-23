#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复LogUtil方法调用中的null参数问题
"""

import os
import re
import glob

def fix_logutil_calls(file_path):
    """修复单个文件中的LogUtil调用"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # 修复logBusiness调用 - 需要Map<String, Object>参数
    # LogUtil.logBusiness("operation", null) -> LogUtil.logBusiness("operation", new HashMap<>())
    content = re.sub(
        r'LogUtil\.logBusiness\(([^,]+),\s*null\)',
        r'LogUtil.logBusiness(\1, new HashMap<>())',
        content
    )
    
    # 修复logError调用 - 有5个参数的版本
    # LogUtil.logError("op", null, "code", "msg", ex) -> LogUtil.logError("op", "", "code", "msg", ex)
    content = re.sub(
        r'LogUtil\.logError\(([^,]+),\s*null,\s*([^,]+),\s*([^,]+),\s*([^)]+)\)',
        r'LogUtil.logError(\1, "", \2, \3, \4)',
        content
    )
    
    # 修复logWarn调用 - 3个参数版本，第二个参数为null
    # LogUtil.logWarn("op", null, "msg") -> LogUtil.logWarn("op", "", "msg")
    content = re.sub(
        r'LogUtil\.logWarn\(([^,]+),\s*null,\s*([^)]+)\)',
        r'LogUtil.logWarn(\1, "", \2)',
        content
    )
    
    # 修复logInfo调用 - 3个参数版本，第二个参数为null
    # LogUtil.logInfo("op", null, "msg") -> LogUtil.logInfo("op", "", "msg")
    content = re.sub(
        r'LogUtil\.logInfo\(([^,]+),\s*null,\s*([^)]+)\)',
        r'LogUtil.logInfo(\1, "", \2)',
        content
    )
    
    # 修复logDebug调用 - 3个参数版本，第二个参数为null
    # LogUtil.logDebug("op", null, "msg") -> LogUtil.logDebug("op", "", "msg")
    content = re.sub(
        r'LogUtil\.logDebug\(([^,]+),\s*null,\s*([^)]+)\)',
        r'LogUtil.logDebug(\1, "", \2)',
        content
    )
    
    # 修复logBusinessOperation调用 - 3个参数版本，第二个参数为null
    # LogUtil.logBusinessOperation("op", null, "details") -> LogUtil.logBusinessOperation("op", "", "details")
    content = re.sub(
        r'LogUtil\.logBusinessOperation\(([^,]+),\s*null,\s*([^)]+)\)',
        r'LogUtil.logBusinessOperation(\1, "", \2)',
        content
    )
    
    # 修复logPerformance调用 - 3个参数版本，第三个参数为Map
    # LogUtil.logPerformance("op", duration, null) -> LogUtil.logPerformance("op", duration, new HashMap<>())
    content = re.sub(
        r'LogUtil\.logPerformance\(([^,]+),\s*([^,]+),\s*null\)',
        r'LogUtil.logPerformance(\1, \2, new HashMap<>())',
        content
    )
    
    # 修复logDatabaseOperation调用 - 4个参数版本，第三个参数为null
    # LogUtil.logDatabaseOperation("op", "table", null, duration) -> LogUtil.logDatabaseOperation("op", "table", "", duration)
    content = re.sub(
        r'LogUtil\.logDatabaseOperation\(([^,]+),\s*([^,]+),\s*null,\s*([^)]+)\)',
        r'LogUtil.logDatabaseOperation(\1, \2, "", \3)',
        content
    )
    
    # 修复logApiCall调用 - 5个参数版本，第三个参数为null
    # LogUtil.logApiCall("method", "path", null, statusCode, duration) -> LogUtil.logApiCall("method", "path", "", statusCode, duration)
    content = re.sub(
        r'LogUtil\.logApiCall\(([^,]+),\s*([^,]+),\s*null,\s*([^,]+),\s*([^)]+)\)',
        r'LogUtil.logApiCall(\1, \2, "", \3, \4)',
        content
    )
    
    # 如果内容有变化，写回文件
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    """主函数"""
    # 查找所有Java文件
    java_files = glob.glob('/Users/wujames/Documents/wanli-backend/src/**/*.java', recursive=True)
    
    fixed_files = []
    
    for file_path in java_files:
        try:
            if fix_logutil_calls(file_path):
                fixed_files.append(file_path)
                print(f"已修复: {file_path}")
        except Exception as e:
            print(f"修复失败 {file_path}: {e}")
    
    print(f"\n总共修复了 {len(fixed_files)} 个文件")
    for file_path in fixed_files:
        print(f"  - {file_path}")

if __name__ == '__main__':
    main()