#!/bin/bash

set -e

echo "[-] BUILD & PUSH TO $REGISTRY agent version $VERSION"

if [ -z "$VERSION" ]; then
    echo "ERROR: VERSION is undefined."
    exit 1
fi

if [ -z "$REGISTRY" ]; then
    echo "ERROR: REGISTRY is undefined."
    exit 1
fi

echo "[-] Start build"
echo "    Registry: $REGISTRY"
echo "    Version: $VERSION"

echo "[-] Build Image"
docker build -t ${REGISTRY}/agni-ai-agent:${VERSION} -f config/agent/dockerfile .

echo "[-] Push ${REGISTRY}/agni-ai-agent:${VERSION}"
docker push ${REGISTRY}/agni-ai-agent:${VERSION}
echo "PUSH SUCCESSFUL!"
