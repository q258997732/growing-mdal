plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "bob"
version = "0.0.1-SNAPSHOT"

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
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.projectlombok:lombok:1.18.30")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // JNR dependencies
    implementation("com.github.jnr:jnr-ffi:2.2.13")
    // JNA 4.5.1
    implementation("net.java.dev.jna:jna:4.5.1")
    implementation("net.java.dev.jna:jna-platform:4.5.1")
    // JAI
    implementation("com.sun.media:jai-codec:1.1.3")
    // Printer
    implementation("org.apache.pdfbox:pdfbox:3.0.1")
    // JavaCV + FFmpeg
    implementation("org.bytedeco:javacv-platform:1.5.9")
    // SNMP4J
    implementation("org.snmp4j:snmp4j:3.7.2")

}

tasks.withType<Test> {
    useJUnitPlatform()
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