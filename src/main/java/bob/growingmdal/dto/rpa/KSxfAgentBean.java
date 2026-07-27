package bob.growingmdal.dto.rpa;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * K-RPA 当天任务信息。
 */
@Data
@NoArgsConstructor
public class KSxfAgentBean {

    private String FlowID;
    private String FlowName;
    private String Department;
    private String ScenesName;
    private String BeginTime;
    private String EndTime;
    private String Remark;
    private String ExecState;
    private String IP;
    private String AgentName;
    private String ExecTime;

    public KSxfAgentBean(Map<String, Object> beanParamsMap) {
        this.FlowID = toString(beanParamsMap.get("FlowID"));
        this.FlowName = toString(beanParamsMap.get("FlowName"));
        this.Department = toString(beanParamsMap.get("Department"));
        this.ScenesName = toString(beanParamsMap.get("ScenesName"));
        this.BeginTime = toString(beanParamsMap.get("BeginTime"));
        this.EndTime = toString(beanParamsMap.get("EndTime"));
        this.Remark = toString(beanParamsMap.get("Remark"));
        this.ExecState = toString(beanParamsMap.get("ExecState"));
        this.IP = toString(beanParamsMap.get("IP"));
        this.AgentName = toString(beanParamsMap.get("AgentName"));
        this.ExecTime = toString(beanParamsMap.get("ExecTime"));
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }
}
