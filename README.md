# growing-mdal

`growing-mdal` 是一个面向 AI 一体机的硬件控制网关服务，基于 Spring Boot 3 构建。它通过统一的命令协议（WebSocket + REST 双通道），将打印机、身份证读卡器、双目摄像头等外部设备能力，以及 Toptron 中控、K-RPA 客户端、文件上传等外部系统对接能力封装为标准接口，使 AI 中台无需关注厂商细节即可操作硬件或与外部系统交互。

- 语言：Java 17
- 框架：Spring Boot 3.5.0
- 构建：Gradle Kotlin DSL
- 组织/版本：`bob:1.0.3`

---

## 支持的硬件 / 外部系统

| 设备 / 系统 | DeviceType | 接入协议 | 说明 |
|------|------|------|------|
| 德卡 T10-MX4 身份证读卡器 | `IDCard` | JNR-FFI + 原生 DLL | 支持国内/外国人身份证读取 |
| 本地打印机 | `Printer` | Java 打印服务 | 根据文件路径或 Base64 打印 PDF |
| Lexmark 打印机 | `LexmarkPrinter` | SNMP v2c | 查询打印机状态 |
| 南天双目摄像头 | `Camera` | Jakarta WebSocket Client | 可见光 + 红外，支持拍照、人脸检测、视频流 |
| Toptron 中控 | `Toptron` | TCP Socket | 电源控制与原始十六进制报文发送 |
| K-RPA 客户端 | `Rpa` | HTTP/JSON（Apache HttpClient 5） | 组件调用、任务队列、Agent/流程/状态查询 |
| 文件上传服务 | `FileUpload` | 本地文件系统 + Base64 | 小文件上传、分块上传、MD5 检查 |

> 接入新硬件或外部系统时，只需新增一个 `HardwareCommandHandler` 实现并声明 `@DeviceOperation`，无需修改分发器。详见[接入新硬件/外部系统](#接入新硬件外部系统)。

---

## 技术栈

- Java 17
- Spring Boot 3.5.0（Web / WebSocket / Actuator / Validation）
- Gradle Kotlin DSL
- JNR-FFI 2.2.13（原生库调用）
- JNA 5.14.0
- Apache PDFBox 3.0.1（PDF 打印）
- SNMP4J 3.7.2（Lexmark 打印机）
- Apache HttpClient 5.4.4（K-RPA HTTP 对接）
- Jakarta WebSocket Client（摄像头）
- Jackson
- Lombok
- JUnit 5 / Mockito / AssertJ / Awaitility
- JaCoCo 0.8.11（覆盖率）

---

## 前置条件

1. JDK 17 或更高版本。仓库约定本地 JDK 路径为 `C:\Users\Administrator\.jdks\ms-17.0.19`。
2. 身份证读卡器：Windows x64/x86 环境，已安装德卡 T10-MX4 驱动。
3. 本地打印：操作系统已配置可用打印机。
4. Lexmark 打印机：网络可达的打印机 IP 与 SNMP community。
5. 南天摄像头：网络可达的摄像头 WebSocket 地址。
6. Toptron 中控：网络可达的中控主机 IP 与端口。
7. K-RPA：网络可达的 K-RPA 服务地址以及有效的用户名/密码。
8. 文件上传：应用对配置的 `file-upload.path` 目录具有读写权限。

构建前需将 `JAVA_HOME` 指向 Java 17：

```bash
export JAVA_HOME="/c/Users/Administrator/.jdks/ms-17.0.19"
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## 构建与运行

### 构建

```bash
./gradlew clean bootJar
```

打包后的 JAR 位于 `build/libs/growing-mdal-1.0.3.jar`。

### 运行

**Windows:**

```bat
run.bat
```

**Linux / macOS:**

```bash
./run.sh
```

或直接：

```bash
java -jar build/libs/growing-mdal-1.0.3.jar
```

也可以使用 Gradle 直接启动：

```bash
./gradlew bootRun
```

### 运行测试

```bash
./gradlew test
```

运行单个测试类：

```bash
./gradlew test --tests bob.growingmdal.GrowingMdalApplicationTests
```

生成覆盖率报告：

```bash
./gradlew jacocoTestReport
# 报告位于 build/reports/jacoco/test/html/index.html
```

当前 JaCoCo 覆盖率门槛为 **70%**（核心业务逻辑）。硬件相关代码（`adapter`、`util`、设备 Service、`CameraCommandExecutor`、`controller`）因依赖真实硬件或外部系统，已排除在覆盖率统计之外。

### 持续集成

项目包含 GitHub Actions 工作流 `.github/workflows/ci.yml`，每次 push/PR 会自动运行 `./gradlew clean build`。

---

## 架构概要

### 双通道：WebSocket + REST

所有硬件命令均支持 WebSocket；非流式命令额外提供 RESTful HTTP 入口。

```
客户端 WebSocket      客户端 HTTP
     │                    │
     ▼                    ▼
HardwareWebSocketHandler  HardwareCommandRestController
     │                    │
     └──────────┬─────────┘
                ▼
      CommandDispatcherService
                │
                ▼
      CommandRegistry + HandlerMethodInvoker
                │
                ▼
          设备 Service / Connector
```

- **WebSocket 入口：** `/hardware-ws`
  - 握手需携带 `X-API-Key` 与符合 `ALLOWED_ORIGINS` 的 `Origin`。
  - 入站消息由 `HardwareWebSocketHandler` 解析为 `DeviceCommand`，通过 `messageTaskExecutor` 线程池异步处理。
  - 同步结果包装为 `CommandResponse` 返回；持续结果（插卡提示、人脸检测、视频帧）通过 `OperationResultEvent` 推送。
- **REST 入口：** `/api/v1/hardware/{deviceType}/commands/{processCommand}`
  - `readOnly=true` 的命令使用 `GET`，其他使用 `POST`。
  - 由 `HardwareApiKeyFilter` 校验 `X-API-Key`。
  - 受 `adapter.rest.*` 开关控制；摄像头 REST 默认关闭。
  - 流式命令直接返回 `202 Accepted`，不执行硬件操作。

### 命令分发

1. `DeviceCommand` 包含 `Function`、`DeviceType`、`ProcessCommand`、`TransferData`。
2. `CommandRegistry` 启动时扫描所有 `HardwareCommandHandler` Bean，将 `@DeviceOperation(DeviceType, ProcessCommand, streaming, readOnly)` 注册为 `DeviceType:ProcessCommand -> HandlerMapping`。
3. `CommandDispatcherService.dispatch(command)` 找到匹配映射后，由 `HandlerMethodInvoker` 反射调用。
4. 方法签名仅支持 `()`、`(DeviceCommand)`、`(int)`、`(int, int, int)`；返回值通过 Jackson 序列化为 JSON 字符串，最终放入响应 `data` 字段。

### 流式隔离

- 服务通过 `ApplicationEventPublisher` 发布 `OperationResultEvent(session, result)`。
- `HardwareWebSocketHandler.handleOperationResult` 将事件交给 `WebSocketOutboundService`：
  - 命令响应/错误同步发送，保证顺序。
  - 流式事件提交到独立 `outboundTaskExecutor`，避免摄像头工作线程阻塞在网络 I/O。
  - 所有 outbound 发送均对 `WebSocketSession` 加锁，防止帧交错。
- `TimeSortedBufferQueue` 对摄像头二进制帧设置了容量上限，超限时丢弃最老帧。

---

## 配置说明

配置分为两部分：`application.properties`（Spring/WebSocket/安全/REST 开关）和 `adapter.properties`（设备专属参数）。部署时可通过外部 `./adapter.properties` 覆盖 classpath 默认值，无需重新打包。

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `HARDWARE_API_KEY` | WebSocket 握手与 REST 接口必须在 `X-API-Key` 请求头中携带的密钥 | `changeme` |
| `ALLOWED_ORIGINS` | 允许连接 `/hardware-ws` 的来源，多个用逗号分隔 | `http://localhost:8080` |
| `MANAGEMENT_PORT` | Actuator 健康检查端口 | `9090` |
| `DEKA_WORK_DIR` | 德卡 DLL 工作目录 | `${user.dir}/src/main/resources/lib/deka_T10-MX4_x64` |
| `DEKA_READER_USB_PORT` | 德卡读卡器 USB 端口 | `100` |
| `DEKA_READER_BAUD` | 德卡读卡器波特率 | `115200` |
| `DEKA_READER_WAIT_TIME` | 读卡等待时间（毫秒） | `30000` |
| `DEKA_READER_LOOP_PERIOD` | 读卡循环周期（毫秒） | `2000` |
| `PRINTER_LOCAL_NAME` | 本地打印机名称 | `Lexmark MS439dn` |
| `PRINTER_LOCAL_ALLOWED_DIR` | 本地打印允许的基础目录 | `${user.dir}/print-files` |
| `LEXMARK_IP` | Lexmark 打印机 IP | `192.168.107.112` |
| `LEXMARK_SNMP_PORT` | Lexmark SNMP 端口 | `161` |
| `LEXMARK_COMMUNITY` | Lexmark SNMP community | `public` |
| `LEXMARK_SNMP_TIMEOUT` | Lexmark SNMP 超时（毫秒） | `5000` |
| `LEXMARK_SNMP_RETRY` | Lexmark SNMP 重试次数 | `3` |
| `NANTIAN_URL` | 南天摄像头 WebSocket URL | `ws://192.168.107.103:7000` |
| `NANTIAN_CAMERA_ENABLE` | 是否启用南天摄像头 | `false` |
| `NANTIAN_CAMERA_AUTO_RECONNECT` | 摄像头断线后是否自动重连 | `false` |
| `NANTIAN_CAMERA_AUTO_RECONNECT_INTERVAL` | 自动重连间隔（毫秒） | `5000` |
| `NANTIAN_CAMERA_RESPONSE_TIMEOUT` | 摄像头响应超时（秒） | `3` |
| `NANTIAN_CAMERA_VIDEO_TIME` | 采集视频时间（毫秒） | `3000` |
| `NANTIAN_CAMERA_DETECT_TIME` | 人脸检测超时（秒） | `10` |
| `NANTIAN_MSG_CLEAN_INTERVAL` | 摄像头消息缓存清理间隔（毫秒） | `600000` |
| `NANTIAN_VIDEO_CLEAN_INTERVAL` | 摄像头视频缓存清理间隔（毫秒） | `600000` |
| `TOPTRON_HOST` | Toptron 中控 IP | `192.168.107.200` |
| `TOPTRON_PORT` | Toptron 中控端口 | `5000` |
| `TOPTRON_TOKEN` | Toptron 鉴权 Token | （空字符串） |
| `TOPTRON_CONNECT_TIMEOUT` | Toptron TCP 连接超时（毫秒） | `3000` |
| `RPA_HOST` | K-RPA 服务 IP | `192.168.107.100` |
| `RPA_PORT` | K-RPA 服务端口 | `80` |
| `RPA_USER` | K-RPA 用户名 | （空字符串） |
| `RPA_PASS` | K-RPA 密码 | （空字符串） |
| `RPA_CALLFUN_TIMEOUT` | K-RPA 调用超时（毫秒） | `5000` |
| `FILE_UPLOAD_PATH` | 文件上传保存目录 | `${user.dir}/uploads` |
| `REST_ENABLED` | REST 接口全局总开关 | `true` |
| `REST_IDCARD_ENABLED` | IDCard REST 开关 | `true` |
| `REST_PRINTER_ENABLED` | Printer REST 开关 | `true` |
| `REST_LEXMARK_PRINTER_ENABLED` | LexmarkPrinter REST 开关 | `true` |
| `REST_TOPTRON_ENABLED` | Toptron REST 开关 | `true` |
| `REST_RPA_ENABLED` | Rpa REST 开关 | `true` |
| `REST_FILE_UPLOAD_ENABLED` | FileUpload REST 开关 | `true` |
| `REST_CAMERA_ENABLED` | Camera REST 开关（默认关闭） | `false` |

### application.properties 示例

```properties
server.port=8080
hardware.api-key=${HARDWARE_API_KEY:changeme}
spring.websocket.allowed-origins=${ALLOWED_ORIGINS:http://localhost:8080}

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized
management.server.port=${MANAGEMENT_PORT:9090}

# REST 硬件命令接口开关
adapter.rest.enabled=${REST_ENABLED:true}
adapter.rest.idcard=${REST_IDCARD_ENABLED:true}
adapter.rest.printer=${REST_PRINTER_ENABLED:true}
adapter.rest.lexmark-printer=${REST_LEXMARK_PRINTER_ENABLED:true}
adapter.rest.toptron=${REST_TOPTRON_ENABLED:true}
adapter.rest.rpa=${REST_RPA_ENABLED:true}
adapter.rest.file-upload=${REST_FILE_UPLOAD_ENABLED:true}
adapter.rest.camera=${REST_CAMERA_ENABLED:false}
```

### adapter.properties 示例

```properties
adapter.printer-local-name=${PRINTER_LOCAL_NAME:Lexmark MS439dn}
adapter.printer-local-allowed-dir=${PRINTER_LOCAL_ALLOWED_DIR:${user.dir}/print-files}
adapter.printer-lexmark-ip=${LEXMARK_IP:192.168.107.112}
adapter.printer-lexmark-snmp-port=${LEXMARK_SNMP_PORT:161}
adapter.printer-lexmark-snmp-community=${LEXMARK_COMMUNITY:public}
adapter.printer-lexmark-snmp-timeout=${LEXMARK_SNMP_TIMEOUT:5000}
adapter.printer-lexmark-snmp-retry=${LEXMARK_SNMP_RETRY:3}

adapter.deka-work-dir=${DEKA_WORK_DIR:${user.dir}/src/main/resources/lib/deka_T10-MX4_x64}
adapter.deka-reader-usb-port=${DEKA_READER_USB_PORT:100}
adapter.deka-reader-baud=${DEKA_READER_BAUD:115200}
adapter.deka-reader-wait-time=${DEKA_READER_WAIT_TIME:30000}
adapter.deka-reader-loop-period=${DEKA_READER_LOOP_PERIOD:2000}

adapter.nantian-camera-url=${NANTIAN_URL:ws://192.168.107.103:7000}
adapter.nantian-camera-enable=${NANTIAN_CAMERA_ENABLE:false}
adapter.nantian-camera-auto-reconnect=${NANTIAN_CAMERA_AUTO_RECONNECT:false}
adapter.nantian-camera-auto-reconnect-interval=${NANTIAN_CAMERA_AUTO_RECONNECT_INTERVAL:5000}
adapter.nantian-camera-response-timeout=${NANTIAN_CAMERA_RESPONSE_TIMEOUT:3}
adapter.nantian-camera-video-time=${NANTIAN_CAMERA_VIDEO_TIME:3000}
adapter.nantian-camera-detect-time=${NANTIAN_CAMERA_DETECT_TIME:10}
adapter.nantian-msg-clean-interval=${NANTIAN_MSG_CLEAN_INTERVAL:600000}
adapter.nantian-video-clean-interval=${NANTIAN_VIDEO_CLEAN_INTERVAL:600000}

# Toptron 中控
adapter.toptron-host=${TOPTRON_HOST:192.168.107.200}
adapter.toptron-port=${TOPTRON_PORT:5000}
adapter.toptron-token=${TOPTRON_TOKEN:}
adapter.toptron-connect-timeout=${TOPTRON_CONNECT_TIMEOUT:3000}

# K-RPA 客户端
adapter.rpa-host=${RPA_HOST:192.168.107.100}
adapter.rpa-port=${RPA_PORT:80}
adapter.rpa-user=${RPA_USER:}
adapter.rpa-pass=${RPA_PASS:}
adapter.rpa-call-fun-timeout=${RPA_CALLFUN_TIMEOUT:5000}

# 文件上传服务
adapter.file-upload-path=${FILE_UPLOAD_PATH:${user.dir}/uploads}
```

---

## WebSocket API 协议

客户端连接 `ws://<host>:<port>/hardware-ws`，并在握手请求头中携带：

```
X-API-Key: <HARDWARE_API_KEY>
Origin: <ALLOWED_ORIGINS 中的某个来源>
```

### 请求消息格式

```json
{
  "Function": "InPut",
  "DeviceType": "IDCard",
  "ProcessCommand": "GetIDCardInfo",
  "TransferData": ""
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `Function` | string | 固定为 `InPut` 或 `OutPut` |
| `DeviceType` | string | 设备类型，如 `IDCard`、`Printer`、`Camera`、`LexmarkPrinter`、`Toptron`、`Rpa`、`FileUpload` |
| `ProcessCommand` | string | 操作命令，如 `GetIDCardInfo`、`PrintLocalPDF`、`PowerControl`、`CallComponent`、`Upload` |
| `TransferData` | string | 命令负载，通常为 JSON 字符串或 Base64/PDF 路径 |

### 响应消息格式

```json
{
  "function": "OutPut",
  "deviceType": "IDCard",
  "processCommand": "GetIDCardInfo",
  "success": true,
  "data": "{...}",
  "errorCode": null,
  "errorMessage": null
}
```

### 常用命令示例

#### 读取身份证

```json
{
  "Function": "InPut",
  "DeviceType": "IDCard",
  "ProcessCommand": "GetIDCardInfo",
  "TransferData": ""
}
```

#### 打印本地 PDF

```json
{
  "Function": "InPut",
  "DeviceType": "Printer",
  "ProcessCommand": "PrintLocalPDF",
  "TransferData": "/var/spool/print/document.pdf"
}
```

> `TransferData` 中的路径必须位于配置的允许目录内，禁止路径遍历。

#### Toptron 中控电源控制

```json
{
  "Function": "InPut",
  "DeviceType": "Toptron",
  "ProcessCommand": "PowerControl",
  "TransferData": "{\"gatePosition\":\"1\",\"turnon\":true}"
}
```

#### K-RPA 调用组件

```json
{
  "Function": "InPut",
  "DeviceType": "Rpa",
  "ProcessCommand": "CallComponent",
  "TransferData": "{\"script\":\"组件名\",\"params\":\"{}\",\"agentIp\":\"192.168.1.10\"}"
}
```

#### 文件上传

```json
{
  "Function": "InPut",
  "DeviceType": "FileUpload",
  "ProcessCommand": "Upload",
  "TransferData": "{\"name\":\"report.pdf\",\"contentBase64\":\"JVBERi0xLjQK...\"}"
}
```

> 分块上传使用 `UploadChunk`；单块原始数据建议控制在 75KB 以内，以避免超过 `TransferData` 长度限制。

---

## REST API 协议

非流式命令同时提供 RESTful 入口：

```
GET|POST /api/v1/hardware/{deviceType}/commands/{processCommand}
```

- 请求头必须携带 `X-API-Key`。
- `readOnly=true` 的命令使用 `GET`，可选 `?transferData=...` 查询参数。
- 其他命令使用 `POST`，请求体为 `{"transferData": "..."}`。
- 流式命令（如摄像头视频流）调用 REST 会返回 `202 Accepted`，提示改用 WebSocket。
- 摄像头 REST 默认关闭，需通过 `REST_CAMERA_ENABLED=true` 显式开启。

示例：

```bash
curl -H "X-API-Key: changeme" \
     "http://localhost:8080/api/v1/hardware/IDCard/commands/GetIDCardInfo"

curl -X POST -H "X-API-Key: changeme" -H "Content-Type: application/json" \
     -d '{"transferData":"{\"gatePosition\":\"1\",\"turnon\":true}"}' \
     "http://localhost:8080/api/v1/hardware/Toptron/commands/PowerControl"
```

> REST 命令当前为同步调用：RPA、DLL、打印等耗时操作会占用 Tomcat 工作线程，客户端需设置合理超时；高并发或长耗时场景建议使用 WebSocket。

---

## 安全说明

1. **认证**：`/hardware-ws` 握手与 `/api/v1/hardware/**` REST 接口均校验 `X-API-Key`。
2. **CORS**：`ALLOWED_ORIGINS` 必须配置为实际 AI 中台来源，生产环境禁止使用 `*`。
3. **路径遍历**：打印命令的 `TransferData` 会经过 `PathTraversalValidator` 校验，禁止跳出允许目录。
4. **PII 保护**：身份证姓名、身份证号、地址、照片、指纹等信息不会写入应用日志。
5. **错误信息**：客户端收到的错误消息为通用描述，详细堆栈仅记录服务端日志。
6. **配置安全**：敏感值（API Key、SNMP community、Toptron Token、K-RPA 用户名/密码）应通过环境变量注入，不要提交到版本控制。
7. **Actuator**：健康检查端点默认暴露在独立端口（默认 `9090`），仅暴露 `health`、`info`、`metrics`。

---

## 健康检查

启动后可通过以下地址查看健康状态：

```bash
curl http://localhost:9090/actuator/health
```

返回示例：

```json
{
  "status": "UP",
  "components": {
    "deka": { "status": "UP" },
    "printer": { "status": "UP" },
    "lexmark": { "status": "UP" },
    "camera": { "status": "DOWN", "details": { "reason": "not connected" } },
    "toptron": { "status": "UP" },
    "rpa": { "status": "UP" },
    "fileUpload": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 接入新硬件 / 外部系统

未来接入新硬件或外部系统时，推荐按以下步骤扩展：

1. 在 `src/main/java/bob/growingmdal/service/` 下新建 `XxxService`，继承 `AnnotationDrivenHandler`（或直接实现 `HardwareCommandHandler`）。
2. 实现 `getDeviceType()` 方法，返回唯一的设备类型字符串（如 `"BarcodeScanner"`）。
3. 实现 `supports(DeviceCommand)` 方法，用于分发器识别。
4. 如果硬件需要连接/断开/健康检查，让 `XxxService` 同时实现 `LifecycleManaged` 接口。
5. 如果硬件使用新的协议，在 `src/main/java/bob/growingmdal/connector/` 下新建 `XxxConnector`，将协议细节封装在内；已有参考：
   - `ToptronTcpConnector`（TCP Socket）
   - `RpaHttpConnector`（Apache HttpClient 5）
   - `FileUploadStore`（本地文件系统）
6. 为每个支持的操作添加 `@DeviceOperation(DeviceType = "...", ProcessCommand = "...", streaming = false, readOnly = false)` 注解的方法。
7. 方法参数仅支持 `()`、`(DeviceCommand)`、`(int)`、`(int, int, int)`；返回值会被 Jackson 自动序列化为 `data` 字段。
8. 新增单元测试 `XxxServiceTest`，验证命令映射、正常路径与异常路径。
9. 如需纳入健康检查，新增 `XxxHealthIndicator` 并注册为 Spring Bean。
10. 如需通过 REST 暴露，将新设备类型加入 `RestEndpointProperties` 的开关列表。
11. 无需修改 `CommandDispatcherService` —— 启动时 `CommandRegistry` 会自动发现并注册新的 `@DeviceOperation`。

---

## 目录结构

```
growing-mdal
├── src/main/java/bob/growingmdal/
│   ├── adapter/          # 设备适配器（DLL/SNMP 等）
│   ├── annotation/       # @DeviceOperation
│   ├── camera/           # 南天摄像头组件（生命周期、消息路由、命令执行、缓冲队列）
│   ├── config/           # Spring 配置（WebSocket、线程池、Adapter 属性、REST 开关、优雅关闭）
│   ├── connector/        # 外部系统连接器（TCP/HTTP/文件系统）
│   ├── controller/       # REST 入口（HardwareCommandRestController）
│   ├── core/             # 命令模型、分发器、参数绑定、异常
│   ├── dto/              # 请求/响应 DTO（RPA、文件上传、硬件命令）
│   ├── entity/           # 实体、事件、响应对象、缓冲队列
│   ├── handler/          # WebSocket 处理器
│   ├── hardware/         # 硬件连接与生命周期抽象
│   ├── health/           # 健康检查指示器
│   ├── security/         # 认证拦截器、API Key 过滤器、路径校验
│   ├── service/          # 设备服务（Deka/Printer/Lexmark/Toptron/Rpa/FileUpload 等）
│   ├── util/             # 工具类（Base64、SNMP、DLL 加载、RpaUtil 等）
│   └── validation/       # 输入校验
├── src/main/resources/
│   ├── lib/              # 原生 DLL
│   ├── adapter.properties
│   └── application.properties
├── src/test/java/        # 单元测试与集成测试
├── build.gradle.kts
├── run.bat
├── run.sh
└── README.md
```

---

## 已知限制 / 注意事项

- `DekaService` 的 DLL 工作目录基于 `user.dir + "/src/main/resources/lib/deka_T10-MX4_x64"`，只有在项目根目录启动时才有效。
- 文件上传元数据与分块索引保存在内存中，服务重启后丢失。
- REST 命令当前为同步调用：耗时操作（RPA、DLL、打印）会占用 Tomcat 工作线程，客户端需设置合理超时。
- 流式事件由多线程 outbound 池发送，顺序为尽力保证，极端背压下会丢帧。
- 摄像头 REST 接口默认关闭，流式命令（视频流、人脸检测）只能通过 WebSocket 使用。
- 仓库中存在几个探索性/占位测试文件（`PinadCxxJNRTest`、`PinpadCJNR`、`TestTmp`），不是真正的带断言 JUnit 测试。

---

## 常见问题

### Q1: 身份证读卡器提示 DLL 加载失败

- 确认 `DEKA_WORK_DIR` 指向的目录存在 `dcrf32.dll`。
- 确认操作系统位数与 DLL 位数一致（x64 应用使用 x64 DLL）。
- 确认德卡驱动已安装。
- 确认应用是从项目根目录启动（`user.dir` 指向项目根）。

### Q2: 本地打印提示找不到打印机

- 检查 `PRINTER_LOCAL_NAME` 是否与操作系统中配置的打印机名称完全一致。
- 在控制面板 / 系统设置中确认打印机处于就绪状态。

### Q3: 南天摄像头无法连接

- 检查 `NANTIAN_URL` 是否可达。
- 检查摄像头是否已通电并启动 WebSocket 服务。
- 查看日志中的连接错误与自动重连状态。

### Q4: WebSocket 连接被 403 拒绝

- 检查 `X-API-Key` 请求头是否与 `HARDWARE_API_KEY` 一致。
- 检查 `Origin` 是否在 `ALLOWED_ORIGINS` 列表中。

### Q5: REST 接口返回 503

- 检查 `REST_ENABLED` 是否为 `true`。
- 检查对应设备类型的 `REST_*_ENABLED` 开关是否打开（摄像头默认关闭）。

### Q6: REST 接口返回 202 Accepted

- 该命令为流式命令（如摄像头视频流），REST 不执行实际操作，请改用 WebSocket `/hardware-ws`。

### Q7: 健康检查显示摄像头 DOWN

- 当 `NANTIAN_CAMERA_ENABLE=false` 时，摄像头健康检查预期为 `DOWN`。
- 当启用后仍 DOWN，请检查网络连接与摄像头服务状态。

### Q8: Toptron 中控命令返回 false

- 检查 `TOPTRON_HOST` 和 `TOPTRON_PORT` 是否可达。
- 检查 `TOPTRON_TOKEN` 是否与中控配置一致。
- 查看服务端日志中的 TCP 连接错误。

### Q9: K-RPA 调用返回 false

- 检查 `RPA_HOST` 和 `RPA_PORT` 是否可达。
- 检查 `RPA_USER` 和 `RPA_PASS` 是否正确。
- 检查被调用的组件名/流程名/Agent IP 是否在 K-RPA 服务端存在。

### Q10: 文件上传失败或返回 error

- 检查 `FILE_UPLOAD_PATH` 目录是否存在且应用进程有读写权限。
- 单条 `TransferData` 不宜过大，大文件请使用 `UploadChunk` 分块上传。

---

## 许可证

本项目为内部项目，未经许可不得对外发布。
