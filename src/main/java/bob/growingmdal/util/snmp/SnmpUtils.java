package bob.growingmdal.util.snmp;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.*;

public class SnmpUtils {

    private final String address;
    private final String community;
    private final int version;
    private final int timeout;
    private final int retries;

    public SnmpUtils(String address, String community) {
        this(address, community, SnmpConstants.version2c, 1500, 2);
    }

    public SnmpUtils(String address, String community, int version, int timeout, int retries) {
        this.address = address;
        this.community = community;
        this.version = version;
        this.timeout = timeout;
        this.retries = retries;
    }

    /**
     * 创建 SNMP 会话
     */
    private Snmp createSnmpSession() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
        return snmp;
    }

    /**
     * 创建 Target 配置
     */
    private Target createTarget() {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(GenericAddress.parse(address));
        target.setVersion(version);
        target.setTimeout(timeout);
        target.setRetries(retries);
        return target;
    }

    /**
     * GET 操作 - 获取单个 OID 值
     */
    public String get(String oid) throws IOException {
        try (Snmp snmp = createSnmpSession()) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            ResponseEvent event = snmp.send(pdu, createTarget());
            return handleResponse(event, oid);
        }
    }

    /**
     * GETNEXT 操作 - 获取下一个 OID 的值
     */
    public Map<String, String> getNext(String oid) throws IOException {
        try (Snmp snmp = createSnmpSession()) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GETNEXT);

            ResponseEvent event = snmp.send(pdu, createTarget());
            return handleGetNextResponse(event);
        }
    }

    /**
     * WALK 操作 - 遍历指定 OID 下的所有子节点
     */
    public Map<String, String> walk(String rootOid) throws IOException {
        Map<String, String> result = new TreeMap<>();
        try (Snmp snmp = createSnmpSession()) {
            Target target = createTarget();

            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(rootOid));

            if (events == null || events.isEmpty()) {
                return result;
            }

            for (TreeEvent event : events) {
                if (event != null && !event.isError()) {
                    VariableBinding[] vbs = event.getVariableBindings();
                    if (vbs != null) {
                        for (VariableBinding vb : vbs) {
                            result.put(vb.getOid().toString(), vb.getVariable().toString());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * GETTABLE 操作 - 获取表格数据（如接口表、路由表等）
     */
    public List<Map<String, String>> getTable(String tableOid) throws IOException {
        List<Map<String, String>> table = new ArrayList<>();
        Map<String, String> allData = walk(tableOid);

        // 按行索引分组
        Map<String, Map<String, String>> rows = new TreeMap<>();

        for (Map.Entry<String, String> entry : allData.entrySet()) {
            String fullOid = entry.getKey();
            String value = entry.getValue();

            // 提取行索引 (tableOid 之后的数字部分)
            String rowIndex = fullOid.substring(tableOid.length() + 1);
            String columnOid = rowIndex.substring(rowIndex.lastIndexOf('.') + 1);

            // 获取行标识 (去掉最后一列的OID)
            String rowIdentifier = rowIndex.substring(0, rowIndex.lastIndexOf('.'));

            // 初始化行数据
            if (!rows.containsKey(rowIdentifier)) {
                rows.put(rowIdentifier, new TreeMap<>());
            }

            // 添加列数据 (列OID -> 值)
            rows.get(rowIdentifier).put(columnOid, value);
        }

        // 转换为行列表
        table.addAll(rows.values());
        return table;
    }

    private String handleResponse(ResponseEvent event, String oid) {
        if (event == null || event.getResponse() == null) {
            throw new RuntimeException("SNMP request timed out for OID: " + oid);
        }

        PDU response = event.getResponse();
        if (response.getErrorStatus() != PDU.noError) {
            throw new RuntimeException("SNMP error: " +
                    response.getErrorStatusText() + " for OID: " + oid);
        }

        VariableBinding vb = response.get(0);
        return vb.getVariable().toString();
    }

    private Map<String, String> handleGetNextResponse(ResponseEvent event) {
        Map<String, String> result = new HashMap<>();
        if (event == null || event.getResponse() == null) {
            throw new RuntimeException("SNMP request timed out");
        }

        PDU response = event.getResponse();
        if (response.getErrorStatus() != PDU.noError) {
            throw new RuntimeException("SNMP error: " + response.getErrorStatusText());
        }

        for (int i = 0; i < response.size(); i++) {
            VariableBinding vb = response.get(i);
            result.put(vb.getOid().toString(), vb.getVariable().toString());
        }
        return result;
    }

    // 使用示例
    public static void main(String[] args) {
        String deviceIp = "192.168.1.1";
        String community = "public";

        SnmpUtils snmp = new SnmpUtils("udp:" + deviceIp + "/161", community);

        try {
            // 1. GET 示例
            String sysDescr = snmp.get("1.3.6.1.2.1.1.1.0");
            System.out.println("System Description: " + sysDescr);

            // 2. GETNEXT 示例
            Map<String, String> nextOid = snmp.getNext("1.3.6.1.2.1.1.1.0");
            System.out.println("Next OID value: " + nextOid);

            // 3. WALK 示例
            Map<String, String> systemWalk = snmp.walk("1.3.6.1.2.1.1");
            System.out.println("System OIDs:");
            systemWalk.forEach((k, v) -> System.out.println(k + " = " + v));

            // 4. GETTABLE 示例 (接口表)
            List<Map<String, String>> ifTable = snmp.getTable("1.3.6.1.2.1.2.2.1");
            System.out.println("Interface Table:");
            for (int i = 0; i < ifTable.size(); i++) {
                System.out.println("Interface " + i + ": " + ifTable.get(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}