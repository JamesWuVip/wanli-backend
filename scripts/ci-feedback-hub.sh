#!/bin/bash

# 万里后端项目 CI/CD 反馈中心
# 统一管理所有CI/CD反馈功能的入口脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# 图标定义
ICON_SUCCESS="✅"
ICON_ERROR="❌"
ICON_WARNING="⚠️"
ICON_INFO="ℹ️"
ICON_ROCKET="🚀"
ICON_GEAR="⚙️"
ICON_MONITOR="👁️"
ICON_FIX="🔧"
ICON_LOG="📋"
ICON_CLEAN="🧹"
ICON_LINK="🔗"
ICON_TERMINAL="🖥️"

# 项目配置
PROJECT_NAME="万里后端项目"
GITHUB_REPO="JamesWuVip/wanli-backend"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 显示欢迎界面
show_welcome() {
    clear
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${WHITE}                           $PROJECT_NAME CI/CD 反馈中心                           ${CYAN}║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo
    echo -e "${GREEN}$ICON_SUCCESS CI/CD 反馈中心已成功初始化！${NC}"
    echo
}

# 显示主菜单
show_main_menu() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${WHITE}                                主功能菜单${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo
    echo -e "${CYAN}1.${NC} ${ICON_TERMINAL} ${GREEN}实时反馈终端${NC}     - 在终端中实时显示所有CI/CD反馈"
    echo -e "${CYAN}2.${NC} ${ICON_MONITOR} ${GREEN}持续监控模式${NC}     - 自动监控CI/CD状态并触发修复"
    echo -e "${CYAN}3.${NC} ${ICON_FIX} ${GREEN}自动修复工具${NC}     - 根据问题类型自动修复代码"
    echo -e "${CYAN}4.${NC} ${ICON_INFO} ${GREEN}状态检查工具${NC}     - 检查GitHub Actions和CI/CD状态"
    echo -e "${CYAN}5.${NC} ${ICON_ROCKET} ${GREEN}一键部署流程${NC}     - 自动化代码提交和部署"
    echo -e "${CYAN}6.${NC} ${ICON_LOG} ${GREEN}查看反馈日志${NC}     - 查看所有历史反馈记录"
    echo -e "${CYAN}7.${NC} ${ICON_GEAR} ${GREEN}配置管理${NC}        - 管理CI/CD配置和环境设置"
    echo -e "${CYAN}8.${NC} ${ICON_LINK} ${GREEN}外部链接${NC}        - 快速访问GitHub、SonarCloud等"
    echo -e "${CYAN}9.${NC} ${ICON_CLEAN} ${GREEN}清理工具${NC}        - 清理日志和临时文件"
    echo -e "${CYAN}0.${NC} ${RED}❌ 退出${NC}            - 退出反馈中心"
    echo
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# 启动实时反馈终端
start_realtime_feedback() {
    echo -e "${GREEN}$ICON_TERMINAL 启动实时反馈终端...${NC}"
    if [ -f "$SCRIPT_DIR/terminal-feedback.sh" ]; then
        bash "$SCRIPT_DIR/terminal-feedback.sh"
    else
        echo -e "${RED}$ICON_ERROR 未找到 terminal-feedback.sh 脚本${NC}"
        read -p "按回车键继续..."
    fi
}

# 启动持续监控模式
start_continuous_monitor() {
    echo -e "${GREEN}$ICON_MONITOR 启动持续监控模式...${NC}"
    if [ -f "$SCRIPT_DIR/continuous-monitor.sh" ]; then
        bash "$SCRIPT_DIR/continuous-monitor.sh"
    else
        echo -e "${RED}$ICON_ERROR 未找到 continuous-monitor.sh 脚本${NC}"
        read -p "按回车键继续..."
    fi
}

# 启动自动修复工具
start_auto_fix() {
    echo -e "${GREEN}$ICON_FIX 启动自动修复工具...${NC}"
    echo
    echo "请选择修复类型："
    echo "1. 编译错误修复"
    echo "2. 测试失败修复"
    echo "3. 代码质量修复"
    echo "4. 安全漏洞修复"
    echo "5. 部署问题修复"
    echo "6. 全部修复"
    echo
    read -p "请输入选择 (1-6): " fix_choice
    
    case $fix_choice in
        1) fix_type="compile" ;;
        2) fix_type="test" ;;
        3) fix_type="quality" ;;
        4) fix_type="security" ;;
        5) fix_type="deploy" ;;
        6) fix_type="all" ;;
        *) echo -e "${RED}$ICON_ERROR 无效选择${NC}"; return ;;
    esac
    
    if [ -f "$SCRIPT_DIR/auto-feedback.sh" ]; then
        bash "$SCRIPT_DIR/auto-feedback.sh" "$fix_type"
    else
        echo -e "${RED}$ICON_ERROR 未找到 auto-feedback.sh 脚本${NC}"
    fi
    read -p "按回车键继续..."
}

# 状态检查工具
check_status() {
    echo -e "${GREEN}$ICON_INFO 检查CI/CD状态...${NC}"
    if [ -f "$SCRIPT_DIR/monitor-ci-status.sh" ]; then
        bash "$SCRIPT_DIR/monitor-ci-status.sh" -d
    else
        echo -e "${RED}$ICON_ERROR 未找到 monitor-ci-status.sh 脚本${NC}"
    fi
    read -p "按回车键继续..."
}

# 一键部署流程
start_deployment() {
    echo -e "${GREEN}$ICON_ROCKET 启动一键部署流程...${NC}"
    echo
    
    # 检查未提交的更改
    if ! git diff --quiet || ! git diff --cached --quiet; then
        echo -e "${YELLOW}$ICON_WARNING 检测到未提交的更改${NC}"
        git status --short
        echo
        read -p "是否要提交这些更改？(y/n): " commit_choice
        
        if [ "$commit_choice" = "y" ] || [ "$commit_choice" = "Y" ]; then
            read -p "请输入提交信息: " commit_message
            git add .
            git commit -m "$commit_message"
            echo -e "${GREEN}$ICON_SUCCESS 代码已提交${NC}"
        fi
    fi
    
    # 推送代码
    echo -e "${BLUE}$ICON_INFO 推送代码到远程仓库...${NC}"
    current_branch=$(git branch --show-current)
    git push origin "$current_branch"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}$ICON_SUCCESS 代码推送成功！${NC}"
        echo -e "${BLUE}$ICON_INFO 启动实时监控...${NC}"
        
        # 启动监控
        if [ -f "$SCRIPT_DIR/terminal-feedback.sh" ]; then
            bash "$SCRIPT_DIR/terminal-feedback.sh"
        fi
    else
        echo -e "${RED}$ICON_ERROR 代码推送失败${NC}"
    fi
    
    read -p "按回车键继续..."
}

# 查看反馈日志
view_logs() {
    echo -e "${GREEN}$ICON_LOG 查看反馈日志...${NC}"
    echo
    echo "可用的日志文件："
    
    log_files=("realtime-feedback.log" "monitor.log" "ci-feedback.log" "deployment.log")
    
    for i in "${!log_files[@]}"; do
        log_file="$LOG_DIR/${log_files[$i]}"
        if [ -f "$log_file" ]; then
            echo -e "${CYAN}$((i+1)).${NC} ${log_files[$i]} ($(wc -l < "$log_file") 行)"
        else
            echo -e "${GRAY}$((i+1)).${NC} ${log_files[$i]} (不存在)"
        fi
    done
    
    echo
    read -p "请选择要查看的日志文件 (1-${#log_files[@]}): " log_choice
    
    if [ "$log_choice" -ge 1 ] && [ "$log_choice" -le "${#log_files[@]}" ]; then
        log_file="$LOG_DIR/${log_files[$((log_choice-1))]}"
        if [ -f "$log_file" ]; then
            echo -e "${GREEN}$ICON_INFO 显示 ${log_files[$((log_choice-1))]}:${NC}"
            echo
            tail -50 "$log_file"
        else
            echo -e "${RED}$ICON_ERROR 日志文件不存在${NC}"
        fi
    else
        echo -e "${RED}$ICON_ERROR 无效选择${NC}"
    fi
    
    read -p "按回车键继续..."
}

# 配置管理
manage_config() {
    echo -e "${GREEN}$ICON_GEAR 配置管理...${NC}"
    echo
    echo "配置选项："
    echo "1. 查看CI/CD配置文件"
    echo "2. 编辑CI/CD配置"
    echo "3. 查看环境变量"
    echo "4. 检查依赖配置"
    echo "5. 查看分支策略"
    echo
    read -p "请选择配置选项 (1-5): " config_choice
    
    case $config_choice in
        1)
            if [ -f ".github/workflows/ci-cd.yml" ]; then
                echo -e "${GREEN}$ICON_INFO CI/CD配置文件内容:${NC}"
                cat .github/workflows/ci-cd.yml
            else
                echo -e "${RED}$ICON_ERROR 未找到CI/CD配置文件${NC}"
            fi
            ;;
        2)
            if [ -f ".github/workflows/ci-cd.yml" ]; then
                ${EDITOR:-nano} .github/workflows/ci-cd.yml
            else
                echo -e "${RED}$ICON_ERROR 未找到CI/CD配置文件${NC}"
            fi
            ;;
        3)
            echo -e "${GREEN}$ICON_INFO 环境变量:${NC}"
            env | grep -E "(JAVA|MAVEN|SPRING|CI|CD)" | sort
            ;;
        4)
            if [ -f "pom.xml" ]; then
                echo -e "${GREEN}$ICON_INFO Maven依赖配置:${NC}"
                mvn dependency:tree | head -20
            else
                echo -e "${RED}$ICON_ERROR 未找到pom.xml文件${NC}"
            fi
            ;;
        5)
            echo -e "${GREEN}$ICON_INFO Git分支策略:${NC}"
            git branch -a
            echo
            echo "当前分支: $(git branch --show-current)"
            ;;
        *)
            echo -e "${RED}$ICON_ERROR 无效选择${NC}"
            ;;
    esac
    
    read -p "按回车键继续..."
}

# 外部链接
show_external_links() {
    echo -e "${GREEN}$ICON_LINK 外部反馈链接...${NC}"
    echo
    echo -e "${CYAN}🔗 GitHub Actions:${NC}"
    echo "   https://github.com/$GITHUB_REPO/actions"
    echo
    echo -e "${CYAN}🔗 SonarCloud (代码质量):${NC}"
    echo "   https://sonarcloud.io/project/overview?id=wanli-backend"
    echo
    echo -e "${CYAN}🔗 Codecov (测试覆盖率):${NC}"
    echo "   https://codecov.io/gh/$GITHUB_REPO"
    echo
    echo -e "${CYAN}🔗 GitHub仓库:${NC}"
    echo "   https://github.com/$GITHUB_REPO"
    echo
    
    if command -v open >/dev/null 2>&1; then
        read -p "是否要在浏览器中打开GitHub Actions页面？(y/n): " open_choice
        if [ "$open_choice" = "y" ] || [ "$open_choice" = "Y" ]; then
            open "https://github.com/$GITHUB_REPO/actions"
        fi
    fi
    
    read -p "按回车键继续..."
}

# 清理工具
clean_system() {
    echo -e "${GREEN}$ICON_CLEAN 系统清理...${NC}"
    echo
    echo "清理选项："
    echo "1. 清理日志文件"
    echo "2. 清理临时文件"
    echo "3. 清理Maven缓存"
    echo "4. 全部清理"
    echo
    read -p "请选择清理选项 (1-4): " clean_choice
    
    case $clean_choice in
        1|4)
            echo -e "${BLUE}$ICON_INFO 清理日志文件...${NC}"
            rm -f "$LOG_DIR"/*.log
            echo -e "${GREEN}$ICON_SUCCESS 日志文件已清理${NC}"
            ;;&
        2|4)
            echo -e "${BLUE}$ICON_INFO 清理临时文件...${NC}"
            find . -name "*.tmp" -delete 2>/dev/null
            find . -name ".DS_Store" -delete 2>/dev/null
            echo -e "${GREEN}$ICON_SUCCESS 临时文件已清理${NC}"
            ;;&
        3|4)
            if command -v mvn >/dev/null 2>&1; then
                echo -e "${BLUE}$ICON_INFO 清理Maven缓存...${NC}"
                mvn clean
                echo -e "${GREEN}$ICON_SUCCESS Maven缓存已清理${NC}"
            fi
            ;;
        *)
            echo -e "${RED}$ICON_ERROR 无效选择${NC}"
            ;;
    esac
    
    read -p "按回车键继续..."
}

# 主循环
main() {
    while true; do
        show_welcome
        show_main_menu
        
        read -p "请选择功能 (0-9): " choice
        echo
        
        case $choice in
            1) start_realtime_feedback ;;
            2) start_continuous_monitor ;;
            3) start_auto_fix ;;
            4) check_status ;;
            5) start_deployment ;;
            6) view_logs ;;
            7) manage_config ;;
            8) show_external_links ;;
            9) clean_system ;;
            0) 
                echo -e "${GREEN}$ICON_SUCCESS 感谢使用万里后端CI/CD反馈中心！${NC}"
                exit 0
                ;;
            *) 
                echo -e "${RED}$ICON_ERROR 无效选择，请重新输入${NC}"
                read -p "按回车键继续..."
                ;;
        esac
    done
}

# 检查依赖
check_dependencies() {
    local missing_deps=()
    
    if ! command -v git >/dev/null 2>&1; then
        missing_deps+=("git")
    fi
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        echo -e "${RED}$ICON_ERROR 缺少必要的依赖: ${missing_deps[*]}${NC}"
        echo "请安装缺少的依赖后重新运行此脚本"
        exit 1
    fi
}

# 脚本入口
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    check_dependencies
    main "$@"
fi