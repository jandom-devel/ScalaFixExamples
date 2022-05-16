FROM fedora:36
RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > /etc/yum.repos.d/sbt-rpm.repo && \
    dnf -y install java-17-openjdk-headless git ppl sbt && \
    adduser scalafix
USER scalafix
ENTRYPOINT [ "/bin/bash" ]
WORKDIR /home/scalafix
RUN git clone https://github.com/jandom-devel/ScalaFix.git && \
    git clone https://github.com/jandom-devel/ScalaFixExamples.git
WORKDIR ScalaFixExamples
RUN sbt compile
