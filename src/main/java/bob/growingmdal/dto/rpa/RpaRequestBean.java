package bob.growingmdal.dto.rpa;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * K-RPA 请求/响应通用 Bean。
 */
@Data
@NoArgsConstructor
public class RpaRequestBean {

    private String Name;
    private int Type;
    private Object Value;

    public RpaRequestBean(Object value, int type, String name) {
        this.Value = value;
        this.Type = type;
        this.Name = name;
    }
}
