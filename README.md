# Java State Flow Faces
This project integrate Java Server Faces witch State Chat Flow. It uses scxml notations to construct flow diagrams in the jaunted views of Java Server Faces. It is an alternative to the native Flow Faces. It extends the possibility of constructing flow diagrams with the assumptions contained in the scxml specification. Is a project integrating Java Server Faces with the State Chart XML (SCXML) specification.

https://www.w3.org/TR/scxml/

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Dependency configuration would be as follows;
```
<dependency>
    <groupId>org.ssoft.faces</groupId>
    <artifactId>state-flow-fases-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.ssoft.faces</groupId>
    <artifactId>state-flow-fases-impl</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Srate Flow Faces are not available at Maven Central,  add the following repository definition to your pom.xml in repositories section.
```
<repository>
   <id>state-flow-faces-mvn-repo</id>
   <url>https://raw.github.com/wklaczynski/state-flow-faces/mvn-repo/</url>
   <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
      </snapshots>
</repository>
```
## Running the tests

To run the test, run "state-flow-basic-demo.war" on the server.

## Authors

* **Waldemar Kłaczyński** - *Initial work* - [Waldemar Kłaczyński](https://github.com/wklaczynski)

See also the list of [contributors](https://github.com/wklaczynski/state-flow-faces/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Inspiration

https://www.w3.org/TR/scxml/
http://www.oracle.com/technetwork/java/javaee/javaserverfaces-139869.html
http://commons.apache.org/proper/commons-scxml/


