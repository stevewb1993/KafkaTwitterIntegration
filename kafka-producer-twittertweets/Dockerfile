FROM openjdk:8-jre-slim
WORKDIR /root/kafka-producer-twittertweets
COPY target/kafka-producer-twittertweets-1.0-SNAPSHOT-jar-with-dependencies.jar /root/kafka-producer-twittertweets

#RUN apk add libexecinfo libexecinfo-dev
#RUN apk update libsnappy-dev
#RUN apk add libsnappy-dev

#RUN apk add openjdk8
#ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
#ENV PATH $PATH:$JAVA_HOME/bin


CMD java -jar kafka-producer-twittertweets-1.0-SNAPSHOT-jar-with-dependencies.jar