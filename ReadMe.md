# PEM Converter Maven Plugin

A Maven plugin that converts OpenSSL PEM-formatted root certificate files to a format more easily and efficiently readable in a Java program.

## 1. In your Maven build:

Add this to the `<build><plugins>...</plugins></build>` section of your "pom.xml":

```xml
<plugin>
    <groupId>com.dropbox.maven</groupId>
    <artifactId>pem-converter-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <configuration>
                <input>whatever.pem</input>
                <output>${project.build.directory}/generated-resources/certs/whatever.raw</output>
            </configuration>
            <goals>
                <goal>convert</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Add this to the `<build><resources>...</resources></build>` section of your "pom.xml":

```xml
<resource>
    <directory>${project.build.directory}/generated-resources/certs</directory>
</resource>
```

## 2. In your program: 

Use `getResourceAsStream` to read the generated resource file:

```java
InputStream in = MyClass.class.getResourceAsStream("whatever.raw");
if (in == null) {
    throw new AssertionError("Couldn't find resource \"whatever.raw\");
}
```

Then load the certificates from an `InputStream` into a `KeyStore`.  Example code you can copy: [RawLoader.java](src/com/dropbox/maven/pem_converter/RawLoader.java)

## Command-Line Script

There's also a command-line script that will run the conversion.

1. `mvn compile`
2. `./run`
