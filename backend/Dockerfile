# 方案：本地 Maven 构建 JAR，Docker 只负责打包运行环境
# 前提：在 backend/ 目录下先执行 mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
