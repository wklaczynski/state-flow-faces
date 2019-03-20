# Java State Flow Faces
This project integrates Java Server Faces with State Chat Flow. Uses scxml notation to create flow diagrams in Java Server Faces views. It is an alternative to Flow Faces. It extends the possibility of constructing flow patterns with assumptions included in the scxml specification.

https://www.w3.org/TR/scxml/

## Code Example
```
<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<f:metadata
    xmlns:f="http://xmlns.jcp.org/jsf/core"
    xmlns:x="http://xmlns.ssoft.org/faces/scxml"
    xmlns:fx="http://xmlns.ssoft.org/faces/fxscxml"
    xmlns:c="http://xmlns.jcp.org/jsp/jstl/core">
    <x:scxml id="main" initial="start">

        <x:datamodel>
            <x:data id="hasback" expr="#{scxml_has_parent == true}"/>
            <x:data id="title" expr="State Flow Faces from Faces Metadata"/>
            <x:data id="view" expr="/flow1/faces-flow-main.xhtml"/>
            
            <x:data id="chartAssignedTest" expr="no tested"/>
            <x:data id="assignedRasult" expr="no result"/>
            <x:data id="book" src="/test/data.xml"/>
        </x:datamodel>
        <x:state id="start">
            <x:onentry>
                <x:if cond="#{main.prepare()}">
                    <x:raise event="start.prepare.success"/>
                    <x:else/>
                    <x:raise event="start.prepare.failed"/>
                </x:if>
            </x:onentry>
            <x:transition event="start.prepare.success" target="main"/>
            <x:transition event="start.prepare.failed" target="exit"/>
        </x:state>

        <x:state id="main">
            <x:invoke type="view" src="MainPage.xhtml">
                <x:param name="page_description" expr="Page description from scxml."/>
            </x:invoke>

            <x:transition event="error.execution" target="main"/>

            <x:transition event="view.action.show" target="show-client"/>
            <x:transition event="view.action.update" target="update-client"/>

            <x:transition event="view.action.exit" target="exit"/>
            <x:transition event="view.action.assign" target="assing-test"/>
            <x:transition event="view.action.clear" target="clear-test"/>
            
            <x:transition event="view.action.custom-action">
                <d:test message="I'm executing test action"/>
            </x:transition>
            
            <x:transition event="view.action.fx-redirect">
                <fx:redirect url="/flow2/faces-flow-test.xhtml">
                    <x:param name="aparam" expr="#{true}"/>
                </fx:redirect> 
            </x:transition>
            
            <x:transition event="view.action.fx-call">
                <fx:call expr="#{main.testCall}">
                    <x:param name="name" location="string" expr="Parametr 1"/>
                    <x:param name="value" location="string" expr="Parametr 2"/>
                </fx:call> 
            </x:transition>
            
        </x:state> 

        <x:state id="show-client"> 
            <x:onentry>
                <x:var name="selected" expr="#{book.client['@id'][param.clid]}"/>
            </x:onentry>
            <x:invoke type="scxml" src="faces-flow-client-show.xhtml">
                <x:param name="clientId" expr="#{selected.id.$text}"/>
                <x:param name="clientName" expr="#{selected.name.$text}"/>
                <x:param name="clientSurname" expr="#{selected.surname.$text}"/>
                <x:param name="clientMobilePhone" expr="#{selected.phone['@type=\'mobile\''].$text}"/>
                <x:param name="clientHomePhone" expr="#{selected.phone['@type=\'home\''].$text}"/>
            </x:invoke>
            <x:transition event="error.execution" target="main"/>
            <x:transition event="done.invoke.show-client" target="main"/>
        </x:state>      

        <x:state id="update-client"> 
            <x:onentry>
                <x:var name="selected" expr="#{book.client['@id'][param.clid]}"/>
            </x:onentry>
            <x:invoke type="scxml" src="faces-flow-client-update.xhtml">
                <x:param name="clientId" expr="#{selected.id.$text}"/>
                <x:param name="clientName" expr="#{selected.name.$text}"/>
                <x:param name="clientSurname" expr="#{selected.surname.$text}"/>
                <x:param name="clientMobilePhone" expr="#{selected.phone['@type=\'mobile\''].$text}"/>
                <x:param name="clientHomePhone" expr="#{selected.phone['@type=\'home\''].$text}"/>
            </x:invoke>
            <x:transition event="error.execution" target="main"/>

            <x:transition 
                event="done.invoke.update-client" 
                cond="#{done.success}" 
                target="main">
                <x:assign location="#{selected.name.$text}" expr="#{done.name}"/>
                <x:assign location="#{selected.surname.$text}" expr="#{done.surname}"/>
                <x:assign location="#{selected.phone['@type=\'mobile\''].$text}" expr="#{done.mobilePhone}"/>
                <x:assign location="#{selected.phone['@type=\'home\''].$text}" expr="#{done.homePhone}"/>
            </x:transition> 
            <x:transition event="done.invoke.update-client" target="main"/>
        </x:state>      

        <x:state id="assing-test">
            <x:onentry>
                <x:assign 
                    location="#{main.assignedTest1}" 
                    expr="#{book['client[last()]/phone[@type=\'mobile\']'].$text}"
                    />
                <x:assign 
                    location="#{client.assignedTest1}" 
                    expr="#{book['client[last()]/phone[@type=\'home\']'].$text}"
                    />
                <x:assign 
                    location="chartAssignedTest" 
                    expr="#{book['client[1]/phone[@type=\'home\']'].$text}"
                    />
                <x:raise event="assing-test.finish"/>
            </x:onentry>
            <x:transition event="assing-test.finish" target="main"/>
        </x:state> 

        <x:state id="clear-test">
            <x:onentry>
                <x:assign location="#{main.assignedTest1}"/>
                <x:assign location="#{client.assignedTest1}"/>
                <x:assign location="chartAssignedTest"/>
                <x:raise event="clear-test.finish"/>
            </x:onentry>
            <x:transition event="clear-test.finish" target="main">
            </x:transition>
        </x:state> 
        
        <x:state id="error"> 
            <x:invoke type="scxml" src="/common/error.scxml">
                <x:finalize/>
            </x:invoke>
            <x:transition event="done.invoke.error.*" target="exit"/>
        </x:state>      

        <x:final id="exit">

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
    <version>1.1-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.ssoft.faces</groupId>
    <artifactId>state-flow-fases-impl</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency>
```
And primefaces extension to add dialog invoker and pull task with primefaces ajax script:

```
<dependency>
    <groupId>org.ssoft.faces</groupId>
    <artifactId>state-flow-primefaces</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency>
```


Srate Flow Faces are not available at Maven Central,  add the following repository definition to your pom.xml in repositories section.
```
<repository>
   <id>state-flow-faces-mvn-repo</id>
   <url>https://raw.github.com/wklaczynski/state-flow-faces/mvn-repo</url>
   <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
   </snapshots>
</repository>
```
## Running the tests

To run the test, deploy "state-flow-basic-demo.war" or "state-flow-prime-demo.war" on the server.

## Roadmap

Version 1.1 will have the ability to define flows in portlets as components of the facelets "ui: component". Displaying "facet" in invoke in the jsf component. Full support jsf 2.3.

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


