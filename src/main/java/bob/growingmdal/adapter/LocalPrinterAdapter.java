package bob.growingmdal.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

@Slf4j
public class LocalPrinterAdapter {

    private PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    private static volatile LocalPrinterAdapter instance;

    private LocalPrinterAdapter() {
        super();
    }

    /**
     * 获取单例实例
     * @return LocalPrinterAdapter单例实例
     */
    public static LocalPrinterAdapter getInstance() {
        if (instance == null) {
            synchronized (LocalPrinterAdapter.class) {
                if (instance == null) {
                    instance = new LocalPrinterAdapter();
                }
            }
        }
        return instance;
    }

    /**
     * 判断打印机是否存在
     * @param printerName 打印机名称
     * @return true:存在 false:不存在
     */
    public boolean isPrinterExist(String printerName){
        for (PrintService printService : printServices) {
            if (printService.getName().equals(printerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打印PDF
     * @param PdfPath PDF文件路径
     * @param printerName 打印机名称
     * @return true:成功 false:失败
     */
    public boolean printPDF(String PdfPath, String printerName){
        try (PDDocument document = Loader.loadPDF(new File(PdfPath))) {
            PrinterJob job = PrinterJob.getPrinterJob();
            if (printerName != null && !printerName.isEmpty()) {
                PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
                for (PrintService service : printServices) {
                    if (service.getName().equals(printerName)) {
                        job.setPrintService(service);
                        break;
                    }
                }
            }
            job.setPageable(new PDFPageable(document));
            job.print();
        } catch (Exception e) {
            log.error("打印失败", e);
            return false;
        }
        return true;
    }

    /**
     * 从Base64字符串打印PDF
     * @param base64String Base64字符串
     * @param printerName 打印机名称
     * @return true:成功 false:失败
     */
    public boolean printPDFFromBase64(String base64String, String printerName) {
        File tempFile = null;
        try {
            // 解码Base64字符串
            byte[] pdfBytes = Base64.getDecoder().decode(base64String);

            // 创建临时文件
            tempFile = File.createTempFile("print_", ".pdf");
            tempFile.deleteOnExit();

            // 写入PDF数据到临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            // 调用现有的打印方法
            return printPDF(tempFile.getAbsolutePath(), printerName);
        } catch (Exception e) {
            log.error("Base64字符串打印失败", e);
            return false;
        } finally {
            // 尝试删除临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("无法删除临时PDF文件: {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }


}
