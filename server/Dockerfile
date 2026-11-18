# Tahap 1: build pakai JDK
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# Download dependency dulu supaya di-cache Docker (tidak diulang kalau cuma kode yang berubah)
RUN ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon installDist

# Tahap 2: image akhir cuma berisi JRE + hasil build
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system sempat
# --chown: tanpa ini jar milik root dengan izin 600 dan tidak bisa dibaca user sempat
COPY --from=build --chown=sempat:sempat /src/build/install/sempat-server ./
USER sempat
EXPOSE 8080
CMD ["./bin/sempat-server"]
