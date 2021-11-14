# TODO replace with JRE later
FROM eclipse-temurin:17-focal
WORKDIR /application

ENV DEFAULT_JAVA_OPTS="-Duser.timezone=UTC"
ENV JAVA_OPTS=""

# copy unpacked layers
COPY exploded-jar/dependencies/ ./
COPY exploded-jar/snapshot-dependencies/ ./
COPY exploded-jar/spring-boot-loader/ ./
COPY exploded-jar/application/ ./

EXPOSE 8080

RUN echo '#!/bin/sh \n set -e; exec java ${DEFAULT_JAVA_OPTS} ${JAVA_OPTS} org.springframework.boot.loader.JarLauncher $@' > entrypoint.sh
RUN chmod u+x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh", "--spring.profiles.active=local-dev"]
