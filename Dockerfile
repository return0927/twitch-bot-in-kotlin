FROM amazoncorretto:17-alpine as builder

WORKDIR /tmp/luya-support

ADD *.kts gradlew ./
ADD gradle ./gradle
RUN chmod +x gradlew && ./gradlew build -x test --parallel --continue 2> /dev/null || true

COPY . /tmp/luya-support
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon
RUN cp ./build/libs/*.jar boot.jar

FROM amazoncorretto:17-alpine as production

ENV JAVA_ARGS=""
ENV PROC_ARGS=""

WORKDIR /run/luya-support/
COPY --from=builder /tmp/luya-support/boot.jar ./boot.jar

CMD [ "sh", "-c", "java $JAVA_ARGS -jar ./boot.jar $PROC_ARGS " ]
