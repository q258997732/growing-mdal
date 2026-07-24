package bob.growingmdal.camera;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.command.HardwareCommandHandler;
import bob.growingmdal.core.exception.PreOperationException;
import bob.growingmdal.entity.OperationResultEvent;
import bob.growingmdal.entity.TimestampedBuffer;
import bob.growingmdal.entity.response.NantianCameraResponse;
import bob.growingmdal.util.ZZWsResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class CameraCommandExecutor implements HardwareCommandHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final CameraResponseMatcher responseMatcher;
    private final CameraMessageRouter messageRouter;
    private final CameraLifecycleManager lifecycleManager;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, AtomicBoolean> cameraStatus = new ConcurrentHashMap<>();
    private final ExecutorService faceDetectionExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService videoStreamExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService getFaceTemplExecutor = Executors.newSingleThreadExecutor();

    @Value("${nantian.camera.response.timeout}")
    private int responseTimeout;
    @Value("${nantian.camera.video.time}")
    private int videoTime;
    @Value("${nantian.camera.detect.time}")
    private int detectTime;

    private Instant lastGetVideoTime;
    private Instant lastFaceDetectTime;

    @Autowired
    public CameraCommandExecutor(ApplicationEventPublisher eventPublisher,
                                 CameraResponseMatcher responseMatcher,
                                 CameraMessageRouter messageRouter,
                                 CameraLifecycleManager lifecycleManager,
                                 ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.responseMatcher = responseMatcher;
        this.messageRouter = messageRouter;
        this.lifecycleManager = lifecycleManager;
        this.objectMapper = objectMapper;
        initializeStatus();
    }

    private void initializeStatus() {
        cameraStatus.put("cameraOpen", new AtomicBoolean(false));
        cameraStatus.put("faceDetect", new AtomicBoolean(false));
        cameraStatus.put("getFaceStart", new AtomicBoolean(false));
        cameraStatus.put("getVideo", new AtomicBoolean(false));
        cameraStatus.put("videoCollect", new AtomicBoolean(true));
    }

    @Override
    public boolean supports(DeviceCommand command) {
        return "Camera".equals(command.getDeviceType());
    }

    public ConcurrentHashMap<String, AtomicBoolean> getCameraStatus() {
        return cameraStatus;
    }

    public void shutdown() {
        faceDetectionExecutor.shutdownNow();
        videoStreamExecutor.shutdownNow();
        getFaceTemplExecutor.shutdownNow();
    }

    public void performOperation(DeviceCommand command) {
        command.setFunction("OutPut");
        eventPublisher.publishEvent(new OperationResultEvent(command.getSession(), command.toString()));
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenDevice")
    public NantianCameraResponse openDevice(int index) {
        return send("OpenDevice@" + index);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenHideDevice")
    public NantianCameraResponse openHideDevice(int index) {
        return send("OpenHideDevice@" + index);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenHideVideo")
    public NantianCameraResponse openHideVideo() {
        return send("OpenHideVideo");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "OpenVideo")
    public NantianCameraResponse openVideo() {
        return send("OpenVideo");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseDevice")
    public NantianCameraResponse closeDevice() {
        return send("CloseDevice");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseHideDevice")
    public NantianCameraResponse closeHideDevice() {
        return send("CloseHideDevice");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "UnFaceDetect")
    public NantianCameraResponse unFaceDetect() {
        return send("UnFaceDetect");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopGetFace")
    public NantianCameraResponse stopGetFace() {
        return send("StopGetFace");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseVideo")
    public NantianCameraResponse closeVideo() {
        return send("CloseVideo");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "CloseHideVideo")
    public NantianCameraResponse closeHideVideo() {
        return send("CloseHideVideo");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "DeinitFaceMgr")
    public NantianCameraResponse deinitFaceMgr() {
        return send("DeinitFaceMgr");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateRight")
    public NantianCameraResponse rotateRight() {
        return send("RotateRight");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateLeft")
    public NantianCameraResponse rotateLeft() {
        return send("RotateLeft");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateHideRight")
    public NantianCameraResponse rotateHideRight() {
        return send("RotateHideRight");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "RotateHideLeft")
    public NantianCameraResponse rotateHideLeft() {
        return send("RotateHideLeft");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "EnableFrFaceImage")
    public NantianCameraResponse enableFrFaceImage(int type) {
        return send("EnableFrFaceImage@" + type);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "InitFaceMgr")
    public NantianCameraResponse initFaceMgr() {
        return send("InitFaceMgr");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "SetResolution")
    public NantianCameraResponse setResolution(int type, int width, int height) {
        if (!cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        return send("SetResolution@" + type + "@" + width + "@" + height);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "SetHideResolution")
    public NantianCameraResponse setHideResolution(int type, int width, int height) {
        if (!cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        return send("SetHideResolution@" + type + "@" + width + "@" + height);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "Capture")
    public NantianCameraResponse capture(int type) {
        return send("Capture@" + type);
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "TakePhoto")
    public NantianCameraResponse takePhoto() {
        if (!cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        NantianCameraResponse result = capture(1);
        if (!result.isSuccess()) {
            return new NantianCameraResponse(500, "Pre Operation fail", "Capture failed");
        }
        return result;
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StartNtCamera")
    public NantianCameraResponse startNtCamera() {
        if (!lifecycleManager.isEnabled()) {
            return unavailable();
        }
        if (cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(500, "Camera already open", "");
        }
        try {
            NantianCameraResponse result = openDevice(2);
            if (!result.isSuccess()) throw new PreOperationException("Open camera failed");
            cameraStatus.get("cameraOpen").set(true);

            result = setResolution(2, 640, 480);
            if (!result.isSuccess()) throw new PreOperationException("Set resolution failed");

            result = openVideo();
            if (!result.isSuccess()) throw new PreOperationException("Open video failed");

            result = rotateRight();
            if (!result.isSuccess()) throw new PreOperationException("Rotate right failed");

            result = openHideDevice(4);
            if (!result.isSuccess()) throw new PreOperationException("Open hide camera failed");

            result = setHideResolution(2, 640, 480);
            if (!result.isSuccess()) throw new PreOperationException("Set hide Resolution failed");

            result = openHideVideo();
            if (!result.isSuccess()) throw new PreOperationException("Open hide video failed");

            result = rotateHideRight();
            if (!result.isSuccess()) throw new PreOperationException("Rotate hide right failed");

            result = enableFrFaceImage(1);
            if (!result.isSuccess()) throw new PreOperationException("Enable fr face image failed");

            result = initFaceMgr();
            for (int i = 0; i < 3; i++) {
                if (result.isSuccess()) break;
                result = initFaceMgr();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("init face mgr , sleep error : {}", e.getMessage());
                }
            }
            if (!result.isSuccess()) throw new PreOperationException("Init face mgr failed");

            return result;
        } catch (PreOperationException e) {
            return new NantianCameraResponse(500, "Pre Operation fail", e.getMessage());
        }
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopNtCamera")
    public NantianCameraResponse stopNtCamera() {
        if (!cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(200, "Camera not open", "");
        }
        cameraStatus.get("cameraOpen").set(false);
        try {
            NantianCameraResponse result = closeVideo();
            if (!result.isSuccess()) throw new PreOperationException("Close video failed");

            result = closeHideVideo();
            if (!result.isSuccess()) throw new PreOperationException("Close hide video failed");

            result = closeDevice();
            if (!result.isSuccess()) throw new PreOperationException("Close device failed");

            result = closeHideDevice();
            if (!result.isSuccess()) throw new PreOperationException("Close hide device failed");

            result = deinitFaceMgr();
            if (!result.isSuccess()) throw new PreOperationException("Deinit face mgr failed");

            return result;
        } catch (PreOperationException e) {
            return new NantianCameraResponse(500, "Pre Operation fail", e.getMessage());
        }
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "GetFaceTempl")
    public NantianCameraResponse getFaceTempl(DeviceCommand command) {
        if (!cameraStatus.get("getFaceStart").compareAndSet(false, true)) {
            return new NantianCameraResponse(500, "GetFaceTempl already start", "");
        }
        NantianCameraResponse result = send("GetFaceTempl@2");

        getFaceTemplExecutor.submit(() -> {
            while (cameraStatus.get("getFaceStart").get() && !Thread.currentThread().isInterrupted()) {
                messageRouter.drainMatching(msg -> {
                    if (msg.startsWith("FaceResultEvent#")) {
                        log.debug("人脸识别结果: {}", ZZWsResponseParser.parseResponse(msg));
                        command.setTransferData(ZZWsResponseParser.parseResponse(msg));
                        performOperation(command);
                        return true;
                    }
                    return false;
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        return result;
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopGetFaceTempl")
    public String stopGetFaceTempl() {
        cameraStatus.get("getFaceStart").set(false);
        return "StopGetFaceTempl success";
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StartGetVideo")
    public NantianCameraResponse startGetVideo(DeviceCommand command) {
        if (!cameraStatus.get("cameraOpen").get()) {
            return new NantianCameraResponse(500, "Camera not open", "");
        }
        if (!cameraStatus.get("getVideo").compareAndSet(false, true)) {
            return new NantianCameraResponse(500, "Video already open", "");
        }
        lastGetVideoTime = Instant.now();

        videoStreamExecutor.submit(() -> {
            while (cameraStatus.get("getVideo").get() && !Thread.currentThread().isInterrupted()) {
                Instant end = lastGetVideoTime.plus(Duration.ofSeconds(videoTime));
                if (Instant.now().isAfter(end)) {
                    cameraStatus.get("getVideo").set(false);
                    log.info("Get video finish ({}s)", videoTime);
                }
                messageRouter.getBinaryQueue().getBetweenAndRemove(lastGetVideoTime, end).forEach(tb -> {
                    String base64Str = Base64.getEncoder().encodeToString(tb.getBuffer().array());
                    command.setTransferData(base64Str);
                    performOperation(command);
                });
            }
            cameraStatus.get("getVideo").set(false);
        });
        return new NantianCameraResponse(200, "Get Video start", "");
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopGetVideo")
    public String stopGetVideo() {
        cameraStatus.get("getVideo").set(false);
        return "Stop Get Video";
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "FaceDetect")
    public NantianCameraResponse faceDetect(DeviceCommand command) {
        if (!cameraStatus.get("faceDetect").compareAndSet(false, true)) {
            return new NantianCameraResponse(500, "FaceDetect already start", "");
        }
        NantianCameraResponse result;
        try {
            String transferDataJson = command.getTransferData().replace("\\\"", "\"");
            JsonNode transferData = objectMapper.readTree(transferDataJson);
            String threshold = transferData.get("threshold").asText();
            String eigenvalue = transferData.get("eigenvalue").asText();
            String message = "FaceDetect@" + eigenvalue + "@" + threshold + "@1";
            result = send(message);
        } catch (Exception e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            cameraStatus.get("faceDetect").set(false);
            return new NantianCameraResponse(400, "Fail", "error :" + e.getMessage());
        }
        lastFaceDetectTime = Instant.now();

        faceDetectionExecutor.submit(() -> {
            while (cameraStatus.get("faceDetect").get() && !Thread.currentThread().isInterrupted()) {
                if (lastFaceDetectTime.plus(Duration.ofSeconds(detectTime)).isBefore(Instant.now())) {
                    command.setTransferData("face detect timeout :" + detectTime);
                    performOperation(command);
                    stopFaceDetect();
                }
                messageRouter.drainMatching(msg -> {
                    if (msg.startsWith("FaceDetectEvent#")) {
                        String parseRet = ZZWsResponseParser.parseResponse(msg);
                        log.debug("人脸比对结果: {}", parseRet);
                        command.setTransferData(parseRet);
                        performOperation(command);
                        if (parseRet.contains("成功")) {
                            stopFaceDetect();
                        }
                        return true;
                    }
                    return false;
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        return result;
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "StopFaceDetect")
    public String stopFaceDetect() {
        cameraStatus.get("faceDetect").set(false);
        messageRouter.drainMatching(msg -> msg.startsWith("FaceDetect#"));
        return "stop face detect success";
    }

    @DeviceOperation(DeviceType = "Camera", ProcessCommand = "GetFaceTemplFromBase64")
    public NantianCameraResponse getFaceTemplFromBase64(DeviceCommand command) {
        return send("GetFaceTemplFromBase64@" + command.getTransferData());
    }

    private NantianCameraResponse send(String message) {
        if (!lifecycleManager.isEnabled()) {
            return unavailable();
        }
        return responseMatcher.sendMessageGetResponse(message, responseTimeout, lifecycleManager.getSession());
    }

    private NantianCameraResponse unavailable() {
        return new NantianCameraResponse(500, "Internal Server Error", "Nantian camera service is disabled.");
    }
}
