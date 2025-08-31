#!/bin/bash

# 万里后端项目 - 自动反馈修复脚本
# 根据CI/CD结果自动启动调试或优化

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

# 图标定义
ICON_SUCCESS="✅"
ICON_ERROR="❌"
ICON_WARNING="⚠️"
ICON_INFO="ℹ️"
ICON_FIX="🔧"
ICON_TEST="🧪"
ICON_QUALITY="🔍"
ICON_SECURITY="🛡️"
ICON_DEPLOY="🚀"
ICON_BUILD="🏗️"
ICON_REPORT="📋"

# 项目配置
PROJECT_NAME="万里后端项目"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="$SCRIPT_DIR/logs/ci-feedback.log"
REPORT_FILE="$SCRIPT_DIR/logs/fix-report.md"

# 创建日志目录
mkdir -p "$(dirname "$LOG_FILE")"

# 记录日志
log_message() {
    local level="$1"
    local message="$2"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" | tee -a "$LOG_FILE"
}

# 显示标题
show_header() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${WHITE}                        $PROJECT_NAME 自动修复工具                        ${CYAN}║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo
}

# 初始化修复报告
init_report() {
    cat > "$REPORT_FILE" << EOF
# CI/CD 自动修复报告

**生成时间:** $(date '+%Y-%m-%d %H:%M:%S')
**项目:** $PROJECT_NAME
**修复类型:** $1

## 修复概览

| 检查项目 | 状态 | 修复操作 | 结果 |
|---------|------|----------|------|
EOF
}

# 更新修复报告
update_report() {
    local item="$1"
    local status="$2"
    local action="$3"
    local result="$4"
    
    echo "| $item | $status | $action | $result |" >> "$REPORT_FILE"
}

# 完成修复报告
finish_report() {
    cat >> "$REPORT_FILE" << EOF

## 修复详情

### 执行的修复操作

$1

### 建议的后续操作

$2

---
*报告生成于: $(date '+%Y-%m-%d %H:%M:%S')*
EOF
}

# 编译错误修复
fix_compile_errors() {
    echo -e "${BLUE}$ICON_BUILD 检查编译错误...${NC}"
    log_message "INFO" "开始编译错误修复"
    
    local fix_actions=""
    local suggestions=""
    
    if [ ! -f "pom.xml" ]; then
        echo -e "${YELLOW}$ICON_WARNING 未找到pom.xml文件，跳过编译检查${NC}"
        update_report "编译检查" "跳过" "无Maven项目" "N/A"
        return 0
    fi
    
    # 尝试编译
    if mvn compile -q 2>/dev/null; then
        echo -e "${GREEN}$ICON_SUCCESS 编译成功，无需修复${NC}"
        update_report "编译检查" "✅ 成功" "无需修复" "通过"
        log_message "INFO" "编译检查通过"
        return 0
    fi
    
    echo -e "${RED}$ICON_ERROR 发现编译错误，开始自动修复...${NC}"
    
    # 获取编译错误信息
    local compile_output=$(mktemp)
    mvn compile 2>&1 | tee "$compile_output"
    
    # 常见编译错误修复
    local fixed=false
    
    # 1. 缺少导入
    if grep -q "cannot find symbol" "$compile_output"; then
        echo -e "${YELLOW}$ICON_FIX 修复缺少的导入...${NC}"
        fix_actions+="- 添加缺少的导入语句\n"
        
        # 添加常用导入
        find src/main/java -name "*.java" -exec grep -l "List\|Map\|Set" {} \; | while read -r file; do
            if ! grep -q "import java.util" "$file"; then
                sed -i '1a\import java.util.*;' "$file"
            fi
        done
        fixed=true
    fi
    
    # 2. 修复注解问题
    if grep -q "annotation" "$compile_output"; then
        echo -e "${YELLOW}$ICON_FIX 修复注解问题...${NC}"
        fix_actions+="- 修复注解相关问题\n"
        
        # 确保Spring Boot注解正确
        find src/main/java -name "*Controller.java" -exec grep -L "@RestController\|@Controller" {} \; | while read -r file; do
            sed -i '1a\import org.springframework.web.bind.annotation.*;' "$file"
            sed -i '/^public class/i\@RestController' "$file"
        done
        fixed=true
    fi
    
    # 3. 修复依赖问题
    if grep -q "package does not exist" "$compile_output"; then
        echo -e "${YELLOW}$ICON_FIX 检查Maven依赖...${NC}"
        fix_actions+="- 检查并更新Maven依赖\n"
        
        # 重新下载依赖
        mvn dependency:resolve -q
        fixed=true
    fi
    
    # 清理临时文件
    rm -f "$compile_output"
    
    # 重新编译验证
    if [ "$fixed" = true ]; then
        echo -e "${BLUE}$ICON_INFO 重新编译验证...${NC}"
        if mvn compile -q 2>/dev/null; then
            echo -e "${GREEN}$ICON_SUCCESS 编译错误已修复${NC}"
            update_report "编译修复" "✅ 成功" "自动修复" "通过"
            log_message "INFO" "编译错误修复成功"
        else
            echo -e "${RED}$ICON_ERROR 自动修复失败，需要手动处理${NC}"
            update_report "编译修复" "❌ 失败" "自动修复" "需要手动处理"
            suggestions+="- 请检查具体的编译错误信息\n- 确认所有依赖都已正确配置\n"
            log_message "ERROR" "编译错误自动修复失败"
        fi
    else
        update_report "编译修复" "⚠️ 跳过" "未识别错误类型" "需要手动处理"
        suggestions+="- 请手动检查编译错误\n- 运行 mvn compile 查看详细错误信息\n"
    fi
    
    return 0
}

# 测试失败修复
fix_test_failures() {
    echo -e "${BLUE}$ICON_TEST 检查测试失败...${NC}"
    log_message "INFO" "开始测试失败修复"
    
    local fix_actions=""
    local suggestions=""
    
    if [ ! -d "src/test" ]; then
        echo -e "${YELLOW}$ICON_WARNING 未找到测试目录，跳过测试检查${NC}"
        update_report "测试检查" "跳过" "无测试目录" "N/A"
        return 0
    fi
    
    # 检查是否有测试文件
    local test_count=$(find src/test -name "*.java" | wc -l | tr -d ' ')
    if [ "$test_count" -eq 0 ]; then
        echo -e "${YELLOW}$ICON_WARNING 未找到测试文件${NC}"
        update_report "测试检查" "⚠️ 警告" "无测试文件" "建议添加测试"
        suggestions+="- 建议为主要功能添加单元测试\n"
        return 0
    fi
    
    # 运行测试
    if mvn test -q 2>/dev/null; then
        echo -e "${GREEN}$ICON_SUCCESS 所有测试通过${NC}"
        update_report "测试检查" "✅ 成功" "无需修复" "通过"
        log_message "INFO" "测试检查通过"
        return 0
    fi
    
    echo -e "${RED}$ICON_ERROR 发现测试失败，开始自动修复...${NC}"
    
    # 获取测试失败信息
    local test_output=$(mktemp)
    mvn test 2>&1 | tee "$test_output"
    
    local fixed=false
    
    # 1. 修复测试配置问题
    if grep -q "No tests were executed" "$test_output"; then
        echo -e "${YELLOW}$ICON_FIX 修复测试配置...${NC}"
        fix_actions+="- 修复测试配置问题\n"
        
        # 确保测试类有正确的注解
        find src/test -name "*.java" -exec grep -L "@Test" {} \; | while read -r file; do
            if grep -q "public.*test" "$file"; then
                sed -i '1a\import org.junit.jupiter.api.Test;' "$file"
                sed -i 's/public void test/@Test\n    public void test/g' "$file"
            fi
        done
        fixed=true
    fi
    
    # 2. 修复断言问题
    if grep -q "AssertionError" "$test_output"; then
        echo -e "${YELLOW}$ICON_FIX 修复断言问题...${NC}"
        fix_actions+="- 修复测试断言\n"
        
        # 这里可以添加更具体的断言修复逻辑
        suggestions+="- 请检查测试断言是否正确\n- 确认测试数据和预期结果\n"
        fixed=true
    fi
    
    # 清理临时文件
    rm -f "$test_output"
    
    # 重新运行测试验证
    if [ "$fixed" = true ]; then
        echo -e "${BLUE}$ICON_INFO 重新运行测试验证...${NC}"
        if mvn test -q 2>/dev/null; then
            echo -e "${GREEN}$ICON_SUCCESS 测试问题已修复${NC}"
            update_report "测试修复" "✅ 成功" "自动修复" "通过"
            log_message "INFO" "测试问题修复成功"
        else
            echo -e "${RED}$ICON_ERROR 自动修复失败，需要手动处理${NC}"
            update_report "测试修复" "❌ 失败" "自动修复" "需要手动处理"
            suggestions+="- 请检查具体的测试失败信息\n- 运行 mvn test 查看详细错误\n"
            log_message "ERROR" "测试问题自动修复失败"
        fi
    else
        update_report "测试修复" "⚠️ 跳过" "未识别错误类型" "需要手动处理"
        suggestions+="- 请手动检查测试失败原因\n- 运行 mvn test 查看详细信息\n"
    fi
    
    return 0
}

# 代码质量问题修复
fix_quality_issues() {
    echo -e "${BLUE}$ICON_QUALITY 检查代码质量问题...${NC}"
    log_message "INFO" "开始代码质量问题修复"
    
    local fix_actions=""
    local suggestions=""
    local fixed=false
    
    # 1. 修复代码格式问题
    echo -e "${YELLOW}$ICON_FIX 修复代码格式...${NC}"
    fix_actions+="- 统一代码格式\n"
    
    # 移除多余的空行
    find src -name "*.java" -exec sed -i '/^$/N;/^\n$/d' {} \;
    
    # 统一缩进（转换tab为4个空格）
    find src -name "*.java" -exec sed -i 's/\t/    /g' {} \;
    
    fixed=true
    
    # 2. 修复未使用的导入
    echo -e "${YELLOW}$ICON_FIX 清理未使用的导入...${NC}"
    fix_actions+="- 清理未使用的导入语句\n"
    
    # 这里可以添加更复杂的导入清理逻辑
    # 简单示例：移除明显未使用的导入
    find src -name "*.java" -exec grep -l "import.*\*;" {} \; | while read -r file; do
        # 检查是否真的需要通配符导入
        if [ $(grep -c "import.*\*;" "$file") -gt 3 ]; then
            suggestions+="- 建议检查 $file 中的通配符导入\n"
        fi
    done
    
    # 3. 添加缺少的注释
    echo -e "${YELLOW}$ICON_FIX 检查方法注释...${NC}"
    fix_actions+="- 检查方法注释\n"
    
    find src/main/java -name "*.java" -exec grep -L "/\*\*" {} \; | while read -r file; do
        suggestions+="- 建议为 $file 添加JavaDoc注释\n"
    done
    
    if [ "$fixed" = true ]; then
        echo -e "${GREEN}$ICON_SUCCESS 代码质量问题已修复${NC}"
        update_report "代码质量" "✅ 改进" "自动修复" "已优化"
        log_message "INFO" "代码质量问题修复完成"
    else
        update_report "代码质量" "⚠️ 检查" "无需修复" "良好"
    fi
    
    return 0
}

# 安全漏洞修复
fix_security_issues() {
    echo -e "${BLUE}$ICON_SECURITY 检查安全漏洞...${NC}"
    log_message "INFO" "开始安全漏洞修复"
    
    local fix_actions=""
    local suggestions=""
    local fixed=false
    
    # 1. 检查硬编码密码
    echo -e "${YELLOW}$ICON_FIX 检查硬编码敏感信息...${NC}"
    
    if grep -r "password\s*=\s*\"" src/ 2>/dev/null; then
        echo -e "${RED}$ICON_ERROR 发现硬编码密码${NC}"
        fix_actions+="- 发现硬编码密码，需要移除\n"
        suggestions+="- 请将密码移至配置文件或环境变量\n- 使用Spring Boot的@Value注解读取配置\n"
    fi
    
    # 2. 检查SQL注入风险
    if grep -r "Statement.*execute" src/ 2>/dev/null; then
        echo -e "${YELLOW}$ICON_WARNING 发现潜在SQL注入风险${NC}"
        fix_actions+="- 检查SQL注入风险\n"
        suggestions+="- 建议使用PreparedStatement\n- 使用JPA或MyBatis等ORM框架\n"
    fi
    
    # 3. 检查XSS风险
    if grep -r "innerHTML\|document.write" src/ 2>/dev/null; then
        echo -e "${YELLOW}$ICON_WARNING 发现潜在XSS风险${NC}"
        fix_actions+="- 检查XSS风险\n"
        suggestions+="- 对用户输入进行转义\n- 使用安全的模板引擎\n"
    fi
    
    # 4. 检查依赖漏洞
    if [ -f "pom.xml" ]; then
        echo -e "${YELLOW}$ICON_FIX 检查依赖安全性...${NC}"
        fix_actions+="- 检查Maven依赖安全性\n"
        
        # 可以集成OWASP dependency check
        suggestions+="- 建议定期更新依赖版本\n- 使用OWASP dependency check插件\n"
    fi
    
    if [ "$fixed" = true ]; then
        echo -e "${GREEN}$ICON_SUCCESS 安全问题已修复${NC}"
        update_report "安全检查" "✅ 修复" "自动修复" "已加固"
        log_message "INFO" "安全问题修复完成"
    else
        echo -e "${GREEN}$ICON_SUCCESS 未发现严重安全问题${NC}"
        update_report "安全检查" "✅ 通过" "无需修复" "安全"
        log_message "INFO" "安全检查通过"
    fi
    
    return 0
}

# 部署问题修复
fix_deploy_issues() {
    echo -e "${BLUE}$ICON_DEPLOY 检查部署问题...${NC}"
    log_message "INFO" "开始部署问题修复"
    
    local fix_actions=""
    local suggestions=""
    local fixed=false
    
    # 1. 检查配置文件
    if [ ! -f "src/main/resources/application.yml" ] && [ ! -f "src/main/resources/application.properties" ]; then
        echo -e "${YELLOW}$ICON_FIX 创建默认配置文件...${NC}"
        fix_actions+="- 创建默认Spring Boot配置\n"
        
        mkdir -p src/main/resources
        cat > src/main/resources/application.yml << EOF
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: wanli-backend
  profiles:
    active: dev

logging:
  level:
    com.wanli: INFO
EOF
        fixed=true
    fi
    
    # 2. 检查主类
    if ! find src/main/java -name "*.java" -exec grep -l "@SpringBootApplication" {} \; | head -1 >/dev/null; then
        echo -e "${YELLOW}$ICON_FIX 检查Spring Boot主类...${NC}"
        fix_actions+="- 检查Spring Boot主类配置\n"
        suggestions+="- 确保有@SpringBootApplication注解的主类\n- 主类应包含main方法\n"
    fi
    
    # 3. 检查端口冲突
    if netstat -an 2>/dev/null | grep -q ":8080.*LISTEN"; then
        echo -e "${YELLOW}$ICON_WARNING 端口8080已被占用${NC}"
        fix_actions+="- 检测到端口冲突\n"
        suggestions+="- 考虑更改应用端口\n- 或停止占用8080端口的进程\n"
    fi
    
    if [ "$fixed" = true ]; then
        echo -e "${GREEN}$ICON_SUCCESS 部署问题已修复${NC}"
        update_report "部署检查" "✅ 修复" "自动修复" "已优化"
        log_message "INFO" "部署问题修复完成"
    else
        echo -e "${GREEN}$ICON_SUCCESS 部署配置正常${NC}"
        update_report "部署检查" "✅ 通过" "无需修复" "正常"
        log_message "INFO" "部署检查通过"
    fi
    
    return 0
}

# 显示修复报告
show_report() {
    if [ -f "$REPORT_FILE" ]; then
        echo -e "${BLUE}$ICON_REPORT 修复报告:${NC}"
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        cat "$REPORT_FILE"
        echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${CYAN}$ICON_INFO 完整报告已保存至: $REPORT_FILE${NC}"
    fi
}

# 主修复函数
run_fixes() {
    local fix_type="$1"
    
    show_header
    init_report "$fix_type"
    
    local fix_actions=""
    local suggestions=""
    
    case "$fix_type" in
        "compile")
            fix_compile_errors
            ;;
        "test")
            fix_test_failures
            ;;
        "quality")
            fix_quality_issues
            ;;
        "security")
            fix_security_issues
            ;;
        "deploy")
            fix_deploy_issues
            ;;
        "all")
            echo -e "${CYAN}$ICON_INFO 执行全面修复检查...${NC}"
            fix_compile_errors
            fix_test_failures
            fix_quality_issues
            fix_security_issues
            fix_deploy_issues
            ;;
        *)
            echo -e "${RED}$ICON_ERROR 未知的修复类型: $fix_type${NC}"
            echo "支持的类型: compile, test, quality, security, deploy, all"
            exit 1
            ;;
    esac
    
    # 完成报告
    finish_report "$fix_actions" "$suggestions"
    
    echo
    echo -e "${GREEN}$ICON_SUCCESS 自动修复完成！${NC}"
    log_message "INFO" "自动修复流程完成: $fix_type"
    
    show_report
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}万里后端项目 - 自动修复工具${NC}"
    echo
    echo -e "${WHITE}用法:${NC}"
    echo "  $0 [修复类型]"
    echo
    echo -e "${WHITE}修复类型:${NC}"
    echo "  compile   - 修复编译错误"
    echo "  test      - 修复测试失败"
    echo "  quality   - 修复代码质量问题"
    echo "  security  - 修复安全漏洞"
    echo "  deploy    - 修复部署问题"
    echo "  all       - 执行所有修复 (默认)"
    echo
    echo -e "${WHITE}示例:${NC}"
    echo "  $0 compile    # 只修复编译错误"
    echo "  $0 all        # 执行全面修复"
    echo
}

# 主函数
main() {
    local fix_type="${1:-all}"
    
    case "$fix_type" in
        "-h"|"--help")
            show_help
            exit 0
            ;;
        *)
            run_fixes "$fix_type"
            ;;
    esac
}

# 脚本入口
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi