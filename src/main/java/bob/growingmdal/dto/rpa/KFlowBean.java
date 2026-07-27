package bob.growingmdal.dto.rpa;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * K-RPA 流程信息。
 */
@Data
@NoArgsConstructor
public class KFlowBean {

    private String FlowID;
    private String PID;
    private String FlowName;
    private String ModifyTime;
    private String CreateTime;
    private String IsDisable;
    private String Version;
    private String Audit;
    private String IsPassed;
    private String FlowVer;
    private String Remark;
    private String BsID;
    private String BsFlow;
    private String TagID;
    private String TagColor;
    private String TagName;
    private String Importance;

    public KFlowBean(Map<String, Object> beanParamsMap) {
        this.FlowID = toString(beanParamsMap.get("FlowID"));
        this.PID = toString(beanParamsMap.get("PID"));
        this.FlowName = toString(beanParamsMap.get("FlowName"));
        this.ModifyTime = toString(beanParamsMap.get("ModifyTime"));
        this.CreateTime = toString(beanParamsMap.get("CreateTime"));
        this.IsDisable = toString(beanParamsMap.get("IsDisable"));
        this.Version = toString(beanParamsMap.get("Version"));
        this.Audit = toString(beanParamsMap.get("Audit"));
        this.IsPassed = toString(beanParamsMap.get("IsPassed"));
        this.FlowVer = toString(beanParamsMap.get("FlowVer"));
        this.Remark = toString(beanParamsMap.get("Remark"));
        this.BsID = toString(beanParamsMap.get("BsID"));
        this.BsFlow = toString(beanParamsMap.get("BsFlow"));
        this.TagID = toString(beanParamsMap.get("TagID"));
        this.TagColor = toString(beanParamsMap.get("TagColor"));
        this.TagName = toString(beanParamsMap.get("TagName"));
        this.Importance = toString(beanParamsMap.get("Importance"));
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }
}
