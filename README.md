# HTTP Server

An example Java 8 HTTP server.

It serves simple REST API for money transfer between accounts.
This includes data model and running in memory H2 database.

The idea behind this piece of code is to show how to write such server without using Spring.

# Execution

simple: `mvn clean install` builds the project and runs all the tests (unit and integration):

    [INFO] ------------------------------------------------------------------------
    [INFO] Reactor Summary:
    [INFO] 
    [INFO] HTTP Server :: POM ................................. SUCCESS [  0.667 s]
    [INFO] HTTP Server :: Rest Model :: JAR ................... SUCCESS [  3.505 s]
    [INFO] HTTP Server :: Server :: JAR ....................... SUCCESS [  2.595 s]
    [INFO] HTTP Server :: Integration Tests :: JAR ............ SUCCESS [  5.689 s]
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 12.586 s


# Tech stack

**Dev stack:**
- Java 8
- maven
- guice: dependency injection
- Gson: json processing
- Lombok: more elegant and concise code

**Test stack:**
- Spock: unit and integration testing
- H2: in-memory SQL database
- Commons DbUtils: to avoid JDBC boilerplate
- REST-assured: to test REST API

# Notes
As we all know there is no such thing like finished software.

Among many others, following might be additionally done:
- extract common test part into a separate maven module (test constants, database utils etc.)
- put common dependencies versions into parent pom
- more tests
- logging
- model backed by schema

Also, when in need for either more TPS or redundancy, the architecture change would be required.
The requests would be served asynchronously, additional buffer for incoming requests would be added (a cyclic one?).
Multiple instances would put messages to this buffer (many producers) and only one (single consumer) would process them
(including database updates and putting responses on the response queue). The reading process would have failover.
