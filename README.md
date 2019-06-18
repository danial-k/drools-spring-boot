# Embedded KIE Drools Spring Boot sample
## Introduction
This project is a multi-module Maven project with two components:
- A Spring Boot application
- Embedded KIE Drools for processing business rules

Docker will be used to run a Maven container to generate and build the project. 

## Setting up Docker container
Start a Maven container with:
```shell
docker run \
--name drools-spring-boot \
--hostname drools-spring-boot \
-it \
-p 3055:8080 \
-p 3056:35729 \
--mount type=bind,src=`pwd`,dst=/drools-spring-boot \
-w //drools-spring-boot \
maven:3.6-jdk-8 \
bash
```

## Generating Drools sub-module
Browse for the latest Drools Maven archetype at https://search.maven.org/artifact/org.kie/kie-drools-archetype/

Use Maven's archetype to generate a new Drools project:
```shell
mvn archetype:generate \
-DarchetypeGroupId=org.kie \
-DarchetypeArtifactId=kie-drools-archetype \
-DarchetypeVersion=7.23.0.Final \
-DinteractiveMode=false \
-DgroupId=com.example \
-DartifactId=drools \
-Dversion=1.0.0-SNAPSHOT
```

The generated project should contain a sample POJO (plain old java object), a sample drl file containing rules and a basic unit test. Add the following to ```drools/pom.xml```.  This will change the project from a standalone Maven format to a child sub-project by referring to the parent's GAV (group, artifactId and version).  Note the ```kjar``` packaging (knowledge jar) which is specific to Drools and is enabled by the ```kie-maven-plugin```.
```xml
...
<parent>
    <groupId>com.example</groupId>
    <artifactId>drools-spring-boot</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
...
```
Move the ```rules.drl``` file from ```resources``` to ```com/example/drools``` manually or with:
```shell
mkdir -p drools/src/main/resources/com/example/drools
mv drools/src/main/resources/rules.drl drools/src/main/resources/com/example/drools/rules.drl
```
This creates a directory recursively with all the parent directories that do not yet exist (```-p```) then moves the file to the newly created directory.  Similarly, move the test file and the java file.

## Generating Spring Boot sub-module
Use [Spring Initializr](https://start.spring.io/) archetype to generate a new Spring project, either by running the following to download a pre-configured zip file or visiting https://start.spring.io and setting ```Project```: ```Maven```, ```Language```: ```Java```, ```Spring Boot```: ```2.1.5``` Project Metadata's ```Group```: ```com.example```, ```Artifact```: ```spring-boot```, ```Package Name```: ```com.example.spring-boot```, ```Packaging```: ```Jar``` and ```Java```: ```8```. Add a Spring Web Starter dependency so RESTful services are also included.

```shell
curl -LOJ "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=2.1.5.RELEASE&baseDir=spring-boot&groupId=com.example&artifactId=spring-boot&name=spring-boot&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.spring-boot&packaging=jar&javaVersion=1.8&style=web"
```

This command instructs cURL to follow any redirect locations (```-L```), output a file locally using the remote file name (```-O```) and also to use the header provided filename (```-J```) Spring Initializr is used because a Maven Archetype [does not currently exist](https://github.com/spring-projects/spring-boot/issues/6063).

Once the zip archive has been downloaded, expand it to a new ```spring-boot``` directory with the ```-d``` argument:
```shell
unzip spring-boot.zip
```

The zip directory may now be removed with:
```shell
rm spring-boot.zip
```

## Define parent project structure
In the root directory, create a new ```pom.xml``` file with the following content.  Note that each sub-module name should also be the name of each sub-module's directory.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>drools-spring-boot</artifactId>
    <packaging>pom</packaging>
    <version>1.0.0-SNAPSHOT</version>
    <name>KIE Drools Spring Boot API</name>

    <!-- Define sub-modules for this project to be compiled together -->
    <!-- Paths are assumed to match sub-module names -->
    <modules>
        <module>drools</module>
        <module>spring-boot</module>
    </modules>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- Dependency Management controls versions for sub-modules -->
    <!-- This means sub-modules can omit versions -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <version>2.1.5.RELEASE</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <version>2.1.5.RELEASE</version>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-devtools</artifactId>
                <version>2.1.5.RELEASE</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <!-- Plugin Management controls versions for sub-modules -->
        <!-- This means sub-modules can omit versions -->
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>2.1.5.RELEASE</version>
                    <!-- This configuration option allows running mvn spring-boot:run from the parent directory -->
                    <!-- The sub-module pom should contain a skip:false -->
                    <!-- https://github.com/drahkrub/spring-boot-multi-module -->
                    <configuration>
                        <skip>true</skip>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

To run the spring application in development mode navigate to the ```spring-boot``` sub-module and run:
```
mvn spring-boot:run
```
This will start up the built-in Tomcat server on port 8080, and the application will be available at http://127.0.0.1:3055.

# Building a Docker image
To build a deployable application package the following commands are needed:
```shell
mvn package spring-boot:repackage
java -jar spring-boot/target/spring-boot-0.0.1-SNAPSHOT.jar
```
