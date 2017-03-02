FROM java:8
VOLUME /tmp
ADD demo-0.0.1-SNAPSHOT.jar user_microservice.jar
EXPOSE 8091
RUN sh -c 'touch /user_microservice.jar'
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /user_microservice.jar" ]
