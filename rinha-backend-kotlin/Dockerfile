FROM gradle:8.4.0-jdk21-graal AS build

RUN mkdir -p /usr/src/rinha-backend

COPY . /usr/src/rinha-backend

WORKDIR /usr/src/rinha-backend

RUN gradle nativeCompile

# Use uma imagem base com GraalVM e Java 21 (ajuste a tag conforme necessário)
FROM container-registry.oracle.com/graalvm/native-image:21

COPY --from=build /usr/src/rinha-backend/build/native/nativeCompile/ /rinha-backend/

WORKDIR /rinha-backend

EXPOSE 8080
#TODO ainda nao consegue subir dentro de um container docker  porém essa porcaria nao enxerga o banco
ENTRYPOINT ["/rinha-backend/rinha-backend-2024-q1"]