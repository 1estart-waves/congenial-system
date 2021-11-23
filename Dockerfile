FROM openjdk:11
ARG SBT_VERSION="1.5.0"

RUN \
#   curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#   dpkg -i sbt-$SBT_VERSION.deb && \
#   rm sbt-$SBT_VERSION.deb && \
  wget https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz && \
  tar xzvf sbt-$SBT_VERSION.tgz -C /usr/share/ && \
  update-alternatives --install /usr/bin/sbt sbt /usr/share/sbt/bin/sbt 9999 && \
#   apt-get update && \
#   apt-get install sbt && \
  sbt -Dsbt.rootdir=true sbtVersion

WORKDIR /mydir
ADD . /mydir

EXPOSE 9090

CMD ["sbt", "run"]