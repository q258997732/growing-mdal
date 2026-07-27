plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "bob"
version = "1.0.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    // 阿里云镜像（首选）
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    // 清华大学镜像（备用）
    maven { url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
    mavenCentral()
    // jboss镜像
    maven { url = uri("https://repository.jboss.org/maven2/") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.awaitility:awaitility:4.2.0")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JNR dependencies
    implementation("com.github.jnr:jnr-ffi:2.2.13")
    // JNA
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    // Printer
    implementation("org.apache.pdfbox:pdfbox:3.0.1")
    // SNMP4J
    implementation("org.snmp4j:snmp4j:3.7.2")
    // Apache HttpClient 5 for K-RPA
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    include("bob/growingmdal/**")
                    exclude("bob/growingmdal/adapter/**")
                    exclude("bob/growingmdal/util/**")
                    exclude("bob/growingmdal/camera/CameraCommandExecutor.class")
                    exclude("bob/growingmdal/service/DekaService.class")
                    exclude("bob/growingmdal/service/LocalPrinterService.class")
                    exclude("bob/growingmdal/service/LexmarkPrinterService.class")
                }
            }
        )
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    include("bob/growingmdal/**")
                    exclude("bob/growingmdal/adapter/**")
                    exclude("bob/growingmdal/util/**")
                    exclude("bob/growingmdal/camera/CameraCommandExecutor.class")
                    exclude("bob/growingmdal/service/DekaService.class")
                    exclude("bob/growingmdal/service/LocalPrinterService.class")
                    exclude("bob/growingmdal/service/LexmarkPrinterService.class")
                }
            }
        )
    )
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.70)
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
}
