FROM --platform=linux/amd64 openjdk:17-alpine

EXPOSE 8080

ADD target/partner-gateway.jar partner-gateway.jar

ENTRYPOINT exec java $BOOTAPP_OPTS  -jar partner-gateway.jar