# RefactoringAnalyser

RefactoringAnalyser is a tool written in Java that can detect if there is a 
refactoring in a specific line of a merged Java file.

RefactoringAnalyser finds  refactorings trough the use of existing tools as:
* RefactoringMiner (https://github.com/tsantalis/RefactoringMiner)
* RefDiff (https://github.com/aserg-ufmg/RefDiff)


## Getting started

Before building the project, make sure you have git and a Java Development Kit (JDK) version 8 installed in your system. Also, set the JAVA_HOME environment variable to point to the installation directory of the desired JDK.

```
git clone https://github.com/victorlira/refactoring-analyser.git
```

Use gradle to create the Eclipse IDE project metadata. For example, in Windows systems:

```
cd refactoring-analyser
gradlew eclipse
```

Note that in Linux or Mac you should run `./gradlew eclipse` to run the gradle wrapper.

You can detect refactorings in a certain repository/commit/merge/line using the following code:

```java
private static void runExamples()  {
    RefactoringEngine engine = new RefactoringEngine();
    
    String repositoryUrl = "https://github.com/cucumber/cucumber-jvm.git";
    String mergeCommit = "4505c156b6267c1b760deec570ddbfe047b42aa9";
    String className = "cuke4duke.internal.java.JavaLanguage";
    int line = 36;
    
    RefactoringResult result = engine.run(repositoryUrl, mergeCommit, className , line);

    System.out.println("Is refactoring: " + result.isRefactoring());
}
```

## Maven artifacts

RefactoringAnalyser artifacts are also published to Maven central repository under the group id `br.ufpe.cin.ines`.

To use RefactoringAnalyser in Java, you should add the following dependency to your `pom.xml`:

```
<dependency>
  <groupId>br.ufpe.cin.ines</groupId>
  <artifactId>refactoring-analyser</artifactId>
  <version>1.0.0</version>
</dependency>
```
