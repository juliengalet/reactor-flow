<div align="center">
    <b><em>Reactor Flow</em></b><br>
    A workflow engine for Java&trade; with Reactor
</div>
<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)

[![Build Status](https://github.com/juliengalet/reactor-flow/workflows/CI%2FCD/badge.svg)](https://github.com/juliengalet/reactor-flow/actions)
[![Build Status](https://github.com/juliengalet/reactor-flow/workflows/Release/badge.svg)](https://github.com/juliengalet/reactor-flow/actions)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=alert_status&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=sqale_rating&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=reliability_rating&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=security_rating&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=vulnerabilities&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=coverage&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=juliengalet_reactor-flow&metric=ncloc&branch=master)](https://sonarcloud.io/dashboard?id=juliengalet_reactor-flow)
</div>

# Summary

- [Usage](#usage)
    - [Add to your project](#add-to-your-project)
        - [Using Maven](#using-maven)
        - [Using Gradle](#using-gradle)
        - [Notes](#notes)
    - [How to use](#how-to-use)
    - [Flow context](#flow-context)
        - [Provided FlowContext usage](#provided-flowcontext-usage)
        - [Custom FlowContext usage](#custom-flowcontext-usage)
    - [Result report](#result-report)
        - [StepFlow level](#stepflow-level)
        - [Global level](#global-level)
    - [Errors management](#errors-management)
    - [Complex run](#complex-run)
- [Available flows](#available-flows)
    - [StepFlow](#stepflow)
    - [SequentialFlow](#sequentialflow)
    - [ConditionalFlow](#conditionalflow)
    - [SwitchFlow](#switchflow)
    - [ParallelFlow](#parallelflow)
    - [RecoverableFlow](#recoverableflow)
    - [RetryableFlow](#retryableflow)
    - [NoOpFlow](#noopflow)
- [Features to implement](#features-to-implement)

# Usage

## Add to your project

### Using Maven

You should add the following in your `pom.xml` dependencies
```xml
<dependency>
  <groupId>io.github.juliengalet</groupId>
  <artifactId>reactor-flow</artifactId>
  <version>${reactor-flow.version}</version>
</dependency>
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-core</artifactId>
  <version>${reactor.version}</version>
</dependency>
```

### Using Gradle

You should add the following in your `build.gradle` dependencies (do not forget to add mavenCentral() as repository)
```text
compile 'io.github.juliengalet:reactor-flow:${reactor-flow.version}'
compile 'io.projectreactor:reactor-core:${reactor.version}'
```

### Notes

Reactor dependency is mandatory, as it is flagged as optional in this project.
Actually, 3.4.X release version is used to build the jars (exactly 3.4.12), so you should use a similar version
(otherwise, maybe you will have mysterious issues).

## How to use

This library aims to create complex workflows using Reactor Mono and Flux. It also manages errors and a global workflow
context, in order to be able to share data between workflow steps.

Here is a global example of how to use this workflow engine (go to [Available flows](#available-flows) in order to see
how to use each flow in details)

```java
public Mono<GlobalReport<FlowContext>> example() {
  SequentialFlow<FlowContext> complexCase = SequentialFlowBuilder
      .defaultBuilder()
      .named("Complex case")
      .then(successStep("Seq 1"))
      .then(warningStep("Seq 2"))
      .then(RecoverableFlowBuilder
          .defaultBuilder()
          .named("Seq 3 > Recover")
          .tryFlow(errorStep("Seq 3 > Recover > Try"))
          .recover(successStep("Seq 3 > Recover > Recover"))
          .recoverOn(RecoverableFlowException.ALL)
          .build()
      )
      .then(ParallelFlowBuilder
          .defaultBuilder()
          .named("Seq 4 > Parallel")
          .parallelize(List.of(
              successStep("Seq 4 > Parallel > 1"),
              successStep("Seq 4 > Parallel > 2"),
              successStep("Seq 4 > Parallel > 3")
          ))
          .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
          .build()
      )
      .then(ConditionalFlowBuilder
          .defaultBuilder()
          .named("Seq 5 > Conditional")
          .condition(context -> Objects.nonNull(context.get("Seq 3 > Recover > Try")))
          .caseTrue(successStep("Seq 5 > Conditional > True"))
          .caseFalse(successStep("Seq 5 > Conditional > False"))
          .build()
      )
      .doFinally(SwitchFlowBuilder
          .defaultBuilder()
          .named("Finally > Switch")
          .switchCondition(context -> (String) context.get("Seq 2"))
          .switchCase("Seq 1", successStep("Finally > Switch > Seq 1"))
          .switchCase("Seq 2", successStep("Finally > Switch > Seq 2"))
          .defaultCase(successStep("Finally > Switch > Default"))
          .build()
      )
      .build();

  return complexCase.run(FlowContext.create());
}

private Flow<FlowContext> successStep(String name) {
  return StepFlowBuilder
      .defaultBuilder()
      .named(name)
      .execution(((context, metadata) -> {
        context.put(name, name);
        return Mono.just(Report.success(context));
      }))
      .build();
}

private Flow<FlowContext> warningStep(String name) {
  return StepFlowBuilder
      .defaultBuilder()
      .named(name)
      .execution(((context, metadata) -> {
        context.put(name, name);
        return Mono.just(Report.successWithWarning(context, new FlowTechnicalException(name)));
      }))
      .build();
}

private Flow<FlowContext> errorStep(String name) {
  return StepFlowBuilder
      .defaultBuilder()
      .named(name)
      .execution(((context, metadata) -> {
        context.put(name, name);
        return Mono.just(Report.error(context, new FlowTechnicalException(name)));
      }))
      .build();
}
```

If you log the result using

```java
return complexCase.run(FlowContext.create())
    .doOnNext(globalReport->{
      System.out.println(globalReport.toPrettyString());
      System.out.println(globalReport.toPrettyExceptionsRaisedString());
      System.out.println(globalReport.getContext().toPrettyString());
      System.out.println(globalReport.toPrettyTreeString());
    });
```

You should expect to have something like this

```text
Summary
WARNING - SequentialFlow named Complex case ended in 341.45 ms (1594138273)

Errors and warnings
WARNING - TECHNICAL exception occurred on StepFlow named Seq 2 with message Seq 2 (1617838096)
RECOVERED - TECHNICAL exception occurred on StepFlow named Seq 3 > Recover > Try with message Seq 3 > Recover > Try (138776324)

Context
Seq 2 - Seq 2
Seq 1 - Seq 1
Seq 4 > Parallel > 1 - Seq 4 > Parallel > 1
Seq 4 > Parallel > 2 - Seq 4 > Parallel > 2
Seq 4 > Parallel > 3 - Seq 4 > Parallel > 3
Seq 3 > Recover > Try - Seq 3 > Recover > Try
Seq 5 > Conditional > True - Seq 5 > Conditional > True
Finally > Switch > Seq 2 - Finally > Switch > Seq 2
Seq 3 > Recover > Recover - Seq 3 > Recover > Recover

Flow tree
WARNING - SequentialFlow named Complex case ended in 341.45 ms (1594138273)
    SUCCESS - StepFlow named Seq 1 ended in 3.97 ms (2050339061)
    WARNING - StepFlow named Seq 2 ended in 0.36 ms (1617838096)
    SUCCESS - RecoverableFlow named Seq 3 > Recover ended in 1.63 ms (1579957528)
        ERROR - StepFlow named Seq 3 > Recover > Try ended in 0.62 ms (138776324)
        SUCCESS - StepFlow named Seq 3 > Recover > Recover ended in 0.24 ms (750029115)
    SUCCESS - ParallelFlow named Seq 4 > Parallel ended in 9.67 ms (214187874)
        SUCCESS - StepFlow named Seq 4 > Parallel > 1 ended in 7.67 ms (1528923159)
        SUCCESS - StepFlow named Seq 4 > Parallel > 2 ended in 8.00 ms (1683662486)
        SUCCESS - StepFlow named Seq 4 > Parallel > 3 ended in 8.21 ms (1823409783)
    SUCCESS - ConditionalFlow named Seq 5 > Conditional ended in 1.82 ms (1094523823)
        SUCCESS - StepFlow named Seq 5 > Conditional > True ended in 0.31 ms (384515747)
        IGNORED - StepFlow named Seq 5 > Conditional > False ended in 0.00 ms (657736958)
    SUCCESS - SwitchFlow named Finally > Switch ended in 1.74 ms (867988177)
        SUCCESS - StepFlow named Finally > Switch > Seq 2 ended in 0.33 ms (443934570)
        IGNORED - StepFlow named Finally > Switch > Seq 1 ended in 0.00 ms (1428475041)
        IGNORED - StepFlow named Finally > Switch > Default ended in 0.00 ms (1345483087)
```

**Note -** You should call `toPrettyString()` to log a colorized version of the result, 
and `toString()` to log a non colorized version of the result.

## Flow context

The context is used in a workflow to share data between each step.

You can either use the provided [FlowContext](src/main/java/io/github/juliengalet/reactorflow/report/FlowContext.java) class, 
or extend it with a custom implementation, if you don't want to directly deal with the **Map<String, Object>** used inside it.

### Provided FlowContext usage

There are two ways to create a provided [FlowContext](src/main/java/io/github/juliengalet/reactorflow/report/FlowContext.java)
- Create an empty one
  ```java
  FlowContext.create();
  ```
- Create a context from an initial **Map<String, Object>**
  ```java
  FlowContext.createFrom(Map.of("key", "value"));
  ```

### Custom FlowContext usage

If you don't want to directly access the **Map<String, Object>**, you can extend [FlowContext](src/main/java/io/github/juliengalet/reactorflow/report/FlowContext.java) class,
and add methods to wrap access to the **Map<String, Object>**.

This way, you can type safe access the **Map<String, Object>**.

```java
public static class CustomContext extends FlowContext {
  private static final String LIST_KEY = "LIST_KEY";

  @SuppressWarnings("unchecked")
  public List<String> getList() {
    return (List<String>) super.get(LIST_KEY);
  }

  public void setList(List<String> list) {
    super.put(LIST_KEY, list);
  }
}
```

**Note -** If you want to be able to infer your custom context type in the builders, you need to use them like this

```java
ConditionalFlowBuilder
    // Create a builder with explicitly specified context type.  
    .builderForContextOfType(CustomContext.class)
    .named("Name")
    // Here, customContext variable has CustomContext type.
    .condition(customContext -> customContext.getList().isEmpty())
    // Assuming that trueFlow and falseFlow variables already exist.    
    .caseTrue(trueFlow)
    .caseFalse(falseFlow)
    .build();
```

## Result report

Reports are used at two levels.

### StepFlow level

The [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java) level, using [Report](src/main/java/io/github/juliengalet/reactorflow/report/Report.java) class.
A [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java) execution (see [Available flows](#available-flows) - [StepFlow](#stepflow) for more details), 
should return a [Report](src/main/java/io/github/juliengalet/reactorflow/report/Report.java).

A report can be built using many static methods :
- **Build a success report**
  ```java
  Report.success(flowContext)
  ```
  Flow context should be the previous one, updated or not, passed as a parameter in StepFlow execution.
  It can also be a fresh new one, but note the new one will be used for each Flow that follows the one returning it.
  
- **Build a success report with warning(s)**
  ```java
  Report.successWithWarning(flowContext, warnings)
  ```
  Variable warnings can be a single exception, or a list of exceptions.
  All exceptions should extend [FlowException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowException.java) class.
  If you want to use the retry or recover on Functional/Technical exception, you should extend
  [FlowTechnicalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java) or
  [FlowFunctionalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowFunctionalException.java) classes, 
  themselves extending [FlowException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowException.java).
-  **Build an error report**
  ```java
  Report.error(flowContext, errors)
  ```
  Rules are same than for previous methods.
-  **Build an error report with warnings**
  ```java
  Report.error(flowContext, errors, warnings)
  ```
  Rules are same than for previous methods.

**Note -** If your [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java) brutally throw, it will be caught as if you do this
```java
FlowException rawError = throwable instanceof FlowException ?
  (FlowException) throwable :
  new FlowTechnicalException(throwable, throwable.getMessage());
return Mono.just(Report.error(context, rawError));
```

### Global level

The global level, using [GlobalReport](src/main/java/io/github/juliengalet/reactorflow/report/GlobalReport.java) class.
A Flow, whatever flow type it is, will emit a [GlobalReport](src/main/java/io/github/juliengalet/reactorflow/report/GlobalReport.java) instance when ran.

This object contains all the data needed to analyse the result of a Flow.

- **The context**
  ```java
  globalReport.getContext();
  ```
  It will return the [FlowContext](src/main/java/io/github/juliengalet/reactorflow/report/FlowContext.java) after all the steps are executed.
  If you use a builder with a custom context type, it will be inferred.
  
- **The status**
  ```java
  globalReport.getStatus();
  ```
  It will return the status of the Flow. There are 3 possibilities :
  - `SUCCESS` - If all is in success 
    (note that if an error is recovered after a retry or a recover using corresponding Flows, it will be considered as a success. See [Errors management](#errors-management) for more details)
  - `WARNING` - If at least one warning is raised from any sub Flows.
  - `ERROR` - If there is non recovered error.
  
- **The name**
  ```java
  globalReport.getName();
  ```
  It will return the name of the root Flow.

- **The duration**
  ```java
  globalReport.getDurationInMillis();
  ```
  It will return the duration of your Flow (including all its sub Flows) in milliseconds.

- **The errors**
  ```java
  globalReport.getAllErrors();
  ```
  It will return all the errors raised during your Flow.

- **The warnings**
  ```java
  globalReport.getAllWarnings();
  ```
  It will return all the warnings raised during your Flow.

- **The recovered errors**
  ```java
  globalReport.getAllRecoveredErrors();
  ```
  It will return all the recovered errors raised during your Flow (See [Errors management](#errors-management) for more details).

- **To string methods**
  - Get a summary
    ```java
    globalReport.toString();
    globalReport.toPrettyString(); // Same with colors
    ```
    Example
    ```text
    Summary
    WARNING - SequentialFlow named Complex case ended in 283.22 ms (1261031890)
    ```
  - Get a string representation of the errors
    ```java
    globalReport.toExceptionsRaisedString();
    globalReport.toPrettyExceptionsRaisedString(); // Same with colors
    ```
    Example
    ```text
    Errors and warnings
    WARNING - TECHNICAL exception occurred on StepFlow named Seq 2 with message Seq 2 (764419760)
    RECOVERED - TECHNICAL exception occurred on StepFlow named Seq 3 > Recover > Try with message Seq 3 > Recover > Try (1000966072)
    ```
  - Get a string representation of all the sub Flows contained in the Flow
    ```java
    globalReport.toTreeString();
    globalReport.toPrettyTreeString(); // Same with colors
    ```
    Example
    ```text
    Context
    Seq 2 - Seq 2
    Seq 1 - Seq 1
    Seq 4 > Parallel > 1 - Seq 4 > Parallel > 1
    Seq 4 > Parallel > 2 - Seq 4 > Parallel > 2
    Seq 4 > Parallel > 3 - Seq 4 > Parallel > 3
    Seq 3 > Recover > Try - Seq 3 > Recover > Try
    Seq 5 > Conditional > True - Seq 5 > Conditional > True
    Finally > Switch > Seq 2 - Finally > Switch > Seq 2
    Seq 3 > Recover > Recover - Seq 3 > Recover > Recover
    ```
  - Get a string representation of the **Map<String, Object>** contained in the context (customized or not)
    ```java
    globalReport.getContext().toString();
    globalReport.getContext().toPrettyString(); // Same with colors
    ```
    Example
    ```text
    Flow tree
    WARNING - SequentialFlow named Complex case ended in 283.22 ms (1261031890)
    SUCCESS - StepFlow named Seq 1 ended in 4.21 ms (1926004335)
    WARNING - StepFlow named Seq 2 ended in 0.36 ms (764419760)
    SUCCESS - RecoverableFlow named Seq 3 > Recover ended in 1.97 ms (95396809)
        ERROR - StepFlow named Seq 3 > Recover > Try ended in 0.72 ms (1000966072)
        SUCCESS - StepFlow named Seq 3 > Recover > Recover ended in 0.26 ms (1912821769)
    SUCCESS - ParallelFlow named Seq 4 > Parallel ended in 9.87 ms (151593342)
        SUCCESS - StepFlow named Seq 4 > Parallel > 1 ended in 8.28 ms (405215542)
        SUCCESS - StepFlow named Seq 4 > Parallel > 2 ended in 8.40 ms (1617838096)
        SUCCESS - StepFlow named Seq 4 > Parallel > 3 ended in 8.46 ms (138776324)
    SUCCESS - ConditionalFlow named Seq 5 > Conditional ended in 1.48 ms (1208442275)
        SUCCESS - StepFlow named Seq 5 > Conditional > True ended in 0.26 ms (1758008124)
        IGNORED - StepFlow named Seq 5 > Conditional > False ended in 0.00 ms (2050339061)
    SUCCESS - SwitchFlow named Finally > Switch ended in 1.08 ms (1579957528)
        SUCCESS - StepFlow named Finally > Switch > Seq 2 ended in 0.20 ms (750029115)
        IGNORED - StepFlow named Finally > Switch > Seq 1 ended in 0.00 ms (214187874)
        IGNORED - StepFlow named Finally > Switch > Default ended in 0.00 ms (1528923159)
    ```
    
## Errors management

TODO

## Complex run

It is possible to run Flows from other reactor Mono or Flux.
TODO

# Available flows

## StepFlow

You can build a [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java)
using [StepFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/StepFlowBuilder.java) class.

A [StepFlow](./src/main/java/fr/jtools/reactorflow/flow/StepFlow.java) is a unit of work.

It is composed of a name and an execution.
The execution is represented by the [Step](./src/main/java/fr/jtools/reactorflow/flow/Step.java) or [StepWithMetadata](./src/main/java/fr/jtools/reactorflow/flow/StepWithMetadata.java) interfaces.
It contains

Using the builder, the syntax is the following
```java
// Build a StepFlow using default FlowContext and Metadata<Object> types.
StepFlowBuilder
.defaultBuilder()
.named("Step")
.execution(((flowContext, metadata) -> Mono.just(Report.success(flowContext))))
.build();

// Build a StepFlow using a custom context and default Metadata<Object> types.
StepFlowBuilder
.builderForContextOfType(CustomContext.class)
.named("Step")
.execution(((customContext, metadata) -> Mono.just(Report.success(customContext))))
.build();

// Build a StepFlow using default FlowContext and a custom Metadata<String> types.
StepFlowBuilder
.builderForMetadataType(String.class)
.named("Step")
.execution(((flowContext, metadata) -> Mono.just(Report.success(flowContext))))
.build();

// Build a StepFlow using a custom context and a custom Metadata<String> types.
StepFlowBuilder
.builderForTypes(CustomContext.class, String.class)
.named("Step")
.execution(((customContext, metadata) -> Mono.just(Report.success(customContext))))
.build();
```

## SequentialFlow

You can build a [SequentialFlow](src/main/java/io/github/juliengalet/reactorflow/flow/SequentialFlow.java)
using [SequentialFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/SequentialFlowBuilder.java) class.

## ConditionalFlow

You can build a [ConditionalFlow](src/main/java/io/github/juliengalet/reactorflow/flow/ConditionalFlow.java)
using [ConditionalFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/ConditionalFlowBuilder.java) class.

## SwitchFlow

You can build a [SwitchFlow](src/main/java/io/github/juliengalet/reactorflow/flow/SwitchFlow.java)
using [SwitchFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/SwitchFlowBuilder.java) class.

## ParallelFlow

You can build a [ParallelFlow](src/main/java/io/github/juliengalet/reactorflow/flow/ParallelFlow.java)
using [ParallelFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/ParallelFlowBuilder.java) class.

## RecoverableFlow

You can build a [RecoverableFlow](src/main/java/io/github/juliengalet/reactorflow/flow/RecoverableFlow.java)
using [RecoverableFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/RecoverableFlowBuilder.java) class.

## RetryableFlow

You can build a [RetryableFlow](src/main/java/io/github/juliengalet/reactorflow/flow/RetryableFlow.java)
using [RetryableFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/RetryableFlowBuilder.java) class.

## NoOpFlow

You can build a [NoOpFlow](src/main/java/io/github/juliengalet/reactorflow/flow/NoOpFlow.java) using `NoOpFlow.named(String name)`
method.

# Features to implement

- [ ] Default parameters for builders
- [ ] Use a more generic way to recover/retry flow on errors (actually it is mandatory to
  extend [FlowFunctionalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowFunctionalException.java)
  or [FlowTechnicalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java) in order to
  be able to retry on Functional or Technical errors)
