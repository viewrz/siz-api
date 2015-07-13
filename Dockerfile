FROM quay.io/sizio/sbt

MAINTAINER SIZ IO

ENV PLAY_APP_NAME siz-api
ENV PLAY_APP_DIR /var/www/$PLAY_APP_NAME
RUN mkdir -p $PLAY_APP_DIR
COPY build.sbt $PLAY_APP_DIR/
COPY app $PLAY_APP_DIR/app/
COPY conf $PLAY_APP_DIR/conf/
COPY public $PLAY_APP_DIR/public/
COPY project/*.properties project/*.sbt project/*.scala $PLAY_APP_DIR/project/
RUN adduser play_app
RUN chown -R play_app:play_app $PLAY_APP_DIR

WORKDIR $PLAY_APP_DIR
USER play_app
ENV HOME $PLAY_APP_DIR
RUN sbt clean stage

EXPOSE 9000
CMD ["sh", "-c", "$PLAY_APP_DIR/target/universal/stage/bin/$PLAY_APP_NAME"]
