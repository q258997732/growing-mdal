package bob.growingmdal.adapter;


import bob.growingmdal.util.snmp.SnmpUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LexmarkPrinterAdapter {

    private String ip;
    private String community;
    private SnmpUtils snmpUtils;

    public LexmarkPrinterAdapter(String ip, String community) {
        this.ip = ip;
        this.community = community;
        snmpUtils = new SnmpUtils(ip, community);
    }
}
