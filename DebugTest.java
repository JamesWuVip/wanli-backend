// 临时调试文件
public class DebugTest {
    public static void main(String[] args) {
        // 用于调试异常消息的临时代码
        try {
            // 模拟CourseService的validatePaginationParams调用
            validatePaginationParams(-1, 10);
        } catch (Exception e) {
            System.out.println("Exception message: " + e.getMessage());
            System.out.println("Contains '页码不能小于0': " + e.getMessage().contains("页码不能小于0"));
        }
    }
    
    private static void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new RuntimeException("页码不能小于0");
        }
        if (size <= 0) {
            throw new RuntimeException("每页大小必须大于0");
        }
        if (size > 100) {
            throw new RuntimeException("每页大小不能超过100");
        }
    }
}