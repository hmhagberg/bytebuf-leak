#!/bin/sh

CLASSPATH=$1
PROTO_SOURCES_DIR=$2
GENERATED_SOURCES_DIR=$3
PROTOC_VERSION=$4

mkdir -p "${GENERATED_SOURCES_DIR}"
GRPC_PLUGIN_SCRIPT="${GENERATED_SOURCES_DIR}/../../grpc_gen"
cat << EOF > "${GRPC_PLUGIN_SCRIPT}"
#!/bin/bash

java -cp $CLASSPATH io.buoyant.grpc.gen.Main "\$@"
EOF

chmod +x "${GRPC_PLUGIN_SCRIPT}"
java -cp $CLASSPATH com.github.os72.protocjar.Protoc -v:com.google.protobuf:protoc:${PROTOC_VERSION} -I${PROTO_SOURCES_DIR} --plugin=protoc-gen-io.buoyant.grpc=${GRPC_PLUGIN_SCRIPT} --io.buoyant.grpc_out=plugins=grpc:${GENERATED_SOURCES_DIR} ${PROTO_SOURCES_DIR}/*.proto