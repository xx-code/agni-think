set -e 

echo "[-] BUILD & PUSH TO $REGISTRY frontend version $VERSION" 

if [ -z "$VERSION"]; then
    echo " VERSION is undefined."
    exit 1
fi 

if [ -z "$REGISTRY" ]; then
    echo " REGISTRY is undefined."
    exit 1
fi

echo "[-] Start build"
echo "    Registry: $REGISTRY"
echo "    Version: $VERSION"

echo "[-] Build Image"
docker build --build-arg BUILD_CONFIGURATION=Release -t ${REGISTRY}/agni-web:${VERSION} -f config/frontend/Dockerfile .

echo "[-] Push ${REGISTRY}/agni-web:${VERSION}"
docker push ${REGISTRY}/agni-web:${VERSION}
echo "PUSH!"