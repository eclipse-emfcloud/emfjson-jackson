# [EMF](http://www.eclipse.org/emf) Binding for JSON [![build-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/eclipse-emfcloud/job/emfjson-jackson/job/master/)](https://ci.eclipse.org/emfcloud/job/eclipse-emfcloud/job/emfjson-jackson/job/master/)

## IMPORTANT
Due to the move to eclipse the bundle name for p2 bundles as well as the groupId and artfactId for Maven changed! Also the package names changed.
---

JSON binding for EMF (Eclipse Modeling Framework) models, that allows serialization and deserialization of EMF Resources in JSON.

This is how a model looks like in JSON format.

```javascript
{
    "eClass" : "http://www.eclipse.org/emf/2002/Ecore#//EPackage",
    "name" : "model",
    "nsPrefix" : "model",
    "nsURI" : "http://www.example.org/model",
    "eClassifiers" : [
        {
            "eClass" : "http://www.eclipse.org/emf/2002/Ecore#//EClass",
            "name" : "Library"
        }
    ]
}
```

## Documentation and Support
You can find documentation on our [wiki page](https://github.com/eclipse-emfcloud/emfjson-jackson/wiki).

If you have questions, contact us on our [discussion page](https://github.com/eclipse-emfcloud/emfcloud/discussions). 

## Installation

Builds are available for Maven users and Eclipse Plugins users.

### Maven  [![m2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2&label=publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2/)

Add the following dependency to your POM file to use emfjson-jackson.

```xml
<dependency>
	<groupId>org.eclipse.emfcloud</groupId>
	<artifactId>emfjson-jackson</artifactId>
	<version>1.3.1</version>
</dependency>
```
You can download the releases from [Maven Central](https://search.maven.org/search?q=org.eclipse.emfcloud%20emfjson) (note that there are currently releases yet)
or use our [Snapshot Repository](https://oss.sonatype.org/content/repositories/snapshots/org/eclipse/emfcloud/emfjson-jackson/).

Note: If you want to consume the nightly builds you have to configure the sonatype snapshot repository in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>oss.sonatype.org-snapshot</id>
        <url>http://oss.sonatype.org/content/repositories/snapshots</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```


### Eclipse Update Manager [![p2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2&label=publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2/)

Use this update site [https://download.eclipse.org/emfcloud/emfjson-jackson/p2/nightly/](https://download.eclipse.org/emfcloud/emfjson-jackson/p2/nightly/)

This update site contains the bundle and feature for emfjson-jackson and Jackson. 

## Dependencies

* Java 8
* EMF 2.19
* Jackson 2.10

# Development

Development can be done with any Java IDE (Eclipse, IntelliJ, etc...), it does not require any special tooling except Maven. 

> The tests require some code to be generated from a [Xcore](http://wiki.eclipse.org/Xcore) model. This generated code is not included in this repository, but 
is generated when the sources are first build locally, see next section for details.

## Building from sources

If you want to build from sources, you will need the Java 8 runtime installed on your system as well as Maven 3.
First start by cloning this repository.

```
git clone https://github.com/emfjson/emfjson-jackson.git
```

Inside the folder `emfjson-jackson`, run the following maven command to build and install the project locally.
 
```
mvn clean install
``` 

This command will call the `xtext:generate` action that will generate the code from the `Xcore` model for the tests. 

## Running tests

The tests can be run with the maven command:

```
mvn clean test
```

Or run the class `TestSuite` from your IDE.
