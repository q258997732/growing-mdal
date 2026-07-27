package bob.growingmdal.dto.rpa;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * K-RPA Agent 信息。
 */
@Data
@NoArgsConstructor
public class KAgentBean {

    private String id;
    private String pid;
    private String ip;
    private String name;
    private int online;
    private String version;
    private String system;
    private int offNotice;
    private String noticeUser;
    private int emptyNotify;
    private int synTime;
    private int music;
    private String remark;
    private String sysUser;
    private String muTagId;
    private String biosSN;
    private String serverInstance;
    private int iPause;
    private int iUpdate;

    public KAgentBean(Map<String, Object> beanParamsMap) {
        this.id = toString(beanParamsMap.get("ID"));
        this.pid = toString(beanParamsMap.get("PID"));
        this.ip = toString(beanParamsMap.get("IP"));
        this.name = toString(beanParamsMap.get("Name"));
        this.online = toInt(beanParamsMap.get("Online"));
        this.version = toString(beanParamsMap.get("Version"));
        this.system = toString(beanParamsMap.get("System"));
        this.offNotice = toInt(beanParamsMap.get("OffNotice"));
        this.noticeUser = toString(beanParamsMap.get("NoticeUser"));
        this.emptyNotify = toInt(beanParamsMap.get("EmptyNotify"));
        this.synTime = toInt(beanParamsMap.get("SynTime"));
        this.music = toInt(beanParamsMap.get("Music"));
        this.remark = toString(beanParamsMap.get("Remark"));
        this.sysUser = toString(beanParamsMap.get("sysUser"));
        this.muTagId = toString(beanParamsMap.get("MUTagID"));
        this.biosSN = toString(beanParamsMap.get("BiosSN"));
        this.serverInstance = toString(beanParamsMap.get("ServerInstance"));
        this.iPause = toInt(beanParamsMap.get("iPause"));
        this.iUpdate = toInt(beanParamsMap.get("iUpdate"));
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
