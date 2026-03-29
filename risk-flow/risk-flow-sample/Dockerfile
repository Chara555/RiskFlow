# RiskFlow Sample Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制 pom.xml 和源码
COPY pom.xml .
COPY risk-flow-core ./risk-flow-core
COPY risk-flow-sdk-starter ./risk-flow-sdk-starter
COPY risk-flow-sample ./risk-flow-sample

# 下载依赖
RUN mvn dependency:go-offline -B

# 编译
RUN mvn package -pl risk-flow-sample -am -DskipTests

# 运行镜像
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/risk-flow-sample/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
