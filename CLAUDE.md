# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在操作本仓库代码时提供指引。

## 项目概述

`growing-mdal` 是一个 Spring Boot 3 服务，通过单一的 WebSocket 端点控制连接在 AI 一体机上的外部硬件设备，并对接外部系统。它在统一的命令协议下集成了打印机、身份证读卡器、双目摄像头、Toptron 中控、K-RPA 客户端和文件上传服务，使 AI 中台无需关注厂商特定细节即可操作设备或与外部系统交互。

- 语言：Java 17
- 构建工具：Gradle Kotlin DSL（`build.gradle.kts`）
- 框架：Spring Boot 3.5.0
- 组织/版本：`bob:1.0.3`

## 常用命令

构建项目：

```bash
./gradlew build
```

运行应用（使用 Spring Boot 插件）：

```bash
./gradlew bootRun
```

运行全部测试：

```bash
./gradlew test
```

运行单个测试类：

```bash
./gradlew test --tests bob.growingmdal.GrowingMdalApplicationTests
```

运行单个测试方法：

```bash
./gradlew test --tests bob.growingmdal.GrowingMdalApplicationTests.contextLoads
```

清理并重新构建：

```bash
./gradlew clean build
```

构建时使用阿里云和清华大学 Maven 镜像作为主要仓库；`mavenCentral()` 和 JBoss 作为备用。

## 架构

### 命令分发流程

客户端连接 `/hardware-ws` 并发送 JSON 命令，服务端将命令路由到对应的设备服务。

```
客户端 WebSocket -> HardwareWebSocketHandler -> CommandDispatcherService
                                                       |
                                                       v
                                            AnnotationDrivenHandler
                                                       |
                                                       v
                                                 设备服务
```

1. `HardwareWebSocketHandler` 校验 JSON，将其解析为 `DeviceCommand`，然后调用 `CommandDispatcherService.dispatch`。
   - 注意：`processCommand` 等于字符串 `"Camera"` 的命令会在分发前被 `isCameraCommand` 静默丢弃。
2. `CommandDispatcherService` 选择第一个 `supports(DeviceCommand)` 返回 `true` 的 `HardwareCommandHandler`。
3. 每个设备服务都继承 `AnnotationDrivenHandler`，该基类通过反射查找带有 `@DeviceOperation(DeviceType = "...", ProcessCommand = "...")` 注解的方法并调用匹配项。
4. `AnnotationDrivenHandler` 将 `TransferData` 设置为 `result.toString()` 并返回 `command.toString()`。如果需要结构化 JSON 输出，服务必须在返回前自行序列化。

### 设备服务

| 服务 | 设备类型（DeviceType） | 职责 |
|---|---|---|
| `DekaService` | `IDCard` | 通过德卡 T10-MX4 读卡器 DLL（`dcrf32.dll`）读取国内/外国人身份证。 |
| `LocalPrinterService` | `Printer` | 使用本地 Java 打印服务，根据文件路径或 Base64 负载打印 PDF。 |
| `LexmarkPrinterService` | `LexmarkPrinter` | 通过 SNMP 查询 Lexmark 打印机状态。 |
| `NantianCameraService` | `Camera` | 通过 Jakarta WebSocket 客户端连接控制南天双目（可见光 + 红外）摄像头。 |
| `ToptronControlService` | `Toptron` | 通过 TCP Socket 发送十六进制报文控制 Toptron 中控。 |
| `RpaClientService` | `Rpa` | 通过 Apache HttpClient 5 调用 K-RPA 服务。 |
| `FileUploadService` | `FileUpload` | 接收 Base64 文件数据并保存到本地文件系统，支持分块上传。 |

`supports` 方法是扩展点：新增设备服务必须对自身的 `deviceType` 返回 `true`，才会被分发器纳入。每个 `deviceType` 必须只被一个服务声明 —— `CommandDispatcherService` 使用 `findFirst()`，若出现重叠声明将静默依赖 Spring 的 Bean 顺序。

### 异步结果

部分操作会持续向客户端回传中间结果（例如身份证插卡提示、人脸检测事件、视频帧）。这些结果通过 Spring 的 `ApplicationEventPublisher` 发出：

- 服务代码发布 `OperationResultEvent(session, result)`。
- `HardwareWebSocketHandler.handleOperationResult` 监听该事件，并将负载转发给原始会话。

这意味着处理函数可能先返回一个初始响应，后续数据再通过事件通道到达。

### 本地库

设备 DLL 打包在 `src/main/resources/lib/` 下：

- `deka_T10-MX4_x64/` 和 `deka_T10-MX4_x86/` —— 德卡读卡器 SDK
- `jieyu_D10/` —— 另一套摄像头/读卡器 SDK
- `yk_j80_x64/` 和 `yk_j80_x86/` —— 另一套设备 SDK

`NativeLibraryLoader` 在运行时将所需 DLL 解压到临时目录，并通过 JNR-FFI 加载。接口（如 `DekaReaderAdapter`）定义了 JNR 映射到 DLL 的 C 函数签名。

### 外部系统连接器

非 DLL/SNMP 类的外部系统对接逻辑放在 `src/main/java/bob/growingmdal/connector/` 下：

- `ToptronTcpConnector`：通过 TCP Socket 与 Toptron 中控通信，发送电源控制或自定义十六进制报文。
- `RpaHttpConnector`：基于 Apache HttpClient 5 连接池与 K-RPA 服务通信，发送 `CallFunc.aom` 请求。
- `FileUploadStore`：管理本地文件上传目录，维护内存元数据索引，负责小文件写入、分块合并与 MD5 检查。

这些 Connector 由对应 Service 注入，生命周期随 Spring Bean 管理；`RpaHttpConnector` 在销毁时通过 `@PreDestroy` 关闭连接池。

### 配置

- `application.properties` —— Spring/WebSocket 设置，包含 `spring.websocket.allowed-origins=*`。
- `adapter.properties` —— 设备专属设置（打印机名称、德卡超时、南天摄像头地址、启用开关，以及 Toptron、K-RPA、文件上传参数）。
- `AdapterConfig` 先加载 `classpath:adapter.properties`，再加载外部 `./adapter.properties`（如果存在），因此部署时无需重新打包即可覆盖默认配置。

新增外部系统配置：

- `adapter.toptron.host` / `port` / `token` / `connect-timeout`：Toptron 中控连接参数。
- `adapter.rpa.host` / `port` / `user` / `pass` / `call-fun-timeout`：K-RPA 服务连接参数。
- `adapter.file-upload.path` / `max-transfer-data-length`：文件上传保存目录与单条消息大小限制。

重要运行时开关：

- `nantian.camera.enable=false` 不会阻止 Bean 加载或声明 `deviceType=Camera`；摄像头操作会返回一个 `500` 的 `NantianCameraResponse`，而不会真正操作硬件。
- `nantian.camera.auto.reconnect=false` 禁用摄像头服务的自动重连。

## 新增设备/外部系统操作

要新增一条命令支持：

1. 创建或扩展一个继承 `AnnotationDrivenHandler` 并标注 `@Service` 的服务。
2. 实现 `supports(DeviceCommand)`，使其匹配对应设备的 `deviceType`。
3. 添加带有 `@DeviceOperation(DeviceType = "...", ProcessCommand = "...")` 注解的方法。
4. 方法参数可以为空或仅接受一个 `DeviceCommand` 参数。返回结果对象；`AnnotationDrivenHandler` 会调用 `result.toString()` 并放入 `TransferData`。若需要结构化输出，请自行在返回前完成 JSON 序列化。
5. 如需与外部系统通信，在 `src/main/java/bob/growingmdal/connector/` 下新建 Connector，由 Service 注入使用。参考：`ToptronTcpConnector`、`RpaHttpConnector`、`FileUploadStore`。

## 注意事项

- **每条消息单独线程：** `HardwareWebSocketHandler.handleTextMessage` 为每条入站消息都创建一个原生 `new Thread(...)`。`ThreadPoolConfig` 存在，但目前未用于消息分发。
- **硬编码开发路径：** `DekaService` 的 DLL 工作目录通过 `user.dir + "/src/main/resources/lib/deka_T10-MX4_x64"` 构建，只有在开发环境下从项目根目录启动时才有效。
- **编码：** `build.gradle.kts` 强制编译、测试和 `JavaExec` 任务使用 UTF-8。
- **Lombok：** 项目中大量使用 Lombok 注解（`@Slf4j`、`@Getter` 等），构建时必须启用 Lombok 支持。
- **WebSocket 缓冲区：** `WebSocketConfig` 设置文本/二进制消息缓冲区为 2 MB，空闲超时为 30 分钟。
- **外部系统敏感配置：** Toptron Token、K-RPA 用户名/密码通过 `AdapterProperties` 从环境变量注入，禁止硬编码。
- **文件上传限制：** `CommandValidator.MAX_TRANSFER_DATA_LENGTH` 已调整为 100,000，支持约 75KB 原始数据；超过此大小请使用 `UploadChunk` 分块上传。文件元数据与分块索引保存在内存中，服务重启后丢失。

## 测试说明

- 测试使用 JUnit 5（`useJUnitPlatform()`）。
- 仓库中除了生成的 `GrowingMdalApplicationTests`，还有几个探索性/占位测试文件（`PinadCxxJNRTest`、`PinpadCJNR`、`TestTmp`）。这些文件本质上是临时草稿或 main 方法风格代码，不是真正的带断言 JUnit 测试。
- 新增外部系统服务的单元测试：`ToptronControlServiceTest`、`RpaClientServiceTest`、`FileUploadServiceTest`、`FileUploadStoreTest`、`RpaUtilTest`。
- 涉及本地 DLL、外部硬件或真实外部服务的测试，只有在外部设备/服务已连接并可访问时才能通过。
