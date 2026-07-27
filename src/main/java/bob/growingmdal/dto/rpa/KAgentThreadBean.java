package bob.growingmdal.dto.rpa;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * K-RPA Agent 线程状态。
 */
@Data
@NoArgsConstructor
public class KAgentThreadBean {

    private String ID;
    private String IP;
    private String Name;
    private String Online;
    private String ThreadCount;
    private String ExecCount;
    private String WaitCount;

    public KAgentThreadBean(Map<String, Object> beanParamsMap) {
        this.ID = toString(beanParamsMap.get("ID"));
        this.IP = toString(beanParamsMap.get("IP"));
        this.Name = toString(beanParamsMap.get("Name"));
        this.Online = toString(beanParamsMap.get("Online"));
        this.ThreadCount = toString(beanParamsMap.get("ThreadCount"));
        this.ExecCount = toString(beanParamsMap.get("ExecCount"));
        this.WaitCount = toString(beanParamsMap.get("WaitCount"));
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }
}
