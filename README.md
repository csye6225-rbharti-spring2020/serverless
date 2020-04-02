Serverless Application for Lambda.

Application Details:

```
Programming Language: Java
CI/CD: CircleCI
```

Build And Deploy Instructions.

Prerequisites:

```
JDK 1.8 or later
Maven 3 or later
```

Instructions:

Only Testing (entire application):

```
mvn test
```

Testing specific classes:

```
mvn -Dtest=TestApp1 test
```

The following commands builds, tests and runs the application. After cloning the repository:

```
mvn clean install
```

The following commands are used to create an executable jar

```
mvn package
```
