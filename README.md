# LMI ULDP output adapter for Streambase
LMI Adapter for TIBCO StreamBase® - is an open-source adapter using which users can send StreamBase events to TIBCO LogLogic® Log Management Intelligence (LMI).

## Pre-requisites
You need to have LMI 6.2.1 or later, you will need the ULDP Java package located on the supplemental disk (lmi-uldp-client-api-6.x.y.jar and lmi-uldp-client-api-6.x.y.pom)
You need maven and Java installed to build the artifacts from sources.

## Howto build the uldp-client-api artifact from the sources
First you need to manually install the uldp-client-api artifact in your maven repository. 
```
mvn install:install-file -Dfile= <path to the zip file>/lmi-uldp-client-api-6.2.1.jar -DpomFile <path to the zip file>/lmi-uldp-client-api-6.2.1.pom
```
You may have to change the version referenced in the POM file for this artifact to the version you have (in the file, 6.2.1)
Then use  ```mvn clean install``` to build the artifact (uldp-output-1.0.0-SNAPSHOT.jar in the target directory)
The POM file corresponding to the artifact is the POM file at the root, you can copy it as uldp-output-1.0.0-SNAPSHOT.pom for the following.

## Usage with StreamBase 10.x

### Step 1: install manually a Maven artifact
First you need to manually install the uldp-client-api and uldp-output artifacts in your maven repository.

2 options there...

#### Option 1: Using StreamBase Studio
run/ run-configuration

Select “maven build”

Then click on the “new configuration” button (top left)

Choose base directory as your workspace

Goals: install:install-file

Add parameters:

file
<path to the zip file>/uldp-output-1.0.0-SNAPSHOT.jar
pomFile
<path to the zip file>/uldp-output-1.0.0-SNAPSHOT.pom

In the same manner, you also need to install the ULDP Client library

Goals: install:install-file

Add parameters:

file
<path to the zip file>/lmi-uldp-client-api-6.2.1.jar
pomFile
<path to the zip file>/lmi-uldp-client-api-6.2.1.pom

#### Option 2: Using maven command
```
mvn install:install-file -Dfile= <path to the zip file>/lmi-uldp-client-api-6.2.1.jar -DpomFile <path to the zip file>/lmi-uldp-client-api-6.2.1.pom
mvn install:install-file -Dfile= <path to the zip file>/uldp-output-1.0.0-SNAPSHOT.jar -DpomFile <path to the zip file>/uldp-output-1.0.0-SNAPSHOT.pom

```

## Step 2: Adding the Maven dependency for the project
Two options again

### Option 1/ Using UI
Right-click the StreamBase project, then choose Maven/Add dependency…

group ID: com.loglogic.adapter

artifact ID: uldp-output

version: 1.0.0-SNAPSHOT

### Option 2/ edition POM.xml
You can also add manually the following dependency in the POM.xml file:

<dependency>
    <groupId>com.loglogic.adapter</groupId>
    <artifactId>uldp-output</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>


## Step #3: Adding the ULDP output adapter to your flow


Now right click on project name, and choose Streambase/Refresh Project Typecheck Environment

This will load the new Output Adapter class and make it ready for addition to the flow.

In the Operators and Adapters palette, use “Adapters, Java Operators”,  click to add the Adapter on the Flow canvas.

Then choose Project/LogLogic ULDP.



Double click to set the properties, at least the ULDP host should be set.





## Usage with StreamBase 7


### Adding the dependencies


Right-click on project, select build path/add external archive

Then select lmi-uldp-client-api JAR file

Do it again for UldpOutput JAR file.



### Adding the ULDP output adapter to your flow
Now right click on project name, and choose Streambase/Refresh Project Typecheck Environment

This will load the new Output Adapter class and make it ready for addition to the flow.

In the Operators and Adapters palette, use “Adapters, Java Operators”,  click to add the Adapter on the Flow canvas.

Then choose Project/LogLogic ULDP.



Double click to set the properties, at least the ULDP host should be set.

