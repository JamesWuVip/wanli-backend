#!/bin/bash

# SP1 自动化验收测试运行器
# 用于运行后端的验收测试，并生成测试报告

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
PROJECT_ROOT=$(pwd)
TEST_RESULTS_DIR="$PROJECT_ROOT/test-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$TEST_RESULTS_DIR/SP1_acceptance_test_report_$TIMESTAMP.html"

# 创建测试结果目录
mkdir -p "$TEST_RESULTS_DIR"

echo -e "${BLUE}🚀 SP1 后端自动化验收测试开始${NC}"
echo -e "${BLUE}📅 测试时间: $(date)${NC}"
echo -e "${BLUE}📁 项目路径: $PROJECT_ROOT${NC}"
echo -e "${BLUE}📊 报告路径: $REPORT_FILE${NC}"
echo ""

# 函数：运行后端测试
run_backend_tests() {
    echo -e "${YELLOW}🔧 开始后端API验收测试...${NC}"
    
    # 检查Java和Maven
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ 错误: 未找到Java，请安装Java 17+${NC}"
        return 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ 错误: 未找到Maven，请安装Maven${NC}"
        return 1
    fi
    
    # 运行后端测试
    echo -e "${BLUE}📋 运行后端验收测试 (AC-BE-1.1 到 AC-BE-1.5)...${NC}"
    
    if mvn test -Dtest=SP1AcceptanceTest -Dspring.profiles.active=test > "$TEST_RESULTS_DIR/backend_test_$TIMESTAMP.log" 2>&1; then
        echo -e "${GREEN}✅ 后端验收测试通过${NC}"
        BACKEND_STATUS="PASSED"
    else
        echo -e "${RED}❌ 后端验收测试失败${NC}"
        echo -e "${YELLOW}📄 查看详细日志: $TEST_RESULTS_DIR/backend_test_$TIMESTAMP.log${NC}"
        BACKEND_STATUS="FAILED"
    fi
}



# 函数：生成HTML测试报告
generate_report() {
    echo -e "${BLUE}📊 生成测试报告...${NC}"
    
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SP1 自动化验收测试报告</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 8px 8px 0 0; }
        .content { padding: 30px; }
        .status-passed { color: #28a745; font-weight: bold; }
        .status-failed { color: #dc3545; font-weight: bold; }
        .test-section { margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }
        .test-criteria { background: #f8f9fa; padding: 15px; margin: 10px 0; border-left: 4px solid #007bff; }
        .timestamp { color: #6c757d; font-size: 0.9em; }
        .summary { display: flex; justify-content: space-around; margin: 20px 0; }
        .summary-item { text-align: center; padding: 20px; background: #f8f9fa; border-radius: 5px; }
        .log-link { color: #007bff; text-decoration: none; }
        .log-link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎯 SP1 自动化验收测试报告</h1>
            <p class="timestamp">测试时间: $(date)</p>
            <p>项目: 万里书院 - Sprint 1 验收测试</p>
        </div>
        
        <div class="content">
            <div class="summary">
                <div class="summary-item">
                    <h3>后端API测试</h3>
                    <p class="$([ "$BACKEND_STATUS" = "PASSED" ] && echo "status-passed" || echo "status-failed")">$BACKEND_STATUS</p>
                </div>
            </div>
            
            <div class="test-section">
                <h2>🔧 后端API验收测试</h2>
                <p><strong>状态:</strong> <span class="$([ "$BACKEND_STATUS" = "PASSED" ] && echo "status-passed" || echo "status-failed")">$BACKEND_STATUS</span></p>
                <p><strong>测试文件:</strong> src/test/java/com/wanli/SP1AcceptanceTest.java</p>
                <p><strong>日志文件:</strong> <a href="backend_test_$TIMESTAMP.log" class="log-link">backend_test_$TIMESTAMP.log</a></p>
                
                <h3>验收标准覆盖:</h3>
                <div class="test-criteria">
                    <strong>AC-BE-1.1:</strong> 用户注册功能 - 调用 POST /api/auth/register 成功创建用户
                </div>
                <div class="test-criteria">
                    <strong>AC-BE-1.2:</strong> 用户登录功能 - 调用 POST /api/auth/login 返回有效JWT
                </div>
                <div class="test-criteria">
                    <strong>AC-BE-1.3:</strong> 无权限访问保护 - 未授权访问返回403错误
                </div>
                <div class="test-criteria">
                    <strong>AC-BE-1.4:</strong> 有权限访问验证 - ROLE_HQ_TEACHER可访问所有API
                </div>
                <div class="test-criteria">
                    <strong>AC-BE-1.5:</strong> 数据关联验证 - 课时正确关联到课程
                </div>
            </div>
            

            
            <div class="test-section">
                <h2>📋 测试环境信息</h2>
                <p><strong>操作系统:</strong> $(uname -s)</p>
                <p><strong>Java版本:</strong> $(java -version 2>&1 | head -n 1 || echo "未安装")</p>
                <p><strong>Node.js版本:</strong> $(node --version 2>/dev/null || echo "未安装")</p>
                <p><strong>Maven版本:</strong> $(mvn --version 2>/dev/null | head -n 1 || echo "未安装")</p>

            </div>
        </div>
    </div>
</body>
</html>
EOF

    echo -e "${GREEN}📊 测试报告已生成: $REPORT_FILE${NC}"
}

# 主执行流程
main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help)
                echo "用法: $0 [选项]"
                echo "选项:"
                echo "  --help            显示此帮助信息"
                exit 0
                ;;
            *)
                echo -e "${RED}未知选项: $1${NC}"
                echo "使用 --help 查看可用选项"
                exit 1
                ;;
        esac
    done
    
    # 运行测试
    run_backend_tests
    
    echo ""
    generate_report
    
    # 显示最终结果
    echo ""
    echo -e "${BLUE}📋 测试结果摘要:${NC}"
    echo -e "   后端API测试: $([ "$BACKEND_STATUS" = "PASSED" ] && echo -e "${GREEN}✅ 通过${NC}" || echo -e "${RED}❌ 失败${NC}")"
    echo ""
    echo -e "${BLUE}📊 详细报告: $REPORT_FILE${NC}"
    echo -e "${BLUE}📁 测试日志: $TEST_RESULTS_DIR${NC}"
    
    # 返回适当的退出码
    if [[ "$BACKEND_STATUS" == "FAILED" ]]; then
        echo -e "${RED}❌ 测试失败${NC}"
        exit 1
    else
        echo -e "${GREEN}🎉 后端测试通过！可以开始SP1功能开发了！${NC}"
        exit 0
    fi
}

# 执行主函数
main "$@"