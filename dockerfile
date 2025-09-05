FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y dnsutils
RUN apt-get update && apt-get install -y python3 build-essential
RUN curl -L -o /usr/share/jenkins/ref/jedis.jar \
    https://repo1.maven.org/maven2/redis/clients/jedis/4.4.3/jedis-4.4.3.jar

RUN apt-get update && apt-get install -y redis-tools

USER jenkins
