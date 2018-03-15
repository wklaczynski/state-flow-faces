# Java State Flow Faces
This project integrate Java Server Faces witch State Chat Flow. It uses scxml notations to construct flow diagrams in the jaunted views of Java Server Faces. It is an alternative to the native Flow Faces. It extends the possibility of constructing flow diagrams with the assumptions contained in the scxml specification. Is a project integrating Java Server Faces with the State Chart XML (SCXML) specification.

https://www.w3.org/TR/scxml/

## Code Example

Show what the library does as concisely as possible, developers should be able to figure out **how** your project solves their problem by looking at the code example. Make sure the API you are showing off is obvious, and that your code is short and concise.

```
<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<f:metadata
    xmlns:f="http://xmlns.jcp.org/jsf/core"
    xmlns:x="http://xmlns.ssoft.org/flow/scxml"
    xmlns:c="http://xmlns.jcp.org/jsp/jstl/core"
    >
    <x:scxml initial="start">

        <x:state id="start">
            <x:onentry>
                <x:if cond="#{orders.prepare()}">
                    <x:raise event="main.prepare.success"/>
                    <x:else/>
                    <x:raise event="main.prepare.failed"/>
                </x:if>
            </x:onentry>
            <x:transition event="main.prepare.success" target="main"/>
            <x:transition event="main.prepare.failed" target="exit"/>
        </x:state>

        <x:state id="main">
            <x:invoke type="view" src="Main.xhtml"/>
            <x:transition event="main.view.test" target="test-order"/>
            <x:transition event="main.view.show" target="show-order"/>
        </x:state> 

        <x:state id="show-order"> 
            <x:invoke type="view" src="show-order.xhtml">
                <x:param name="id"  expr="#{orders.selected}"/>
                <x:param name="name" expr="#{orders.selected.name}"/>
            </x:invoke>
            <x:transition event="show-order.invoke.cancel" target="main"/>
            <x:transition event="show-order.invoke.success" target="main">
                <x:send event="update" target="#{orders.dispatchSend}"/>
            </x:transition>
        </x:state>      

        <x:state id="test-order">
            <x:onentry>
                <x:if cond="#{orders.test()}">
                    <x:raise event="test-order.prepare.success"/>
                    <x:else/>
                    <x:raise event="test-order.prepare.failed"/>
                </x:if>
            </x:onentry>
            <x:transition event="test-order.prepare.success" target="main"/>
            <x:transition event="test-order.prepare.failed" target="main"/>
        </x:state>

        <x:state id="error"> 
            <x:invoke type="scxml" src="/common/error.scxml">
                <x:finalize/>
            </x:invoke>
            <x:transition event="error.invoke.done" target="exit"/>
        </x:state>      

        <x:final id="exit">
            <x:onexit>

            </x:onexit>
        </x:final>

    </x:scxml>    

</f:metadata>
```
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

To run the test, deploy "state-flow-basic-demo.war" on the server.

## Built With

* [JSF](http://www.oracle.com/technetwork/java/javaee/javaserverfaces-139869.html) - The Java Server Faces used 
* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/wklaczynski/state-flow-faces/tags). 


## Authors

* **Waldemar Kłaczyński** - *Initial work* - [Waldemar Kłaczyński](https://github.com/wklaczynski)

See also the list of [contributors](https://github.com/wklaczynski/state-flow-faces/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Model documentation https://www.w3.org/TR/scxml/
* Inspiration http://commons.apache.org/proper/commons-scxml/
* Required http://www.oracle.com/technetwork/java/javaee/javaserverfaces-139869.html


