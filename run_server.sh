#!/bin/sh
JAVA_OPTS="\
-Dio.netty.leakDetection.level=paranoid \
-Dcom.twitter.finagle.netty4.trackReferenceLeaks=true \
"
echo "JAVA_OPTS: $JAVA_OPTS"
java -cp main/target/main-0.0.1.jar $JAVA_OPTS node.Server
