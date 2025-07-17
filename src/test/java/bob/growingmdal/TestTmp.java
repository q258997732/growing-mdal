package bob.growingmdal;

import bob.growingmdal.adapter.LexmarkPrinterAdapter;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class TestTmp {
    public static void main(String[] args) throws IOException {
        LexmarkPrinterAdapter lexmarkPrinterAdapter = new LexmarkPrinterAdapter("192.168.107.112", "public");
        String tmp = lexmarkPrinterAdapter.getPrinterErrStatus();
        System.out.println(tmp);
    }

}
