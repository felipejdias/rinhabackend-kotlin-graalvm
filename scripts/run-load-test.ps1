param(
    [string]$BaseUrl = "http://localhost:9999",
    [string]$Simulation = "RinhaBackendCrebitosSimulation"
)

# Por que rodar o Gatling dentro de um container em vez de "./gradlew gatlingRun" direto no Windows:
#
# No projeto teste-itau, o mesmo teste batendo na porta publicada (localhost / host.docker.internal)
# deu ~6% de timeouts sem nenhum erro na aplicacao, enquanto rodando o gerador de carga dentro do
# Docker deu 0 timeouts e latencia ~40% menor. O culpado e o proxy de portas do Docker Desktop
# (WSL2/vpnkit), que engasga sob concorrencia de conexoes fora da visao da JVM.
#
# Aqui o compose usa network_mode: host, entao o Gatling tambem roda com --network host:
# "localhost:9999" e o nginx direto no namespace de rede da VM do Docker, sem passar pelo proxy.
#
# Por que duas etapas: com "Enable host networking" ligado no Docker Desktop, o Gradle NAO funciona
# dentro de um container --network host (cliente, daemon e workers conversam por TCP no loopback e o
# Docker Desktop intercepta essas portas -> "Could not connect to the Gradle daemon"). Entao:
#   1. o Gradle compila a simulacao e copia as dependencias num container comum (rede bridge);
#   2. so a JVM do Gatling roda com --network host, via "java -cp", sem Gradle.
# O relatorio continua saindo em teste-gatling/build/reports/gatling no Windows (volume montado).

$ErrorActionPreference = "Stop"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker nao encontrado. Este script roda o Gatling em um container com --network host."
}

# Daqui pra frente o controle e pelo $LASTEXITCODE: no Windows PowerShell 5.1, com "Stop", qualquer
# linha no stderr do docker/Gradle (ex.: warnings da JVM) vira exception quando a saida e redirecionada.
$ErrorActionPreference = "Continue"

$projectRoot = Split-Path $PSScriptRoot -Parent
$image = "eclipse-temurin:25-jdk"
$projectVolume = "$($projectRoot):/project"

# 1. compila a simulacao e exporta o classpath do Gatling (rede bridge padrao)
$buildArgs = @(
    "run", "--rm",
    "-v", $projectVolume,
    "-v", "rinha-gatling-gradle-cache:/root/.gradle",
    "-w", "/project",
    $image,
    "sh", "-c",
    "chmod +x ./gradlew && ./gradlew --no-daemon --project-cache-dir /tmp/gradle-project-cache :teste-gatling:gatlingRuntimeLibs"
)

& docker @buildArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

# 2. roda o Gatling direto na JVM, na rede host (mesmas flags de JVM que o plugin do Gatling usa)
$gatlingDir = "/project/teste-gatling/build"
$runArgs = @(
    "run", "--rm",
    "--network", "host",
    "-v", $projectVolume,
    "-e", "BASE_URL=$BaseUrl",
    $image,
    "java",
    "-Xmx1G",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:MaxInlineLevel=20",
    "-XX:MaxTrivialSize=12",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
    "--enable-native-access=ALL-UNNAMED",
    "--sun-misc-unsafe-memory-access=allow",
    "-cp", "$gatlingDir/resources/gatling:$gatlingDir/classes/scala/gatling:$gatlingDir/gatling-runtime/lib/*",
    "io.gatling.app.Gatling",
    "-s", $Simulation,
    "-rf", "$gatlingDir/reports/gatling"
)

& docker @runArgs
exit $LASTEXITCODE
