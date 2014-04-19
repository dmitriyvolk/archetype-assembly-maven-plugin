archetype-assembly-maven-plugin
===============================

This Maven plugin will help you build an archetype, containing modules provided by other archetypes.

Example
----
Let's say you want to create an archetype for a standard three-tier Java app. In terms of the source layout, you will probably need the following modules:

+ `domain`
+ `services`
+ `web`

Imagine also that for each of the modules you want to use some framework that requires some thought and care when being introduced into a project, and each module's setup s different. Of course, there are archetypes out there for each individual module type, but you want to have an uber-archetype, a template to tie all those modules together. 

This is where this plugin comes in handy. Let's assume that for the `domain` module we don't need to do anything special, we want `services` to be created created from a Spring archetype, and `web` should be a Spring MVC module. 

First, let's take care of the "nothing special" module, `domain`. You follow the archetype creation [tutorial](http://maven.apache.org/guides/mini/guide-creating-archetypes.html) and put this into `src/main/resources/META-INF/archetype-metadata.xml`:
```XML
<?xml version="1.0" encoding="UTF-8"?>
<archetype-descriptor xsi:schemaLocation="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.0.0 http://maven.apache.org/xsd/archetype-descriptor-1.0.0.xsd" 
    xmlns="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    name="project-inder-test"
    partial="false">
    <!-- ... -->
    <modules>
        <module id="domain" name="Application Domain" dir="domain />
    </modules>
</archetype-descriptor>

```

Normally you would need to list other modules here as well, but you want to take advantage of already created archetypes, in other words, use them as dependencies. In Maven world, all the dependency declaration happens in POM. So let's close this file and look at your archetype's `pom.xml`:
```XML
<project>
    <!-- ... -->
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>org.appfuse.archetypes</groupId>
            <artifactId>appfuse-basic-spring-archetype</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.github.spring-mvc-archetypes</groupId>
            <artifactId>spring-mvc-quickstart</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    
    <build>
        <!-- ... -->
        <plugins>
            <!-- ... -->
            <plugin>
                <groupId>net.dmitriyvolk.maven.plugins</groupId>
                <artifactId>archetype-assembly-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>add-extra-modules</id>
                        <phase>process-resources</phase>
                        <goals>
                            <goal>add-modules</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <additionalModules>
                        <module>
                            <archetype>org.appfuse.archetypes:appfuse-basic-spring-archetype</archetype>
                            <id>services</id>
                            <name>Business logic module</name>
                            <dir>services</dir>
                        </module>
                        <module>
                            <archetype>com.github.spring-mvc-archetypes:spring-mvc-quickstart</archetype>
                            <id>${rootArtifactId}-web</id>
                            <name>Web Application</name>
                            <dir>__rootArtifactId__-web</dir>
                        </module>
                    </additionalModules>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
What happens when you install this archetype into your local repo and then use it to generate a new project?

First of all, the `domain` sub-module will be generated, no surprise here, basic archetype stuff. But you will also see two more sub-modules.

The `services` sub-module is created based on the information you set in the archetype's POM. It will look the same as if you'd generate directly from the `appfuse-basic-spring-archetype` archetype.

For the `web` module the [spring-mvc-quickstart](https://github.com/kolorobot/spring-mvc-quickstart-archetype) will be used, but the directory and the `artifactId` of this module will depend on the `artifactId` that the user will enter when generating a project from this prototype. So, for example, if the user entered "my-cool-app" as the `artifactId` for the generated project, the web application will reside in the `my-cool-app-web` directory and have the same `artifactId`.

Copyright Notice
----------------

Copyright 2014 Dmitriy Volk dmitriy@dmitriyvolk.net

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

