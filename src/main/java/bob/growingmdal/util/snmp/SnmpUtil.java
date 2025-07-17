package bob.growingmdal.util.snmp;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.*;

public class SnmpUtil {

    private Snmp snmp;
    private TransportMapping transport;
    private CommunityTarget target;
    private final String address;
    private final String community;
    private final int version;
    private final int timeout;
    private final int retries;
    private final int port = 161;

    public SnmpUtil(String address) throws IOException {
        this(address, "public");
    }

    public SnmpUtil(String address, String community) throws IOException {
        this(address, community, SnmpConstants.version2c, 1500, 2);
    }

    public SnmpUtil(String address, String community, int version, int timeout, int retries) throws IOException {
        this.address = address;
        this.community = community;
        this.version = version;
        this.timeout = timeout;
        this.retries = retries;
        this.snmp = createSnmpSession();
        target = createTarget();
    }

    /**
     * 创建 SNMP 会话
     */
    private Snmp createSnmpSession() throws IOException {
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
        return snmp;
    }

    /**
     * 创建 Target 配置
     */
    private CommunityTarget createTarget() {
        Address targetAddress = new UdpAddress(address + "/" + port);
        target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setVersion(version);
        target.setTimeout(timeout);
        target.setRetries(retries);
        return target;
    }
    /**
     * 发送SNMP GET请求
     *
     * @param oids OID列表
     * @return 响应结果列表
     * @throws IOException SNMP请求异常
     */
    public List<VariableBinding> snmpGet(String... oids) throws IOException {
        // 创建PDU
        PDU pdu = new PDU();
        for (String oid : oids) {
            pdu.add(new VariableBinding(new OID(oid)));
        }
        pdu.setType(PDU.GET);

        // 发送请求并获取响应
        ResponseEvent response = snmp.send(pdu, target);

        // 处理响应
        if (response != null && response.getResponse() != null) {
            PDU responsePDU = response.getResponse();
//            System.out.println("Got response with status: " + responsePDU.getErrorStatusText());

            //                System.out.println(vb.getOid() + " = " + vb.getVariable());
            return new ArrayList<>(responsePDU.getVariableBindings());
        }
        return null;
    }

    /**
     * 发送SNMP GET请求并返回单个OID的值
     *
     * @param oid OID字符串
     * @return 变量值
     * @throws IOException SNMP请求异常
     */
    public Variable snmpGetSingle(String oid) throws IOException {
        List<VariableBinding> result = snmpGet(oid);
        if (result != null && !result.isEmpty()) {
            return result.get(0).getVariable();
        }
        return null;
    }

    /**
     * 关闭SNMP连接
     *
     * @throws IOException 关闭异常
     */
    public void close() throws IOException {
        if (snmp != null) {
            snmp.close();
        }
    }

    // 使用示例
    public static void main(String[] args) {
//        try {
//            // 创建SNMP工具实例
//            SnmpUtil snmpUtil = new SnmpUtil("192.168.107.112");
//
//            // 查询多个OID
//            List<VariableBinding> results = snmpUtil.snmpGet(
//                    ".1.3.6.1.2.1.43.8.2.1.10.1.4", // 纸盒
//                    ".1.3.6.1.2.1.43.11.1.1.9.1.2", // 墨粉
//                    ".1.3.6.1.2.1.25.3.5.1.2.1"     // 异常状态
//            );
//
//            for(VariableBinding vb : results){
//                System.out.println(vb.getOid() + " = " + vb.getVariable());
//            }
//            // 查询单个OID
//            Variable singleResult = snmpUtil.snmpGetSingle(".1.3.6.1.2.1.1.1.0");
//            System.out.println("System description: " + singleResult);
//
//            // 关闭连接
//            snmpUtil.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}