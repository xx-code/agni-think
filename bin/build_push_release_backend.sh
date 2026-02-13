set -e 

echo "[-] BUILD & PUSH TO $REGISTRY backend version $VERSION" 

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
docker build --build-arg BUILD_CONFIGURATION=Release -t ${REGISTRY}/agni-api-spring-boot:${VERSION} -f config/api/Dockerfile .

echo "[-] Push ${REGISTRY}/agni-api-spring-boot:${VERSION}"
docker push ${REGISTRY}/agni-api-spring-boot:${VERSION}
echo "PUSH!"