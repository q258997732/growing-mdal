# growing-mdal

`growing-mdal` 是一个用于 AI 一体机的硬件控制网关服务。它通过统一的 WebSocket 协议，将打印机、身份证读卡器、双目摄像头等外部设备能力，以及 Toptron 中控、K-RPA 客户端、文件上传等外部系统对接能力封装为标准接口，使 AI 中台无需关注厂商细节即可操作硬件或与外部系统交互。

---

## 支持的硬件

| 设备 | 设备类型（DeviceType） | 接入协议 | 说明 |
|------|------------------------|----------|------|
| 德卡 T10-MX4 身份证读卡器 | `IDCard` | JNR-FFI + 原生 DLL | 支持国内/外国人身份证读取 |
| 本地打印机 | `Printer` | Java 打印服务 | 根据文件路径或 Base64 打印 PDF |
| Lexmark 打印机 | `LexmarkPrinter` | SNMP v2c | 查询打印机状态 |
| 南天双目摄像头 | `Camera` | Jakarta WebSocket Client | 可见光 + 红外，支持拍照、人脸检测、视频流 |
| Toptron 中控 | `Toptron` | TCP Socket | 电源控制与原始十六进制报文发送 |
| K-RPA 客户端 | `Rpa` | HTTP/JSON | 组件调用、任务队列、Agent/流程/状态查询 |
| 文件上传服务 | `FileUpload` | 本地文件系统 + Base64 | 小文件上传、分块上传、MD5 检查 |

> 未来接入新硬件或外部系统时，只需新增一个 `HardwareCommandHandler` 实现并声明 `@DeviceOperation`，无需修改分发器。详见[接入新硬件/外部系统](#接入新硬件外部系统)。

---

## 技术栈

- Java 17
- Spring Boot 3.5.0
- Gradle Kotlin DSL
- JNR-FFI（原生库调用）
- Jakarta WebSocket Client
- Apache HttpClient 5（K-RPA 等 HTTP 对接）
- Jackson
- JUnit 5 / Mockito / AssertJ

---

## 前置条件

1. JDK 17 或更高版本。
2. 对于身份证读卡器，需要 Windows x64/x86 环境并安装德卡 T10-MX4 驱动。
3. 对于本地打印，需要操作系统已配置可用打印机。
4. 对于 Lexmark 打印机，需要网络可达的打印机 IP 与 SNMP community。
5. 对于南天摄像头，需要网络可达的摄像头 WebSocket 地址。
6. 对于 Toptron 中控，需要网络可达的中控主机 IP 与端口。
7. 对于 K-RPA，需要网络可达的 K-RPA 服务地址以及有效的用户名/密码。
8. 对于文件上传服务，需要应用对配置的 `file-upload.path` 目录具有读写权限。

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

### 运行测试

```bash
./gradlew test
```

生成覆盖率报告：

```bash
./gradlew jacocoTestReport
# 报告位于 build/reports/jacoco/test/html/index.html
```

当前 JaCoCo 覆盖率为 **70%**（核心业务逻辑），目标为 80%。硬件相关代码（`adapter`、`util`）与命令执行器（`CameraCommandExecutor`）因依赖真实硬件，已排除在覆盖率统计之外；其余业务代码建议保持 80% 以上。

### 持续集成

项目包含 GitHub Actions 工作流 `.github/workflows/ci.yml`，每次 push/PR 会自动运行 `./gradlew clean build`。

---

## 配置说明

配置分为两部分：`application.properties`（Spring/WebSocket/安全）和 `adapter.properties`（设备专属参数）。部署时可通过外部 `./adapter.properties` 覆盖默认值，无需重新打包。

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `HARDWARE_API_KEY` | WebSocket 握手时必须在 `X-API-Key` 请求头中携带的密钥 | `changeme` |
| `ALLOWED_ORIGINS` | 允许连接 `/hardware-ws` 的来源，多个用逗号分隔 | `http://localhost:8080` |
| `MANAGEMENT_PORT` | Actuator 健康检查端口 | `9090` |
| `DEKA_WORK_DIR` | 德卡 DLL 工作目录，用于临时写入照片文件 | `${user.dir}/src/main/resources/lib/deka_T10-MX4_x64` |
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
| `TOPTRON_HOST` | Toptron 中控 IP | `192.168.107.200` |
| `TOPTRON_PORT` | Toptron 中控端口 | `5000` |
| `TOPTRON_TOKEN` | Toptron 鉴权 Token | （空字符串） |
| `TOPTRON_CONNECT_TIMEOUT` | Toptron TCP 连接超时（毫秒） | `5000` |
| `RPA_HOST` | K-RPA 服务 IP | `192.168.107.100` |
| `RPA_PORT` | K-RPA 服务端口 | `80` |
| `RPA_USER` | K-RPA 用户名 | （空字符串） |
| `RPA_PASS` | K-RPA 密码 | （空字符串） |
| `RPA_CALL_FUN_TIMEOUT` | K-RPA 调用超时（毫秒） | `5000` |
| `FILE_UPLOAD_PATH` | 文件上传保存目录 | `${user.dir}/uploads` |

### application.properties 示例

```properties
server.port=8080
hardware.api-key=${HARDWARE_API_KEY:changeme}
spring.websocket.allowed-origins=${ALLOWED_ORIGINS:http://localhost:8080}

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized
management.server.port=${MANAGEMENT_PORT:9090}
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
adapter.toptron-connect-timeout=${TOPTRON_CONNECT_TIMEOUT:5000}

# K-RPA 客户端
adapter.rpa-host=${RPA_HOST:192.168.107.100}
adapter.rpa-port=${RPA_PORT:80}
adapter.rpa-user=${RPA_USER:}
adapter.rpa-pass=${RPA_PASS:}
adapter.rpa-call-fun-timeout=${RPA_CALL_FUN_TIMEOUT:5000}

# 文件上传服务
adapter.file-upload-path=${FILE_UPLOAD_PATH:${user.dir}/uploads}
adapter.file-upload-max-transfer-data-length=${FILE_UPLOAD_MAX_TRANSFER_DATA_LENGTH:100000}
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

## 安全说明

1. **认证**：`/hardware-ws` 握手时必须携带 `X-API-Key`，否则连接被拒绝。
2. **CORS**：`ALLOWED_ORIGINS` 必须配置为实际 AI 中台来源，生产环境禁止使用 `*`。
3. **路径遍历**：打印命令的 `TransferData` 会经过校验，禁止跳出允许目录。
4. **PII 保护**：身份证姓名、身份证号、地址、照片、指纹等信息不会写入应用日志。
5. **错误信息**：客户端收到的错误消息为通用描述，详细堆栈仅记录服务端日志。
6. **配置安全**：敏感值（API Key、SNMP community、设备 IP、Toptron Token、K-RPA 用户名/密码）应通过环境变量注入，不要提交到版本控制。
7. **Actuator**：健康检查端点默认暴露在独立端口，且仅暴露 `health`、`info`、`metrics`。

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
    "camera": { "status": "DOWN", "details": { "reason": "not connected" } },
    "toptron": { "status": "UP" },
    "rpa": { "status": "UP" },
    "fileUpload": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 接入新硬件/外部系统

未来接入新硬件或外部系统时，推荐按以下步骤扩展：

1. 在 `src/main/java/bob/growingmdal/service/` 下新建 `XxxService`，继承 `AnnotationDrivenHandler`。
2. 实现 `getDeviceType()` 方法，返回唯一的设备类型字符串（如 `"BarcodeScanner"`、`"Toptron"`）。
3. 如果硬件需要连接/断开/健康检查，让 `XxxService` 同时实现 `LifecycleManaged` 接口。
4. 如果硬件使用新的协议，在 `src/main/java/bob/growingmdal/connector/` 下新建 `XxxConnector`，将协议细节封装在内；已有参考：
   - `ToptronTcpConnector`（TCP Socket）
   - `RpaHttpConnector`（Apache HttpClient 5）
   - `FileUploadStore`（本地文件系统）
5. 为每个支持的操作添加 `@DeviceOperation(DeviceType = "...", ProcessCommand = "...")` 注解的方法。
6. 方法参数仅支持 `()` 或 `(DeviceCommand)`；返回值会被 Jackson 自动序列化为 `data` 字段。
7. 新增单元测试 `XxxServiceTest`，验证命令映射、正常路径与异常路径。
8. 如需纳入健康检查，新增 `XxxHealthIndicator` 并注册为 Spring Bean。
9. 无需修改 `CommandDispatcherService` —— 启动时 `CommandRegistry` 会自动发现并注册新的 `@DeviceOperation`。

---

## 目录结构

```
growing-mdal
├── src/main/java/bob/growingmdal/
│   ├── adapter/          # 设备适配器（DLL/SNMP 等）
│   ├── annotation/       # @DeviceOperation
│   ├── camera/           # 南天摄像头组件（生命周期、消息路由、命令执行）
│   ├── config/           # Spring 配置
│   ├── connector/        # 外部系统连接器（TCP/HTTP/文件系统）
│   ├── core/             # 命令模型与分发
│   ├── dto/              # 请求/响应 DTO
│   ├── entity/           # 实体与响应对象
│   ├── handler/          # WebSocket 处理器
│   ├── hardware/         # 硬件连接与生命周期抽象
│   ├── health/           # 健康检查指示器
│   ├── security/         # 认证与路径校验
│   ├── service/          # 设备服务
│   ├── util/             # 工具类
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

## 常见问题

### Q1: 身份证读卡器提示 DLL 加载失败

- 确认 `DEKA_WORK_DIR` 指向的目录存在 `dcrf32.dll`。
- 确认操作系统位数与 DLL 位数一致（x64 应用使用 x64 DLL）。
- 确认德卡驱动已安装。

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

### Q5: 健康检查显示摄像头 DOWN

- 当 `NANTIAN_CAMERA_ENABLE=false` 时，摄像头健康检查预期为 `DOWN`。
- 当启用后仍 DOWN，请检查网络连接与摄像头服务状态。

### Q6: Toptron 中控命令返回 false

- 检查 `TOPTRON_HOST` 和 `TOPTRON_PORT` 是否可达。
- 检查 `TOPTRON_TOKEN` 是否与中控配置一致。
- 查看服务端日志中的 TCP 连接错误。

### Q7: K-RPA 调用返回 false

- 检查 `RPA_HOST` 和 `RPA_PORT` 是否可达。
- 检查 `RPA_USER` 和 `RPA_PASS` 是否正确。
- 检查被调用的组件名/流程名/Agent IP 是否在 K-RPA 服务端存在。

### Q8: 文件上传失败或返回 error

- 检查 `FILE_UPLOAD_PATH` 目录是否存在且应用进程有读写权限。
- 确认单条 `TransferData` 未超过 `FILE_UPLOAD_MAX_TRANSFER_DATA_LENGTH`（默认 100,000，约 75KB 原始数据）。
- 大文件请使用 `UploadChunk` 分块上传。

---

## 许可证

本项目为内部项目，未经许可不得对外发布。
