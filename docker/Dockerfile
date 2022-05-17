FROM fedora:36
RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > /etc/yum.repos.d/sbt-rpm.repo && \
    dnf -y install java-17-openjdk-headless git ppl sbt emacs-nox && \
    dnf -y clean all && \
    adduser scalafix
USER scalafix
RUN cd /home/scalafix && \
    git clone https://github.com/jandom-devel/ScalaFix.git && \
    git clone https://github.com/jandom-devel/ScalaFixExamples.git && \
    cd /home/scalafix/ScalaFix && sbt compile && \
    cd /home/scalafix/ScalaFixExamples && sbt compile
ENTRYPOINT [ "/bin/bash" ]
WORKDIR /home/scalafix/ScalaFixExamples
