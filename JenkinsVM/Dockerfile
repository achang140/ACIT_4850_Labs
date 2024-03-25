FROM jenkins/jenkins:lts-jdk17
LABEL maintainer="mmulder10@bcit.ca"
USER root
RUN rm -rf venv
RUN mkdir /var/log/jenkins
RUN mkdir /var/cache/jenkins
RUN chown -R jenkins:jenkins /var/log/jenkins
RUN chown -R jenkins:jenkins /var/cache/jenkins
RUN apt-get update
RUN apt-get install -y python3 python3-pip
RUN apt-get install -y zip
RUN apt install -y python3.11-venv
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
RUN pip install SQLAlchemy
RUN pip install pylint
RUN pip install --upgrade pip
RUN pip install coverage
RUN apt-get install -y maven
RUN apt-get update && \
    apt-get -y install apt-transport-https \
    ca-certificates \
    curl \
    gnupg2 \
    software-properties-common && \
    curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; \
    echo "$ID")/gpg > /tmp/dkey; apt-key add /tmp/dkey && \
    add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
    $(lsb_release -cs) \
    stable" && \
    apt-get update && \
    apt-get -y install docker-ce
RUN apt-get install -y docker-ce
RUN usermod -a -G docker jenkins

USER jenkins
ENV JAVA_OPTS="-Xmx4096m"
ENV JENKINS_OPTS="--logfile=/var/log/jenkins/jenkins.log --webroot=/var/cache/jenkins/war --prefix=/jenkins"