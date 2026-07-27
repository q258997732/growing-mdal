package bob.growingmdal.util;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Jackson 命名策略：将字段名首字母大写（PascalCase）。
 * 用于 K-RPA 请求/响应序列化。
 */
public class PascalCaseNamingStrategy extends PropertyNamingStrategies.NamingBase {

    private static final long serialVersionUID = 1L;

    @Override
    public String translate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char first = input.charAt(0);
        if (Character.isUpperCase(first)) {
            return input;
        }
        return Character.toUpperCase(first) + input.substring(1);
    }
}
