## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Java JDK 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
- [Maven 3.9.x or higher (with Java JDK 17)](https://maven.apache.org/)
- [Node.js 18.20.x or higher](https://nodejs.org/en/download)
- [pnpm](https://pnpm.io/installation)

## Run Backend

1. Clone the repository

```shell
git clone git@github.com:basedt/dms.git
```

2. Open the project in IntelliJ IDEA.
3. Open terminal in IntelliJ IDEA and start docker containers

```shell
cd scripts/docker/local
docker compose --profile dev -p dms_dev up -d
```

4. Build backend

```shell
cd dms
mvn clean compile -DskipTests
```

5. Open [DmsApplication.java](dms-api/src/main/java/com/basedt/dms/DmsApplication.java) in dms-api module and run it.

## Run Frontend

1. Build frontend

```shell
cd dms-ui
pnpm install
```
2. Start frontend

```shell
pnpm start
```
3. Open your browser and navigate to [http://localhost:8000](http://localhost:8000).