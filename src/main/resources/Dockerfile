FROM openjdk:8
MAINTAINER Brian
RUN mkdir -p /opt/logs
RUN chown -R 1000:1000 /opt
VOLUME /opt/logs
ADD CreditCardService-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'

ENV PARAMS=""
ENV JAVA_OPTS="-Xms2048m -Xmx2048m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=prod $PARAMS" ]
WORKDIR /opt