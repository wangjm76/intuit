# Project

* JDK 17
* springboot mvc/hibernate/postgresql/flyway...
* build : gradle
* Test : JUnit, TestContainer
* IDE: intellij
* docker is required to spin up db instance
* run
    * [./gradlew test] to run the tests
    * start application locally:
        1. spin up a local postgresql db by `docker compose -f ./db.docker-compose.yml up`
        2. `./gradlew bootRun` to start the application 