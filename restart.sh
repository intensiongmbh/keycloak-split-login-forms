#!/bin/bash
docker-compose -f src/main/docker/docker-compose.yaml down -v
mvn clean package -DskipTests
docker-compose -f src/main/docker/docker-compose.yaml up --build --detach
docker logs -f keycloak
