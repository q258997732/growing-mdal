package bob.growingmdal.adapter;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.core.exception.PreOperationException;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.response.NantianCameraResponse;
import bob.growingmdal.util.ZZWsResponseParser;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@ClientEndpoint
public class NantianCameraService extends AnnotationDrivenHandler {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Getter
    private final ArrayDeque<ByteBuffer> binaryQueue = new ArrayDeque<>();
    @Getter
    private final ArrayDeque<String> messageQueue = new ArrayDeque<>();
    @Getter
    private final AtomicBoolean videoCollect = new AtomicBoolean(false);
    private final AtomicBoolean getFaceStart = new AtomicBoolean(false);
    private final CountDownLatch messageLatch = new CountDownLatch(1);
    private volatile CountDownLatch binaryLatch = new CountDownLatch(1);
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService liveDetection = Executors.newSingleThreadScheduledExecutor();

    private Session session;
    private long msgStartTime;
    private long binaryStartTime;
    @Value("${nantian.msg.clean.interval}")
    private int msgCleanTimePeriod;
    @Value("${nantian.video.clean.interval}")
    private int binaryCleanTimePeriod;
    @Value("${nantian.camera.url}")
    private String cameraUrl;
    @Value("${nantian.camera.response.timeout}")
    private int responseTimeout;
    private boolean cameraOpen = false;

    @PostConstruct
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxBinaryMessageBufferSize(100 * 1024 * 1024);
            container.setDefaultMaxTextMessageBufferSize(50 * 1024 * 1024);
            container.connectToServer(this, new URI(cameraUrl));
            binaryStartTime = msgStartTime = System.currentTimeMillis();
            cleaner.scheduleAtFixedRate(cleanupTask, 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect WebSocket", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected to server: {}", cameraUrl);
        this.session = session;
    }

    // 处理文本消息
    @OnMessage
    public void onMessage(String message, Session session) {
//        log.info("Received TEXT message: {}", message);
        messageQueue.add(message);
        messageLatch.countDown();
    }

    // 处理二进制消息
    @OnMessage
    public void onMessage(ByteBuffer bytes, Session session) {
        if (videoCollect.get()) {
            log.info("Received BINARY message, length: {}", bytes.remaining());
            binaryQueue.add(bytes);
        } else {
//        try {
//            // 创建文件输出流
//            FileOutputStream fos = new FileOutputStream("received_image.jpg"+System.currentTimeMillis());
//            // 获取通道
//            FileChannel channel = fos.getChannel();
//            // 写入文件
//            channel.write(bytes);
//            channel.close();
//            fos.close();
//            System.out.println("Image saved as received_image.jpg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

            binaryLatch.countDown();
            binaryLatch = new CountDownLatch(1);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        cleaner.shutdown();
        liveDetection.shutdown();
        System.out.println("Connection closed: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: ");
        throwable.printStackTrace();
    }

    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
                log.info("Sent message: {}", message);
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 定时清理队列
     */
    private final Runnable cleanupTask = () -> {
        synchronized (messageQueue) {
            synchronized (this) { // 确保线程安全
                int removedBinary = binaryQueue.size();
                int removedText = messageQueue.size();
                long currentTime = System.currentTimeMillis();
                boolean isClear = false;

                // 清理二进制队列
                if (currentTime > binaryStartTime + binaryCleanTimePeriod) {
                    binaryQueue.clear();
                    isClear = true;
                    // 重置开始时间
                    binaryStartTime = System.currentTimeMillis();
                }

                // 清理文本队列
                if (currentTime > msgStartTime + msgCleanTimePeriod) {
                    messageQueue.clear();
                    isClear = true;
                    // 重置开始时间
                    msgStartTime = System.currentTimeMillis();
                }

                if ((removedBinary > 0 || removedText > 0) && isClear) {
                    log.info("Cleaned up {} binary and {} text messages", removedBinary, removedText);
                }
            }
        }
    };

    public NantianCameraResponse sendMessageGetResponse(String message, int timeout) {
        boolean received = false;
        String response = null;
        String retData = null;
        sendMessage(message);
        try {
            received = messageLatch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for response", e);
            return new NantianCameraResponse(500, "Internal Server Error", e.getMessage());
        }
        String tmp = messageQueue.getFirst();
        response = getMsgResponse(message);
        log.info("response is: {}", response);

        if (message.contains("Capture")) {
            retData = ZZWsResponseParser.getCaptureBase64(response);
        }

        response = ZZWsResponseParser.parseResponse(response);

        log.info(ZZWsResponseParser.parseResponse(response));

        return new NantianCameraResponse(response.contains("成功") ? 200 : 500, response, retData);
    }

    public String getMsgResponse(String message) {
        message = message.split("@")[0];
        String result = null;
        for (String str : messageQueue) {
            if (str.contains(message)) {
                result = str;
                messageQueue.removeFirstOccurrence(str);
                break;
            }
        }
        return result;
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenDevice")
    public NantianCameraResponse openDevice(int index) {
        String message = "OpenDevice@" + index;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 打开隐藏设备
     *
     * @param index 1-文件摄像头 2-人脸摄像头 3-环境摄像头 4-红外摄像头
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenHideDevice")
    public NantianCameraResponse openHideDevice(int index) {
        String message = "OpenHideDevice@" + index;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 打开隐藏摄像头视频
     *
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenHideVideo")
    public NantianCameraResponse openHideVideo() {
        String message = "OpenHideVideo";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenVideo")
    public NantianCameraResponse openVideo() {
        String message = "OpenVideo";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseDevice")
    public NantianCameraResponse closeDevice() {
        String message = "CloseDevice";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseHideDevice")
    public NantianCameraResponse closeHideDevice() {
        String message = "CloseHideDevice";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "UnFaceDetect")
    public NantianCameraResponse unFaceDetect() {
        String message = "UnFaceDetect";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopGetFace")
    public NantianCameraResponse stopGetFace() {
        String message = "StopGetFace";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseVideo")
    public NantianCameraResponse closeVideo() {
        String message = "CloseVideo";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseHideVideo")
    public NantianCameraResponse closeHideVideo() {
        String message = "CloseHideVideo";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "DeinitFaceMgr")
    public NantianCameraResponse deinitFaceMgr() {
        String message = "DeinitFaceMgr";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateRight")
    public NantianCameraResponse rotateRight() {
        String message = "RotateRight";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateLeft")
    public NantianCameraResponse rotateLeft() {
        String message = "RotateLeft";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateHideRight")
    public NantianCameraResponse rotateHideRight() {
        String message = "RotateHideRight";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateHideLeft")
    public NantianCameraResponse rotateHideLeft() {
        String message = "RotateHideLeft";
        return sendMessageGetResponse(message, responseTimeout);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "EnableFrFaceImage")
    public NantianCameraResponse enableFrFaceImage(int type) {
        String message = "EnableFrFaceImage@" + type;
        return sendMessageGetResponse(message, responseTimeout);
    }


    /**
     * 初始化人脸管理器
     *
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "InitFaceMgr")
    public NantianCameraResponse initFaceMgr() {
        String message = "InitFaceMgr";
        return sendMessageGetResponse(message, responseTimeout);
    }


    /**
     * 设置摄像头分辨率,设置前需确保已打开摄像头
     *
     * @param type   1-YUY2 2-MJPEG
     * @param width  宽
     * @param height 高
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "SetResolution")
    public NantianCameraResponse setResolution(int type, int width, int height) {
        if (!cameraOpen) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        String message = "SetResolution@" + type + "@" + width + "@" + height;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 设置隐藏摄像头分辨率,设置前需确保已打开摄像头
     *
     * @param type   1-YUY2 2-MJPEG
     * @param width  宽
     * @param height 高
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "SetHideResolution")
    public NantianCameraResponse setHideResolution(int type, int width, int height) {
        if (!cameraOpen) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        String message = "SetHideResolution@" + type + "@" + width + "@" + height;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 拍照 拍照前先确保已打开摄像头
     *
     * @param type 图像啊格式: 1-bmp 2-jpg 3-png 4-tiff 5-gif
     * @return base64图像
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "Capture")
    public NantianCameraResponse capture(int type) {
        String message = "Capture" + "@" + type;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 单独拍照，自动打开摄像头与视频并拍摄照片，注意：此方法会完成拍照后会自动关闭摄像头与视频
     *
     * @return base64图像
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "TakePhoto")
    public NantianCameraResponse takePhoto() {
        if (!cameraOpen) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        NantianCameraResponse result = null;
        try {
            result = capture(1);
            if (!result.isSuccess()) {
                throw new PreOperationException("Capture failed");
            }
        } catch (PreOperationException e) {
            return new NantianCameraResponse(500, "Pre Operation fail", e.getMessage());
        }

        return result;
    }

    /**
     * 启动摄像头/隐藏摄像头与视频，初始化人脸识别库
     *
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StartNtCamera")
    public NantianCameraResponse startNtCamera() {
        if (cameraOpen) {
            return new NantianCameraResponse(500, "Camera already open", "");
        }

        NantianCameraResponse result = null;
        try {
            // 打开摄像头
            result = openDevice(2);
            if (!result.isSuccess()) {
                throw new PreOperationException("Open camera failed");
            }
            cameraOpen = true;


            // 设置分辨率
            result = setResolution(2, 640, 480);
            if (!result.isSuccess()) {
                throw new PreOperationException("Set resolution failed");
            }
            // 启动视频
            result = openVideo();
            if (!result.isSuccess()) {
                throw new PreOperationException("Open video failed");
            }
            // 向右旋转
            result = rotateRight();
            if (!result.isSuccess()) {
                throw new PreOperationException("Rotate right failed");
            }

            // 打开隐藏摄像头
            result = openHideDevice(4);
            if (!result.isSuccess()) {
                throw new PreOperationException("Open hide camera failed");
            }
            // 设置隐藏摄像头分辨率
            result = setHideResolution(2, 640, 480);
            if (!result.isSuccess()) {
                throw new PreOperationException("Set hide Resolution failed");
            }
            // 启动隐藏摄像头视频
            result = openHideVideo();
            if (!result.isSuccess()) {
                throw new PreOperationException("Open hide video failed");
            }
            // 向右旋转
            result = rotateHideRight();
            if (!result.isSuccess()) {
                throw new PreOperationException("Rotate right failed");
            }

            // 启动人脸检测
            result = enableFrFaceImage(1);
            if (!result.isSuccess()) {
                throw new PreOperationException("Enable fr face image failed");
            }

            // 初始化人脸识别库
            result = initFaceMgr();
            if (!result.isSuccess()) {
                throw new PreOperationException("Init face mgr failed");
            }

        } catch (PreOperationException e) {
            return new NantianCameraResponse(500, "Pre Operation fail", e.getMessage());
        }
        return result;
    }

    /**
     * 停止摄像头/隐藏摄像头与视频，释放人脸识别库
     *
     * @return 响应
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopNtCamera")
    public NantianCameraResponse stopNtCamera() {
        if (!cameraOpen) {
            return new NantianCameraResponse(200, "Camera not open", "");
        }
        NantianCameraResponse result = null;
        try {
            result = closeVideo();
            if (!result.isSuccess()) {
                throw new PreOperationException("Close video failed");
            }

            result = closeHideVideo();
            if (!result.isSuccess()) {
                throw new PreOperationException("Close video failed");
            }

            result = closeDevice();
            if (!result.isSuccess()) {
                throw new PreOperationException("Close device failed");
            }

            result = closeHideVideo();
            if (!result.isSuccess()) {
                throw new PreOperationException("Close hide device failed");
            }

            result = deinitFaceMgr();
            if (!result.isSuccess()) {
                throw new PreOperationException("Deinit face mgr failed");
            }

            cameraOpen = false;
        } catch (PreOperationException e) {
            return new NantianCameraResponse(500, "Pre Operation fail", e.getMessage());
        }
        return result;
    }

    /**
     * 开始人脸识别
     *
     * @param command 1-bmp 2-jpg 3-png 4-tiff 5-gif
     * @return 调用结果
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "GetFaceTempl")
    public NantianCameraResponse getFaceTempl(DeviceCommand command) {
        if (!getFaceStart.get()) {
            String message = "GetFaceTempl@2";
            // 开启新线程执行
            new Thread(() -> {
                // 判断是否已经开启任务
                while (!getFaceStart.get()) {
                    synchronized (messageQueue) {
                        Iterator<String> iterator = messageQueue.iterator();
                        while (iterator.hasNext()) {
                            String current = iterator.next();
                            if (current.contains("FaceResultEvent")) {
                                log.info("人脸识别结果: {}", ZZWsResponseParser.parseResponse(current));
                                command.setTransferData(ZZWsResponseParser.parseResponse(current));
                                performOperation(command);
                                iterator.remove();
                            }
                        }
                    }
                    try {
                        Thread.sleep(100); // 添加休眠避免过度消耗CPU
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            }).start();

            return sendMessageGetResponse(message, responseTimeout);
        } else {
            return new NantianCameraResponse(500, "GetFaceTempl already start", "");
        }
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopGetFaceTempl")
    public String stopGetFaceTempl() {
        getFaceStart.set(true);
        return "StopGetFaceTempl success . ";
    }

    /**
     * 人脸比对
     *
     * @param eigenvalue 要匹配的特征值，通过人脸识别
     * @param threshold  阈值(1-100)
     * @param type       图像格式：1-bmp 2-jpg 3-png 4-tiff 5-gif
     * @return 调用结果
     */
    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "FaceDetect")
    public NantianCameraResponse faceDetect(String eigenvalue, int threshold, int type) {
        String message = "FaceDetect" + "@" + eigenvalue + "@" + threshold + "@" + type;
        return sendMessageGetResponse(message, responseTimeout);
    }

    /**
     * 直接将信息从Service返回client
     *
     * @param command 命令
     */
    public void performOperation(DeviceCommand command) {
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @Override
    public boolean supports(DeviceCommand command) {
        return "Camera".equals(command.getDeviceType());
    }


//
//    public static void main(String[] args) {
//        try {
//            URI uri = new URI("ws://192.168.107.103:7000");
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            container.setDefaultMaxBinaryMessageBufferSize(100 * 1024 * 1024);
//
//            NantianCameraService client = new NantianCameraService();
//            container.connectToServer(client, uri);
//
//            client.sendMessage("OpenDevice@2");
//
//            boolean received = client.messageLatch.await(10, TimeUnit.SECONDS);
//            if (!received) {
//                System.out.println("No response from server within 10 seconds");
//            }
//
//            boolean bo = false;
//            for (String str : client.messageQueue) {
//                if (str.contains("OpenDevice")) {
//                    bo = true;
//                    System.out.println("OpenDevice success. " + ZZWsResponseParser.parseResponse(str));
//                }
//            }
//            if (!bo) {
//                System.out.println("OpenDevice failed.");
//                return;
//            }
//            System.out.println(System.currentTimeMillis());
//            client.sendMessage("OpenVideo");
//            received = client.binaryLatch.await(2, TimeUnit.SECONDS);
//            if (!received) {
//                System.out.println("No response from server within 2 seconds");
//            }
//            System.out.println(System.currentTimeMillis());
//
//            client.sendMessage("CloseVideo");
//
//
//            client.sendMessage("CloseDevice");
//            received = client.messageLatch.await(2, TimeUnit.SECONDS);
//            if (!received) {
//                System.out.println("No response from server within 10 seconds");
//            }
//            Thread.sleep(1000);
//            client.session.close();
//            System.out.println("Message queue:");
//            for (String str : client.messageQueue) {
//                System.out.println(str);
//            }
//            System.out.println("Binary queue: " + client.binaryQueue.size());
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}