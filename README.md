# Eclipse EMF.cloud EMF JSON-Jackson

[![build-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/eclipse-emfcloud/job/emfjson-jackson/job/master/&label=master-build)](https://ci.eclipse.org/emfcloud/job/eclipse-emfcloud/job/emfjson-jackson/job/master/)
[![p2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2&label=p2-publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2/)
[![m2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2&label=m2-publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2/)

EMF JSON-Jackson provides a [JSON](https://www.json.org/) binding for [EMF (Eclipse Modeling Framework)](http://www.eclipse.org/emf) using [Jackson](https://github.com/FasterXML/jackson), that allows serialization and deserialization of EMF Resources in JSON.

> IMPORTANT:
> This projects was originally hosted by [emfjson/emfjson-jackson](https://github.com/emfjson/emfjson-jackson) and was moved to Eclipse in 2021.
> Due to the move several names (packages, bundle, groupId, artifactId) were adapted and a new version of the p2 bundles and the maven artifacts was released.

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

## Building

The EMF JSON-Jackson bundles are built with Java 11 or higher and maven.
Execute `mvn clean verify -Pm2` to build the Maven artifacts.
The nightly builds are available as maven repository or p2 update site.

### Maven [![m2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2&label=m2-publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-m2/)

- Snapshots: <https://central.sonatype.com/repository/maven-snapshots/org/eclipse/emfcloud/emfjson-jackson/>
- Releases/RCs: <https://repo1.maven.org/maven2/org/eclipse/emfcloud/emfjson-jackson/>

To use the Maven artifact add the following dependency to your POM file:

```xml
<dependency>
 <groupId>org.eclipse.emfcloud</groupId>
 <artifactId>emfjson-jackson</artifactId>
 <version>...</version>
</dependency>
```

If you want to consume the nightly builds you have to configure the sonatype snapshot repository in your `pom.xml`:

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

### P2 Update Site [![p2-deploy-status](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2&label=p2-publish)](https://ci.eclipse.org/emfcloud/job/deploy-emfcloud-emfjson-jackson-p2/)

- Snapshots: <https://download.eclipse.org/emfcloud/emfjson-jackson/p2/nightly/>
- Releases/RCs: <https://download.eclipse.org/emfcloud/emfjson-jackson/p2/releases/>

This update sites contain the bundle and feature for emfjson-jackson and Jackson.

## Development

Development can be done with any Java IDE (Eclipse, IntelliJ, etc.), it does not require any special tooling except Maven.

```
git clone https://github.com/emfjson/emfjson-jackson.git
[go inside directory emfjson-jackson]
mvn clean verify
```

This command will call the `xtext:generate` action that will generate the code from the `Xcore` model for the tests.

The tests can be run with the maven command: `mvn clean test` (or by running class `TestSuite` from your IDE).
The tests require some code to be generated from a [Xcore](http://wiki.eclipse.org/Xcore) model.
This generated code is not included in this repository, but is generated when the sources are first build locally.

## Documentation and Support

You can find documentation on EMF JSON-Jackson in our [wiki page](https://github.com/eclipse-emfcloud/emfjson-jackson/wiki).

If you have questions, please raise them on our [discussions page](https://github.com/eclipse-emfcloud/emfcloud/discussions) and have a look at our [communication and support options](https://www.eclipse.org/emfcloud/contact/).

For further information on EMF.cloud, please visit the [Eclipse EMF.cloud Umbrella repository](https://github.com/eclipse-emfcloud/emfcloud) and the [Eclipse EMF.cloud Website](https://www.eclipse.org/emfcloud/).

If you plan on contributing to this project, please see the [Contribution Guidelines](CONTRIBUTING.md).
