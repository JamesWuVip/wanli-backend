import java.util.regex.Pattern;

public class EmailTest {
    
    // 邮箱验证的正则表达式
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
    
    public static void main(String[] args) {
        // 测试用例
        String[] testEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "invalid.email",
            "@invalid.com",
            "user@",
            "user@.com",
            "user@domain.",
            "valid_email@test-domain.com"
        };
        
        System.out.println("邮箱验证测试结果:");
        System.out.println("==================");
        
        for (String email : testEmails) {
            boolean isValid = isValidEmail(email);
            System.out.printf("%-30s -> %s%n", email, isValid ? "有效" : "无效");
        }
        
        System.out.println("\n测试完成!");
    }
}
