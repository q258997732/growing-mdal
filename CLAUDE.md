# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`growing-mdal` 是一个 Spring Boot 3 硬件控制网关服务，用于 AI 一体机。它通过统一的命令协议，将打印机、身份证读卡器、双目摄像头、Toptron 中控、K-RPA 客户端和文件上传服务封装为对外接口，使 AI 中台无需关注厂商特定细节即可操作硬件或与外部系统交互。

- 语言：Java 17
- 构建工具：Gradle Kotlin DSL（`build.gradle.kts`）
- 框架：Spring Boot 3.5.0
- 组织/版本：`bob:1.0.3`
- 本地 JDK 路径：`C:\Users\Administrator\.jdks\ms-17.0.19`

## Environment Setup

构建前必须将 `JAVA_HOME` 指向 Java 17 JDK（仓库已约定使用上述本地路径）：

```bash
export JAVA_HOME="/c/Users/Administrator/.jdks/ms-17.0.19"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Common Commands

构建项目：

```bash
./gradlew build
```

清理并重新构建：

```bash
./gradlew clean build
```

运行应用：

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

生成覆盖率报告：

```bash
./gradlew jacocoTestReport
# 报告位于 build/reports/jacoco/test/html/index.html
```

构建使用阿里云和清华大学 Maven 镜像作为主要仓库，`mavenCentral()` 和 JBoss 作为备用。

## High-Level Architecture

### Dual Transport: WebSocket + REST

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

### Command Dispatch

1. `DeviceCommand` 包含 `Function`、`DeviceType`、`ProcessCommand`、`TransferData`。
2. `CommandRegistry` 启动时扫描所有 `HardwareCommandHandler` Bean，将 `@DeviceOperation(DeviceType, ProcessCommand, streaming, readOnly)` 注册为 `DeviceType:ProcessCommand -> HandlerMapping`。
3. `CommandDispatcherService.dispatch(command)` 找到匹配映射后，由 `HandlerMethodInvoker` 反射调用。
4. 方法签名仅支持 `()`、`(DeviceCommand)`、`(int)`、`(int, int, int)`；返回值通过 Jackson 序列化为 JSON字符串，最终放入响应 `data` 字段。

### Streaming Isolation

- 服务通过 `ApplicationEventPublisher` 发布 `OperationResultEvent(session, result)`。
- `HardwareWebSocketHandler.handleOperationResult` 将事件交给 `WebSocketOutboundService`：
  - 命令响应/错误同步发送，保证顺序。
  - 流式事件提交到独立 `outboundTaskExecutor`，避免摄像头工作线程阻塞在网络 I/O。
  - 所有 outbound 发送均对 `WebSocketSession` 加锁，防止帧交错。
- `TimeSortedBufferQueue` 对摄像头二进制帧设置了容量上限，超限时丢弃最老帧。

### Device Services

| 服务 | DeviceType | 接入方式 |
|---|---|---|
| `DekaService` | `IDCard` | JNR-FFI + 德卡 DLL |
| `LocalPrinterService` | `Printer` | Java 打印服务 |
| `LexmarkPrinterService` | `LexmarkPrinter` | SNMP v2c |
| `CameraCommandExecutor` | `Camera` | Jakarta WebSocket Client |
| `ToptronControlService` | `Toptron` | TCP Socket |
| `RpaClientService` | `Rpa` | Apache HttpClient 5 |
| `FileUploadService` | `FileUpload` | 本地文件系统 + Base64 |

新增设备：继承 `AnnotationDrivenHandler`（或直接实现 `HardwareCommandHandler`），实现 `getDeviceType()`/`supports()`，添加 `@DeviceOperation` 方法。若需协议封装，在 `connector/` 下新建 Connector。无需修改分发器，`CommandRegistry` 会自动发现。

### Configuration

- `application.properties`：Spring/WebSocket/安全/REST 开关。
- `adapter.properties`：设备专属参数；外部 `./adapter.properties` 会覆盖 classpath 默认值。
- 敏感配置（API Key、SNMP community、Toptron Token、K-RPA 密码）通过环境变量注入，禁止硬编码。

新增 REST 开关示例：

```properties
adapter.rest.enabled=true
adapter.rest.idcard=true
adapter.rest.camera=false
```

## Testing Notes

- 测试使用 JUnit 5 + Mockito + AssertJ。
- `./gradlew clean build` 会执行测试与 JaCoCo 覆盖率校验（当前最低 70%）。
- 涉及本地 DLL、真实硬件或外部服务的测试，只有在外部资源可访问时才能通过。
- 仓库中存在几个探索性/占位测试文件（`PinadCxxJNRTest`、`PinpadCJNR`、`TestTmp`），不是真正的带断言 JUnit 测试。

## Security & Operations

- `/hardware-ws` 握手和 `/api/v1/hardware/**` REST 接口均校验 `X-API-Key`。
- `ALLOWED_ORIGINS` 生产环境禁止配置为 `*`。
- 健康检查端点默认暴露在独立管理端口（默认 `9090`），仅暴露 `health`、`info`、`metrics`。
- 身份证 PII 信息不会写入应用日志。

## Important Caveats

- `DekaService` 的 DLL 工作目录基于 `user.dir + "/src/main/resources/lib/deka_T10-MX4_x64"`，只有在项目根目录启动时才有效。
- 文件上传元数据与分块索引保存在内存中，服务重启后丢失。
- REST 命令当前为同步调用：耗时操作（RPA、DLL、打印）会占用 Tomcat 工作线程，客户端需设置合理超时。
- 流式事件由多线程 outbound 池发送，顺序为尽力保证，极端背压下会丢帧。
