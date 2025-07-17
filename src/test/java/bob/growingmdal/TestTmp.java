package bob.growingmdal;

import bob.growingmdal.adapter.LexmarkPrinterAdapter;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class TestTmp {
    public static void main(String[] args) throws IOException {
        LexmarkPrinterAdapter lexmarkPrinterAdapter = new LexmarkPrinterAdapter("192.168.107.112", "public");
        String tmp = lexmarkPrinterAdapter.getPrinterStatus();
        System.out.println(tmp);
    }

}
