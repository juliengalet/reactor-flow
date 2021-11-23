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

[![Maven Central](https://img.shields.io/maven-central/v/io.github.juliengalet/reactor-flow.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.juliengalet%22%20AND%20a:%22reactor-flow%22)
[![javadoc](https://javadoc.io/badge2/io.github.juliengalet/reactor-flow/javadoc.svg)](https://javadoc.io/doc/io.github.juliengalet/reactor-flow)
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
        - [Basic usage](#basic-usage)
        - [How to recover errors](#how-to-recover-errors)
        - [SequentialFlow finally Flow](#sequentialflow-finally-flow)
    - [Complex run](#complex-run)
- [Available flows](#available-flows)
    - [StepFlow](#stepflow)
    - [SequentialFlow](#sequentialflow)
    - [ConditionalFlow](#conditionalflow)
    - [SwitchFlow](#switchflow)
    - [ParallelFlow](#parallelflow)
        - [Parallelize a unique flow](#parallelize-a-unique-flow)
        - [Parallelize defined flows](#parallelize-defined-flows)
        - [Merge strategy](#merge-strategy)
    - [RecoverableFlow](#recoverableflow)
    - [RetryableFlow](#retryableflow)
    - [NoOpFlow](#noopflow)
- [Complex example using metadata](#complex-example-using-metadata)
    - [Java class](#java-class)
    - [Expected result](#expected-result)
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
how to use each flow in details). You can nest Flow as you want, as each flow extends [Flow](src/main/java/io/github/juliengalet/reactorflow/flow/Flow.java) class.
However, you will always need to finish by [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java)s, as it is the unique flow type that does not need nested flows.

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
  
**Get a summary**

```java
globalReport.toString();
globalReport.toPrettyString(); // Same with colors
```

Example

```text
Summary
WARNING - SequentialFlow named Complex case ended in 283.22 ms (1261031890)
```

**Get a string representation of the errors**

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

**Get a string representation of all the sub Flows contained in the Flow**

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

**Get a string representation of the **Map<String, Object>** contained in the context (customized or not)**

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

### Basic usage

There are 3 types of errors, each extending [FlowException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowException.java) class.
Each type can also be extended for custom usage, if needed.

- [FlowTechnicalException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java)
  This type should handle "Technical" errors, as an unavailable database, or a network issue. Generally, it corresponds to an error that should not be reproduced if you retry.
  
- [FlowFunctionalException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowFunctionalException.java)
  This type should handle "Functional" errors, as a missing business data. Generally, it corresponds to an error that should be reproduced, even if we retry many times.
  
- [FlowBuilderException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowBuilderException.java)
  This type is used by builders, in order to throw an error if the build parameters are invalid.
  
As a consequence, for now, you should always use [FlowTechnicalException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java) or
[FlowFunctionalException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowFunctionalException.java) (or classes that extend them) when returning exceptions in your
[Report](./src/main/java/io/github/juliengalet/reactorflow/report/Report.java) errors or warning.

```java
StepFlowBuilder
    .defaultBuilder()
    .named("Step")
    .execution(((flowContext, metadata) -> Mono.just(Report.error(flowContext, new FlowTechnicalException("Error message")))))
    .build();
```

Note that all the errors or warnings returned during the run of a flow are added to the final [GlobalReport](./src/main/java/io/github/juliengalet/reactorflow/report/GlobalReport.java),
and can be accessed with `globalReport.getAllErrors()` and `globalReport.getAllWarnings()`.

If you return a raw Mono.error(), or a raw exception, it will be converted to a [FlowTechnicalException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java)
if your exception does not extend [FlowException](./src/main/java/io/github/juliengalet/reactorflow/exception/FlowException.java).

```java
StepFlowBuilder
    .defaultBuilder()
    .named("Step")
    .execution(((flowContext, metadata) -> Mono.error(new RuntimeException("Error message"))))
    .build();

StepFlowBuilder
    .defaultBuilder()
    .named("Step")
    .execution(((flowContext1, metadata) -> { throw new RuntimeException("Error message"); }))
    .build();

// Those two examples will produce the same result than
StepFlowBuilder
    .defaultBuilder()
    .named("Step")
    .execution(((flowContext, metadata) -> Mono.just(Report.error(flowContext, new FlowTechnicalException("Error message")))))
    .build();
```

### How to recover errors

If you use [RecoverableFlow](#recoverableflow) or [RetryableFlow](#retryableflow), if after a retry, or after the recover, there is a success,
then the errors will be removed of the global error list, and added to the recovered errors list `globalReport.getAllRecoveredErrors()`

If warnings occurred during the fail flow(s), the warnings will not be moved, but the concerned Retryable/Recoverable flow will have the `SUCCESS` status.
If new warnings are returned during the recover or the retry, then, the Retryable/Recoverable flow will have the `WARNING` status. 

Examples :

- A successful recovered flow (with warnings and errors during tryFlow)

```java
RecoverableFlowBuilder
    .defaultBuilder()
    .named("Recover success")
    .tryFlow(StepFlowBuilder
        .defaultBuilder()
        .named("Failure")
        .execution((ctx, metadata) -> Mono.just(Report.errorWithWarning(ctx, new FlowTechnicalException("Error"), new FlowTechnicalException("Warning"))))
        .build()
    )
    .recover(StepFlowBuilder
        .defaultBuilder()
        .named("Success")
        .execution((ctx, metadata) -> Mono.just(Report.success(ctx)))
        .build()
    )
    .recoverOn(RecoverableFlowException.ALL)
    .build();
```

The result will be something like (note the global `SUCCESS` status)

```text
Summary
SUCCESS - RecoverableFlow named Recover success ended in 126.44 ms (2107443224)

Errors and warnings
WARNING - TECHNICAL exception occurred on StepFlow named Failure with message Warning (609656250)
RECOVERED - TECHNICAL exception occurred on StepFlow named Failure with message Error (609656250)

Context


Flow tree
SUCCESS - RecoverableFlow named Recover success ended in 126.44 ms (2107443224)
    ERROR - StepFlow named Failure ended in 93.98 ms (609656250)
    SUCCESS - StepFlow named Success ended in 0.20 ms (1231799381)
```

- A successful with warning recovered flow (with warnings and errors during tryFlow)

```java
RecoverableFlowBuilder
    .defaultBuilder()
    .named("Recover success")
    .tryFlow(StepFlowBuilder
        .defaultBuilder()
        .named("Failure")
        .execution((ctx, metadata) -> Mono.just(Report.errorWithWarning(ctx, new FlowTechnicalException("Error"), new FlowTechnicalException("Warning"))))
        .build()
    )
    .recover(StepFlowBuilder
        .defaultBuilder()
        .named("Success")
        .execution((ctx, metadata) -> Mono.just(Report.successWithWarning(ctx, new FlowTechnicalException("Warning during recover"))))
        .build()
    )
    .recoverOn(RecoverableFlowException.ALL)
    .build();
```

The result will be something like (note the global `WARNING` status)

```text
Summary
WARNING - RecoverableFlow named Recover success ended in 1.00 ms (1984513847)

Errors and warnings
WARNING - TECHNICAL exception occurred on StepFlow named Failure with message Warning (1241529534)
WARNING - TECHNICAL exception occurred on StepFlow named Success with message Warning during recover (1082309267)
RECOVERED - TECHNICAL exception occurred on StepFlow named Failure with message Error (1241529534)

Context


Flow tree
WARNING - RecoverableFlow named Recover success ended in 1.00 ms (1984513847)
    ERROR - StepFlow named Failure ended in 0.18 ms (1241529534)
    WARNING - StepFlow named Success ended in 0.14 ms (1082309267)
```

### SequentialFlow finally Flow

If you use [SequentialFlow](#sequentialflow), with the `.doFially(flow)` when building it, then there is an additional error management.
Indeed, the finally flow is executed, even if there are errors during a previous `.then(flow)` 
(note that the global sequential flow will still have the `ERROR` status, the idea is that you will be able to do some operations at the end of a sequence, whether the global flow has succeeded or not).

You can get the specific errors that have been raised during the sequential flow and its sub-flows in the [Metadata](./src/main/java/io/github/juliengalet/reactorflow/report/Metadata.java)
instance passed as second argument in a [StepFlow](#stepflow) execution.

Example :

```java
SequentialFlowBuilder
    .defaultBuilder()
    .named("Sequential")
    .then(StepFlowBuilder
        .defaultBuilder()
        .named("Failure")
        .execution((ctx, metadata) -> Mono.just(Report.errorWithWarning(ctx, new FlowTechnicalException("Error"), new FlowTechnicalException("Warning"))))
        .build()
    )
    .doFinally(StepFlowBuilder
        .defaultBuilder()
        .named("Finally")
        .execution((ctx, metadata) -> {
          System.out.println(metadata.getErrors()); // Will log only the error raised by "Failure" StepFlow, even if you include all this sequence in a more complex flow.
          System.out.println(metadata.getWarnings()); // Will log only the warning raised by "Failure" StepFlow, even if you include all this sequence in a more complex flow.
          return Mono.just(Report.success(ctx));
        })
        .build()
    )
    .build();
```

The result will be something like (note the global `ERROR` status)

```text
Summary
ERROR - SequentialFlow named Sequential ended in 162.42 ms (1293680734)

Errors and warnings
ERROR - TECHNICAL exception occurred on StepFlow named Failure with message Error (1076641925)
WARNING - TECHNICAL exception occurred on StepFlow named Failure with message Warning (1076641925)

Context


Flow tree
ERROR - SequentialFlow named Sequential ended in 162.42 ms (1293680734)
    ERROR - StepFlow named Failure ended in 6.57 ms (1076641925)
    SUCCESS - StepFlow named Finally ended in 1.66 ms (1904783235)
```

## Complex run

It is possible to run Flows from other reactor Mono or Flux.

- From Mono
```java
Mono<FlowContext> flowContextMono = Mono.just(FlowContext.createFrom(Map.of("Mono", "Mono")));
parallelFlow.run(flowContextMono);
```

- From Flux
```java
Flux<FlowContext> flowContextFlux = Flux.just(
    FlowContext.createFrom(Map.of("Flux 1", "Flux 1")),
    FlowContext.createFrom(Map.of("Flux 2", "Flux 2")),
    FlowContext.createFrom(Map.of("Flux 3", "Flux 3"))
);
// Parallelize runs using default concurrency of reactor
parallelFlow.run(flowContextFlux);
// Parallelize runs with maximum 2 simultaneous runs
parallelFlow.run(flowContextFlux, 2);
// Do not parallelize runs
parallelFlow.runSequential(flowContextFlux);
```

**WARNING -** When you use Flux, you need to take care about errors in your Flux input, as it is outside the library errors management,
and so, it can break the reactor chain. Example :
```java
Flux<FlowContext> flowContextFlux = Flux
    .merge(
        Flux.just(Mono.just(FlowContext.createFrom(Map.of("Flux 1", "Flux 1"))))
            .then(Mono.<FlowContext>error(new RuntimeException("Error")))
    )
    .onErrorResume(throwable -> {
        System.out.println(throwable.getMessage());
        return Mono.empty();
    });
```

# Available flows

## StepFlow

You can build a [StepFlow](src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java)
using [StepFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/StepFlowBuilder.java) class.

A [StepFlow](./src/main/java/io/github/juliengalet/reactorflow/flow/StepFlow.java) is a unit of work.

It is composed of a name and an execution.
The execution is represented by the [Step](./src/main/java/io/github/juliengalet/reactorflow/flow/Step.java) or [StepWithMetadata](./src/main/java/io/github/juliengalet/reactorflow/flow/StepWithMetadata.java) interfaces.

**Note -** See [ParallelFlow](#parallelflow) and [SequentialFlow](#sequentialflow) to see how metadata are used.

Using the builder, the syntax is the following

```java
// Build using default FlowContext and Metadata<Object> types.
StepFlowBuilder
    .defaultBuilder()
    .named("Step")
    .execution(((flowContext, metadata) -> Mono.just(Report.success(flowContext))))
    .build();

// Build using a custom context and default Metadata<Object> types.
StepFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Step")
    .execution(((customContext, metadata) -> Mono.just(Report.success(customContext))))
    .build();

// Build using default FlowContext and a custom Metadata<String> types.
StepFlowBuilder
    .builderForMetadataType(String.class)
    .named("Step")
    .execution(((flowContext, metadata) -> Mono.just(Report.success(flowContext))))
    .build();

// Build using a custom context and a custom Metadata<String> types.
StepFlowBuilder
    .builderForTypes(CustomContext.class, String.class)
    .named("Step")
    .execution(((customContext, metadata) -> Mono.just(Report.success(customContext))))
    .build();
```

You can also extend those two classes if you don't want to use the builder, or if you want to inject services in 

- [DefaultStep](./src/main/java/io/github/juliengalet/reactorflow/flow/DefaultStep.java) in order to create steps with default metadata.

```java
public class MyStep extends DefaultStep<FlowContext> {
  private final String name;

  /**
   * You can add a constructor, or even use lombok constructors annotations like @RequiredArgsConstructor.
   * So, you will be able to inject services if you want.
   *
   * @param name A name
   */
  public MyStep(String name) {
    this.name = name;
  }

  @Override
  protected String getName() {
    return this.name;
  }

  @Override
  protected Mono<Report<FlowContext>> getExecution(FlowContext context, Metadata<Object> metadata) {
    return Mono.just(Report.success(context));
  }
}

// Usage
SequentialFlow<FlowContext> sequentialFlow = SequentialFlowBuilder
      .defaultBuilder()
      .named("Sequential")
      .then(step.getStep())
      .build();
```
  
- [DefaultMetadataStep](./src/main/java/io/github/juliengalet/reactorflow/flow/DefaultMetadataStep.java) in order to create steps with custom metadata.

```java
public class MyStepWithMetadata extends DefaultMetadataStep<FlowContext, String> {
  private final String name;

  /**
   * You can add a constructor, or even use lombok constructors annotations like @RequiredArgsConstructor.
   * So, you will be able to inject services if you want.
   *
   * @param name A name
   */
  public MyStepWithMetadata(String name) {
    this.name = name;
  }

  @Override
  protected String getName() {
    return this.name;
  }

  @Override
  protected Mono<Report<FlowContext>> getExecution(FlowContext context, Metadata<String> metadata) {
    return Mono.just(Report.success(context));
  }
}

// Usage
ParallelFlow<FlowContext, String> parallelFlow = ParallelFlowBuilder
      .builderForMetadataType(String.class)
      .named("Parallel")
      .parallelizeFromArray(flowContext -> (List<String>) flowContext.get("List"))
      .parallelizedFlow(stepWithMetadata.getStep())
      .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
      .build();
```

## SequentialFlow

You can build a [SequentialFlow](src/main/java/io/github/juliengalet/reactorflow/flow/SequentialFlow.java)
using [SequentialFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/SequentialFlowBuilder.java) class.

Using the builder, the syntax is the following (assuming that sub-flows already exist)

```java
// Build using default FlowContext.
SequentialFlowBuilder
    .defaultBuilder()
    .named("Sequential")
    .then(thenFlowSuccess) // Will be executed
    .then(thenFlowError) // Will be executed
    .then(thenFlowSucess2) // Will NOT be executed as thenFlowError has returned error(s)
    .doFinally(finallyFlow) // Will always be executed, even if one then flow has returned error(s)
    .build();

// Build using a custom context.
SequentialFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Sequential")
    .then(thenFlowSuccess) // Will be executed
    .then(thenFlowError) // Will be executed
    .then(thenFlowSucess2) // Will NOT be executed as thenFlowError has returned error(s)
    .doFinally(finallyFlow) // Will always be executed, even if one then flow has returned error(s)
    .build();
```

**Note -** See [SequentialFlow finally Flow](#sequentialflow-finally-flow) to know how to deal with errors in finally flow.

If you log the result of one of the previous example, you should obtain something like (context is empty as it was a test)

```text
Summary
ERROR - SequentialFlow named Sequential ended in 187.24 ms (1574877131)

Errors and warnings
ERROR - FUNCTIONAL exception occurred on StepFlow named Failure with message Error (2083999882)

Context


Flow tree
ERROR - SequentialFlow named Sequential ended in 187.24 ms (1574877131)
    SUCCESS - StepFlow named Success ended in 3.20 ms (809300666)
    ERROR - StepFlow named Failure ended in 0.40 ms (2083999882)
    IGNORED - StepFlow named Success 2 ended in 0.00 ms (1984513847)
    SUCCESS - StepFlow named Finally ended in 0.31 ms (1241529534)
```

## ConditionalFlow

You can build a [ConditionalFlow](src/main/java/io/github/juliengalet/reactorflow/flow/ConditionalFlow.java)
using [ConditionalFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/ConditionalFlowBuilder.java) class.

Using the builder, the syntax is the following (assuming that trueFlow and caseFlow are already defined)

```java
// Build using default FlowContext.
ConditionalFlowBuilder
    .defaultBuilder()
    .named("Conditional")
    .condition(flowContext -> Objects.nonNull(flowContext.get("any")))
    .caseTrue(trueFlow)
    .caseFalse(falseFlow)
    .build();

// Build using a custom context.
ConditionalFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Conditional")
    .condition(customContext -> Objects.nonNull(customContext.get("any")))
    .caseTrue(trueFlow)
    .caseFalse(falseFlow)
    .build();
```

## SwitchFlow

You can build a [SwitchFlow](src/main/java/io/github/juliengalet/reactorflow/flow/SwitchFlow.java)
using [SwitchFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/SwitchFlowBuilder.java) class.

Using the builder, the syntax is the following (assuming that case1Flow, case2Flow, and defaultFlow are already defined)

```java
// Build using default FlowContext.
SwitchFlowBuilder
    .defaultBuilder()
    .named("Switch")
    .switchCondition(flowContext -> (String) flowContext.get("any"))
    .switchCase("Case 1", case1Flow)
    .switchCase("Case 2", case2Flow)
    .defaultCase(defaultFlow)
    .build();

// Build using a custom context.
SwitchFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Switch")
    .switchCondition(customContext -> customContext.getSwitchProperty())
    .switchCase("Case 1", case1Flow)
    .switchCase("Case 2", case2Flow)
    .defaultCase(defaultFlow)
    .build();
```

## ParallelFlow

You can build a [ParallelFlow](src/main/java/io/github/juliengalet/reactorflow/flow/ParallelFlow.java)
using [ParallelFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/ParallelFlowBuilder.java) class.

### Parallelize a unique flow

Use this solution if you want to iterate over an array extracted from the context.

With this approach, you should specify the metadata type in the builder, 
using `.builderForMetadataType(metadateType)` or `.builderForTypes(contextType, metadataType)`.

```java
// Using builderForMetadataType
ParallelFlowBuilder
    .builderForMetadataType(String.class) // Metadata type should correspond to the type of list item extracted from context
    .named("Parallel")
    .parallelizeFromArray(ctx -> (List<String>) ctx.get("list")) // As Metadata data has String type, we need to extract a List<String>
    .parallelizedFlow(flowToParallelize) // 
    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy()) // Generally you should use this
    .build();

// Using builderForTypes, with a custom context
ParallelFlowBuilder
    .builderForTypes(CustomContext.class, String.class) // Also use a custom context
    .named("Parallel")
    .parallelizeFromArray(CustomContext::getList) // Assuming the customContext.getList() returns a List<String>
    .parallelizedFlow(flowToParallelize)
    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy()) // Generally you should use this
    .build();
```

In flowToParallelize, you should propagate the metadata type in all sub-flows (specially the [StepFlow](#stepflow)s, as you can access the metadata only inside them), 
in order to have well typed flows.
If you don't do that, you will need to manually cast `metadata.getData()` result every time you need it.

Example
```java
ParallelFlow<CustomContext, String> parallelFlow = ParallelFlowBuilder
    .builderForTypes(CustomContext.class, String.class)
    .named("Parallel")
    .parallelizeFromArray(CustomContext::getList) // List contains ["Item 1", "Item 2", "Item 3"]
    .parallelizedFlow(StepFlowBuilder
        .builderForTypes(CustomContext.class, String.class)
        .named("Step")
        .execution((ctx, metadata) -> {
          String data = metadata.getData(); // As .builderForTypes(CustomContext.class, String.class) was used for the StepFlow, metadata.getData() already returns String
          ctx.put(data, data); // data is equal to "Item 1", "Item 2", or "Item 3", as if we were iterating over the array
          return Mono.just(Report.success(ctx));
        })
        .build()
    )
    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
    .build();

parallelFlow.run(new CustomContext());
```

If you log the result, you should expect something like

```text
Summary
SUCCESS - ParallelFlow named Parallel ended in 158.50 ms (1916575798)

Errors and warnings
No error or warning

Context
Item 3 - Item 3
Item 2 - Item 2
Item 1 - Item 1

Flow tree
SUCCESS - ParallelFlow named Parallel ended in 158.50 ms (1916575798)
    SUCCESS - StepFlow named Step (1) ended in 2.92 ms (51554940)
    SUCCESS - StepFlow named Step (2) ended in 0.17 ms (1399794302)
    SUCCESS - StepFlow named Step (3) ended in 0.13 ms (1924949331)
```

### Parallelize defined flows

If you want to parallelize defined flows, without iterating over an array, you will not need to specify metadata type
(except if your [ParallelFlow](#parallelflow) is itself a sub-flow of another one which need to specify metadata type, see [Complex example using metadata](#complex-example-using-metadata)).

```java
// Using default context
ParallelFlowBuilder
    .defaultBuilder()
    .named("Parallel")
    .parallelize(List.of(
        flow1,
        flow2,
        flow3
    ))
    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
    .build();

// Using custom context
ParallelFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Parallel")
    .parallelize(List.of(
        flow1,
        flow2,
        flow3
    ))
    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
    .build();
```

### Merge strategy

With [ParallelFlow](#parallelflow), you need to provide a merge strategy. As many flows are exacted in parallel, it is mandatory to know how to merge all the resulting contexts.

If you use default [Flow context](#flow-context), that uses **ConcurrentHashMap<String, Object>**, and if you don't add non thread-safe entries in the map,
you should need the default merge strategy, that takes any of the resulting context. Indeed, all parallelized flows should write to the same context instance,
so all the resulting contexts are the same instance in reality. If you use a custom context, beware of concurrent thread access.

If this does not correspond to your need, you can add a custom merge strategy, by specifically implementing the way to merge contexts, two by two.

```java
public BinaryOperator<FlowContext> customMergeStrategy() {
    // Here it is the same as the default merge strategy
    // Always return the first of two, as we assume that context1 and context2 are the object in reality
    return (context1, context2) -> context1;
}

// Same with a custom context
public BinaryOperator<CustomContext> customMergeStrategy() {
    // Here it is the same as the default merge strategy
    // Always return the first of two, as we assume that context1 and context2 are the object in reality
    return (context1, context2) -> context1;
}
```

## RecoverableFlow

You can build a [RecoverableFlow](src/main/java/io/github/juliengalet/reactorflow/flow/RecoverableFlow.java)
using [RecoverableFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/RecoverableFlowBuilder.java) class.

Using the builder, the syntax is the following (assuming that tryFlow and recoverFlow already exist)

```java
// Build using default FlowContext.
RecoverableFlowBuilder
    .defaultBuilder()
    .named("Recoverable")
    .tryFlow(tryFlow)
    .recover(recoverFlow)
    .recoverOn(RecoverableFlowException.ALL) // Retry on all exceptions
    .build();

// Build using a custom context.
RecoverableFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Recoverable")
    .tryFlow(tryFlow)
    .recover(recoverFlow)
    .recoverOn(RecoverableFlowException.FUNCTIONAL) // Retry only on Functional exceptions
    .build();
```

**Note -** You can retry on FUNCTIONAL, TECHNICAL, ALL, or NONE exceptions (see [RecoverableFlowException](./src/main/java/io/github/juliengalet/reactorflow/exception/RecoverableFlowException.java),
and [Errors management](#errors-management)).

## RetryableFlow

You can build a [RetryableFlow](src/main/java/io/github/juliengalet/reactorflow/flow/RetryableFlow.java)
using [RetryableFlowBuilder](src/main/java/io/github/juliengalet/reactorflow/builder/RetryableFlowBuilder.java) class.

Using the builder, the syntax is the following (assuming that retryFlow already exists)

```java
// Build using default FlowContext.
RetryableFlowBuilder
    .defaultBuilder()
    .named("Retryable")
    .tryFlow(retryFlow)
    .retryOn(RecoverableFlowException.ALL) // Retry on all exceptions
    .retryTimes(2) // Number of retry
    .delay(100) // Delay in milliseconds
    .build();

// Build using a custom context.
RetryableFlowBuilder
    .builderForContextOfType(CustomContext.class)
    .named("Retryable")
    .tryFlow(retryFlow)
    .retryOn(RecoverableFlowException.FUNCTIONAL) // Retry only on Functional exceptions
    .retryTimes(2) // Number of retry
    .delay(100) // Delay in milliseconds
    .build();
```

**Note -** You can retry on FUNCTIONAL, TECHNICAL, ALL, or NONE exceptions (see [RecoverableFlowException](./src/main/java/io/github/juliengalet/reactorflow/exception/RecoverableFlowException.java), 
and [Errors management](#errors-management)).

## NoOpFlow

You can build a [NoOpFlow](src/main/java/io/github/juliengalet/reactorflow/flow/NoOpFlow.java) using `NoOpFlow.named(String name)`
method.

It is a special flow, used in special cases, when you want to do nothing.

For example, you can use it if you want to apply a flow if a specific condition matches, but you want to do nothing if it does not match.

```java
ConditionalFlowBuilder
    .defaultBuilder()
    .named("Conditional")
    .condition(flowContext -> Objects.nonNull(flowContext.get("any")))
    .caseTrue(trueFlow)
    .caseFalse(NoOpFlow.named("Nothing")) // Do nothing if condition is false
    .build();
```

# Complex example using metadata

## Java class

Given this Java class

```java
package io.github.juliengalet.reactorflow;

import io.github.juliengalet.reactorflow.builder.ParallelFlowBuilder;
import io.github.juliengalet.reactorflow.builder.SequentialFlowBuilder;
import io.github.juliengalet.reactorflow.exception.FlowException;
import io.github.juliengalet.reactorflow.exception.FlowTechnicalException;
import io.github.juliengalet.reactorflow.flow.DefaultStep;
import io.github.juliengalet.reactorflow.flow.DefaultMetadataStep;
import io.github.juliengalet.reactorflow.flow.Flow;
import io.github.juliengalet.reactorflow.report.FlowContext;
import io.github.juliengalet.reactorflow.report.GlobalReport;
import io.github.juliengalet.reactorflow.report.Metadata;
import io.github.juliengalet.reactorflow.report.Report;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Example {
  public static void main(String[] args) {
    Flow<CustomContext> complexFlowUsingMetadata = ParallelFlowBuilder
        .builderForTypes(CustomContext.class, String.class)
        .named("Example")
        .parallelizeFromArray(CustomContext::getStringList)
        .parallelizedFlow(
            SequentialFlowBuilder
                .builderForContextOfType(CustomContext.class)
                .named("Sequential")
                .then(new SuccessStringMetadata("Sequential > String").getStep())
                .then(ParallelFlowBuilder
                    .builderForTypes(CustomContext.class, Integer.class)
                    .named("Sequential > Parallel")
                    .parallelizeFromArray(CustomContext::getFirstIntegerList)
                    .parallelizedFlow(new SuccessIntegerMetadata("Sequential > Parallel > Integer").getStep())
                    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
                    .build()
                )
                .then(ParallelFlowBuilder
                    .builderForTypes(CustomContext.class, Integer.class)
                    .named("Sequential > Parallel other")
                    .parallelizeFromArray(CustomContext::getSecondIntegerList)
                    .parallelizedFlow(SequentialFlowBuilder
                        .builderForContextOfType(CustomContext.class)
                        .named("Sequential > Parallel other > Sequential")
                        .then(new SuccessIntegerMetadata("Sequential > Parallel other > Sequential > Integer other").getStep())
                        .then(new Error("Sequential > Parallel other > Sequential > Error").getStep())
                        .doFinally(new SuccessFinally<>("Sequential > Parallel other > Sequential > Finally").getStep())
                        .build()
                    )
                    .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
                    .build()
                )
                .doFinally(new SuccessFinally<>("Sequential > Finally").getStep())
                .build()
        )
        .mergeStrategy(ParallelFlowBuilder.defaultMergeStrategy())
        .build();

    GlobalReport<CustomContext> globalReport = complexFlowUsingMetadata.run(new CustomContext()).block(); // Use .block() as we are in main

    System.out.println(globalReport.toPrettyString());
    System.out.println(globalReport.toPrettyTreeString());
    System.out.println(globalReport.getContext().toPrettyString());
    System.out.println(globalReport.toPrettyExceptionsRaisedString());
  }

  public static class Error extends DefaultStep<CustomContext> {
    private final String name;

    public Error(String name) {
      this.name = name;
    }

    @Override
    protected String getName() {
      return this.name;
    }

    @Override
    protected Mono<Report<CustomContext>> getExecution(CustomContext context, Metadata<Object> metadata) {
      return Mono.just(Report.error(context, new FlowTechnicalException(String.format(
          "%s (%s)",
          this.name,
          UUID.randomUUID().toString()
      ))));
    }
  }

  public static class SuccessFinally<M> extends DefaultMetadataStep<CustomContext, M> {
    private final String name;

    public SuccessFinally(String name) {
      this.name = name;
    }

    @Override
    protected String getName() {
      return this.name;
    }

    @Override
    protected Mono<Report<CustomContext>> getExecution(CustomContext context, Metadata<M> metadata) {
      String randomUUID = UUID.randomUUID().toString();
      String errorEntry = String.format(
          "Name: %s | Metadata: %s (%s) | Errors: %s | UUID: %s",
          this.name,
          metadata.getData(),
          metadata.getData().getClass().getSimpleName(),
          metadata.getErrors().stream().map(FlowException::getMessage).collect(Collectors.joining(", ")),
          randomUUID
      );
      context.put(errorEntry, randomUUID);
      return Mono.just(Report.success(context));
    }
  }

  public static class SuccessIntegerMetadata extends DefaultMetadataStep<CustomContext, Integer> {
    private final String name;

    public SuccessIntegerMetadata(String name) {
      this.name = name;
    }

    @Override
    protected String getName() {
      return this.name;
    }

    @Override
    protected Mono<Report<CustomContext>> getExecution(CustomContext context, Metadata<Integer> metadata) {
      String randomUUID = UUID.randomUUID().toString();
      String metadataEntryKey = String.format("Name: %s | Metadata: %s | UUID: %s", this.name, metadata.getData(), randomUUID);
      context.put(metadataEntryKey, randomUUID);
      return Mono.just(Report.success(context));
    }
  }

  public static class SuccessStringMetadata extends DefaultMetadataStep<CustomContext, String> {
    private final String name;

    public SuccessStringMetadata(String name) {
      this.name = name;
    }

    @Override
    protected String getName() {
      return this.name;
    }

    @Override
    protected Mono<Report<CustomContext>> getExecution(CustomContext context, Metadata<String> metadata) {
      String randomUUID = UUID.randomUUID().toString();
      String metadataEntryKey = String.format("Name: %s | Metadata: %s | UUID: %s", this.name, metadata.getData(), randomUUID);
      context.put(metadataEntryKey, randomUUID);
      return Mono.just(Report.success(context));
    }
  }

  public static class CustomContext extends FlowContext {
    private static final String STRING_LIST_KEY = "STRING_LIST";
    private static final String INTEGER_FIRST_LIST_KEY = "INTEGER_FIRST_LIST";
    private static final String INTEGER_SECOND_LIST_KEY = "INTEGER_SECOND_LIST";

    public CustomContext() {
      super();
      super.put(STRING_LIST_KEY, List.of("Item 1", "Item 2", "Item 3"));
      super.put(INTEGER_FIRST_LIST_KEY, List.of(1, 2, 3));
      super.put(INTEGER_SECOND_LIST_KEY, List.of(4, 5, 6));
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList() {
      return (List<String>) super.get(STRING_LIST_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getFirstIntegerList() {
      return (List<Integer>) super.get(INTEGER_FIRST_LIST_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getSecondIntegerList() {
      return (List<Integer>) super.get(INTEGER_SECOND_LIST_KEY);
    }
  }
}
```

## Expected result

The result should be something like (note the UUID, that allows to check each error and each data added to the context are really different)

```text
Summary
ERROR - ParallelFlow named Example ended in 568.77 ms (1436901839)

Flow tree
ERROR - ParallelFlow named Example ended in 568.77 ms (1436901839)
    ERROR - SequentialFlow named Sequential (1) ended in 308.80 ms (999522307)
        SUCCESS - StepFlow named Sequential > String ended in 288.16 ms (1866161430)
        SUCCESS - ParallelFlow named Sequential > Parallel ended in 2.40 ms (2024918163)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (1) ended in 0.33 ms (107241811)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (2) ended in 0.43 ms (558922244)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (3) ended in 0.31 ms (339099861)
        ERROR - ParallelFlow named Sequential > Parallel other ended in 8.85 ms (1653986196)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (1) ended in 5.20 ms (1197365356)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.47 ms (1702660825)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.86 ms (1131040331)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 2.80 ms (254749889)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (2) ended in 1.52 ms (973576304)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.29 ms (992802731)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.37 ms (715521683)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.48 ms (1545242146)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (3) ended in 1.48 ms (1524126153)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.37 ms (102065302)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.39 ms (63001505)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.35 ms (191037037)
        SUCCESS - StepFlow named Sequential > Finally ended in 0.50 ms (330084561)
    ERROR - SequentialFlow named Sequential (2) ended in 6.87 ms (1043351526)
        SUCCESS - StepFlow named Sequential > String ended in 0.27 ms (937773018)
        SUCCESS - ParallelFlow named Sequential > Parallel ended in 1.30 ms (728258269)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (1) ended in 0.23 ms (1572098393)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (2) ended in 0.26 ms (1627857534)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (3) ended in 0.22 ms (2084663827)
        ERROR - ParallelFlow named Sequential > Parallel other ended in 4.43 ms (360062456)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (1) ended in 1.27 ms (1790421142)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.22 ms (846947180)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.35 ms (1172131546)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.35 ms (1616974404)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (2) ended in 1.22 ms (927327686)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.31 ms (1582071873)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.23 ms (1908981452)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.44 ms (433287555)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (3) ended in 1.38 ms (27319466)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.31 ms (1003752023)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.32 ms (266272063)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.44 ms (226744878)
        SUCCESS - StepFlow named Sequential > Finally ended in 0.43 ms (172032696)
    ERROR - SequentialFlow named Sequential (3) ended in 5.54 ms (299644693)
        SUCCESS - StepFlow named Sequential > String ended in 0.28 ms (1771243284)
        SUCCESS - ParallelFlow named Sequential > Parallel ended in 1.49 ms (2052256418)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (1) ended in 0.30 ms (2013559698)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (2) ended in 0.32 ms (143695640)
            SUCCESS - StepFlow named Sequential > Parallel > Integer (3) ended in 0.30 ms (2043318969)
        ERROR - ParallelFlow named Sequential > Parallel other ended in 3.19 ms (341878976)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (1) ended in 1.07 ms (1331923253)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.22 ms (1132967838)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.23 ms (1853205005)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.38 ms (2143431083)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (2) ended in 0.86 ms (750468423)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.19 ms (1384010761)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.21 ms (295221641)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.27 ms (2147046752)
            ERROR - SequentialFlow named Sequential > Parallel other > Sequential (3) ended in 0.77 ms (182259421)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Integer other ended in 0.18 ms (715378067)
                ERROR - StepFlow named Sequential > Parallel other > Sequential > Error ended in 0.18 ms (2124643775)
                SUCCESS - StepFlow named Sequential > Parallel other > Sequential > Finally ended in 0.23 ms (1262773598)
        SUCCESS - StepFlow named Sequential > Finally ended in 0.23 ms (688726285)

Context
STRING_LIST - [Item 1, Item 2, Item 3]
INTEGER_FIRST_LIST - [1, 2, 3]
INTEGER_SECOND_LIST - [4, 5, 6]
Name: Sequential > Parallel > Integer | Metadata: 1 | UUID: 24184f4d-504c-49b4-9e58-91d90ff8f787 - 24184f4d-504c-49b4-9e58-91d90ff8f787
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 6 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (db884dcb-99fe-47f5-80d5-d926d6a2929c) | UUID: 8bd28bb2-76bb-4abd-ae96-659daa7ffdff - 8bd28bb2-76bb-4abd-ae96-659daa7ffdff
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 5 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (24b7cee2-35e6-4bef-9929-ec29f3b9045a) | UUID: b0996d6a-0a0c-4ca1-8e34-ea1d0c8adb8e - b0996d6a-0a0c-4ca1-8e34-ea1d0c8adb8e
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 6 | UUID: 991ce6f7-c2ef-4705-b88a-15ab8759d988 - 991ce6f7-c2ef-4705-b88a-15ab8759d988
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 5 | UUID: 3f3c61a2-24aa-491e-a821-c91ad7095d1c - 3f3c61a2-24aa-491e-a821-c91ad7095d1c
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 6 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (68c36a50-6cd6-43be-834c-152fb7e77b15) | UUID: 23995d66-e37a-430a-841d-7d57a69358f6 - 23995d66-e37a-430a-841d-7d57a69358f6
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 6 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (af641ab1-e05d-45b9-95a0-0a2600335c2b) | UUID: c4e228d0-a22c-4b3b-b248-9c24c7af194f - c4e228d0-a22c-4b3b-b248-9c24c7af194f
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 6 | UUID: 210eac8b-29a4-4f24-afd7-1f7567d0417f - 210eac8b-29a4-4f24-afd7-1f7567d0417f
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 5 | UUID: 5235e4b2-867a-45e0-b77a-a2e249daadc8 - 5235e4b2-867a-45e0-b77a-a2e249daadc8
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 5 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (bbcca8f2-e09c-4b2c-9afc-f10e9deb80be) | UUID: 6cd167e5-d752-4b4f-a70e-21efe5fb0e5c - 6cd167e5-d752-4b4f-a70e-21efe5fb0e5c
Name: Sequential > Parallel > Integer | Metadata: 1 | UUID: 68f1335b-08ff-47b4-b61b-0b74826e17ae - 68f1335b-08ff-47b4-b61b-0b74826e17ae
Name: Sequential > Parallel > Integer | Metadata: 3 | UUID: 8d8ca1b2-fd2c-4258-802e-1a5e0e030b09 - 8d8ca1b2-fd2c-4258-802e-1a5e0e030b09
Name: Sequential > Parallel > Integer | Metadata: 2 | UUID: 8bebecd2-21f8-4ea4-95c7-fda9acf5355c - 8bebecd2-21f8-4ea4-95c7-fda9acf5355c
Name: Sequential > Parallel > Integer | Metadata: 2 | UUID: ab875ca7-26d2-4f76-8d96-6713dad66af2 - ab875ca7-26d2-4f76-8d96-6713dad66af2
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 4 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (1f43ddba-21d4-4e42-90bc-9cbfc48efe20) | UUID: 80d73b59-4de7-4b93-95e0-41d772ffcd92 - 80d73b59-4de7-4b93-95e0-41d772ffcd92
Name: Sequential > String | Metadata: Item 3 | UUID: 6719f311-b430-488c-97f5-d072cfab6b52 - 6719f311-b430-488c-97f5-d072cfab6b52
Name: Sequential > Parallel > Integer | Metadata: 1 | UUID: f5730aa5-70e8-4814-bb37-fd28e1105d67 - f5730aa5-70e8-4814-bb37-fd28e1105d67
Name: Sequential > Parallel > Integer | Metadata: 2 | UUID: c673bd63-860a-4b8f-b8cc-b19a089354d0 - c673bd63-860a-4b8f-b8cc-b19a089354d0
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 4 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (d6ae0aed-9d49-4552-86b9-1e5b66c80c8f) | UUID: 4ea26e30-4874-44f8-a6c4-944477243cc8 - 4ea26e30-4874-44f8-a6c4-944477243cc8
Name: Sequential > Finally | Metadata: Item 3 (String) | Errors: Sequential > Parallel other > Sequential > Error (d6ae0aed-9d49-4552-86b9-1e5b66c80c8f), Sequential > Parallel other > Sequential > Error (243577e1-830c-4da4-a611-79f515b6c43f), Sequential > Parallel other > Sequential > Error (68c36a50-6cd6-43be-834c-152fb7e77b15) | UUID: e148224d-af11-420b-8740-70e00f407bc8 - e148224d-af11-420b-8740-70e00f407bc8
Name: Sequential > Finally | Metadata: Item 1 (String) | Errors: Sequential > Parallel other > Sequential > Error (3d619a8a-7295-440a-ad1d-ecae410e05ea), Sequential > Parallel other > Sequential > Error (bbcca8f2-e09c-4b2c-9afc-f10e9deb80be), Sequential > Parallel other > Sequential > Error (af641ab1-e05d-45b9-95a0-0a2600335c2b) | UUID: 7b16264d-0ade-41db-bcb4-c1b9fbf7ae10 - 7b16264d-0ade-41db-bcb4-c1b9fbf7ae10
Name: Sequential > Parallel > Integer | Metadata: 3 | UUID: dd5961b1-999a-47d8-8629-974aa7bdfd4c - dd5961b1-999a-47d8-8629-974aa7bdfd4c
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 6 | UUID: a2819b7f-c3aa-4f7c-aed3-96747c1b85be - a2819b7f-c3aa-4f7c-aed3-96747c1b85be
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 4 | UUID: 5252c540-ab97-4b37-8bf9-3892e5583983 - 5252c540-ab97-4b37-8bf9-3892e5583983
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 5 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (243577e1-830c-4da4-a611-79f515b6c43f) | UUID: 50a8e716-ff39-4f90-9a0d-dc6294246779 - 50a8e716-ff39-4f90-9a0d-dc6294246779
Name: Sequential > Parallel > Integer | Metadata: 3 | UUID: ef5f36ad-c82c-44af-9605-3bba3959bfce - ef5f36ad-c82c-44af-9605-3bba3959bfce
Name: Sequential > Finally | Metadata: Item 2 (String) | Errors: Sequential > Parallel other > Sequential > Error (1f43ddba-21d4-4e42-90bc-9cbfc48efe20), Sequential > Parallel other > Sequential > Error (24b7cee2-35e6-4bef-9929-ec29f3b9045a), Sequential > Parallel other > Sequential > Error (db884dcb-99fe-47f5-80d5-d926d6a2929c) | UUID: 455a3537-f6b4-42ed-803f-1eba5e994b29 - 455a3537-f6b4-42ed-803f-1eba5e994b29
Name: Sequential > String | Metadata: Item 1 | UUID: 33c31b18-b6b1-4c34-9cdd-930b6937902d - 33c31b18-b6b1-4c34-9cdd-930b6937902d
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 4 | UUID: baca33da-d2e4-4f68-a3f3-d46189cfe5c2 - baca33da-d2e4-4f68-a3f3-d46189cfe5c2
Name: Sequential > Parallel other > Sequential > Finally | Metadata: 4 (Integer) | Errors: Sequential > Parallel other > Sequential > Error (3d619a8a-7295-440a-ad1d-ecae410e05ea) | UUID: 01b3c5b0-ac53-4bdd-8997-0ed9296856aa - 01b3c5b0-ac53-4bdd-8997-0ed9296856aa
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 4 | UUID: c9120de9-30cd-4923-b7c8-0962a0ce1695 - c9120de9-30cd-4923-b7c8-0962a0ce1695
Name: Sequential > Parallel other > Sequential > Integer other | Metadata: 5 | UUID: fd7274be-b119-4cf7-8e4a-099f61a80341 - fd7274be-b119-4cf7-8e4a-099f61a80341
Name: Sequential > String | Metadata: Item 2 | UUID: 31a74e2d-1f66-4a25-b208-d7723dcb57a7 - 31a74e2d-1f66-4a25-b208-d7723dcb57a7

Errors and warnings
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (3d619a8a-7295-440a-ad1d-ecae410e05ea) (1131040331)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (bbcca8f2-e09c-4b2c-9afc-f10e9deb80be) (715521683)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (af641ab1-e05d-45b9-95a0-0a2600335c2b) (63001505)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (1f43ddba-21d4-4e42-90bc-9cbfc48efe20) (1172131546)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (24b7cee2-35e6-4bef-9929-ec29f3b9045a) (1908981452)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (db884dcb-99fe-47f5-80d5-d926d6a2929c) (266272063)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (d6ae0aed-9d49-4552-86b9-1e5b66c80c8f) (1853205005)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (243577e1-830c-4da4-a611-79f515b6c43f) (295221641)
ERROR - TECHNICAL exception occurred on StepFlow named Sequential > Parallel other > Sequential > Error with message Sequential > Parallel other > Sequential > Error (68c36a50-6cd6-43be-834c-152fb7e77b15) (2124643775)
```

# Features to implement

- [ ] Default parameters for builders
- [ ] Parametrize the concurrency for ParallelFlow
- [ ] Use a more generic way to recover/retry flow on errors (actually it is mandatory to
  extend [FlowFunctionalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowFunctionalException.java)
  or [FlowTechnicalException](src/main/java/io/github/juliengalet/reactorflow/exception/FlowTechnicalException.java) in order to
  be able to retry on Functional or Technical errors)
