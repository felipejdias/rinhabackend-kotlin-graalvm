# Build stage: GraalVM for JDK 25 (traz o native-image). O contexto do build e a raiz do
# projeto (ver docker-compose.yml) para usar o Gradle Wrapper com a versao fixada.
FROM container-registry.oracle.com/graalvm/native-image:25 AS build

WORKDIR /usr/src/rinha-backend

COPY . .

RUN chmod +x ./gradlew && ./gradlew :rinha-backend-kotlin:nativeCompile --no-daemon

# Runtime stage: o binario nativo nao precisa de JVM, so da mesma glibc do builder (Oracle Linux 10)
FROM container-registry.oracle.com/os/oraclelinux:10-slim

COPY --from=build /usr/src/rinha-backend/rinha-backend-kotlin/build/native/nativeCompile/rinha-backend-2024-q1 /rinha-backend/rinha-backend-2024-q1

WORKDIR /rinha-backend

EXPOSE 8080
ENTRYPOINT ["/rinha-backend/rinha-backend-2024-q1"]
