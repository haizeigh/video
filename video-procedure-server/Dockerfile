# 添加 Java 8 镜像来源
FROM ffmpeg-java:5.0

MAINTAINER yu

#VOLUME /tmp
WORKDIR /tmp/workdir

# 添加参数
#ARG JAR_FILE

# 添加 Spring Boot 包
ADD target/*.jar app.jar
ADD ./D03_20210104110141.mp4  D03_20210104110141.mp4
ADD ./app.sh  app.sh

#中文以及市时区
ENV TZ 'Asia/Shanghai'
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

#ADD ./jdk1.8.0_171 /data/jdk1.8.0_171
#java环境变量
ENV JAVA_HOME=/usr/java/jdk1.8.0_251
ENV CLASSPATH .:$JAVA_HOME/lib:$JAVA_HOME/jre/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH

# 执行启动命令
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
#RUN bash -c 'touch app.jar'
ENTRYPOINT ["java","-jar","app.jar"]
