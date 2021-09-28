<div align="center">
    <b><em>Reactor Flow</em></b><br>
    A workflow engine for Java&trade; with Reactor
</div>
<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)

[![Build Status](https://github.com/juliengalet/reactor-flow/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/juliengalet/reactor-flow/actions)

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
    - [How to use](#how-to-use)
    - [Result report](#result-report)
- [Features to implement](#features-to-implement)
- [Examples](#examples)
    - [StepFlow](#stepflowsrcmainjavafrjtoolsreactorflowflowstepflowjava)
    - [SequentialFlow](#sequentialflowsrcmainjavafrjtoolsreactorflowflowsequentialflowjava)
    - [ConditionalFlow](#conditionalflowsrcmainjavafrjtoolsreactorflowflowconditionalflowjava)
    - [SwitchFlow](#switchflowsrcmainjavafrjtoolsreactorflowflowswitchflowjava)
    - [ParallelFlow](#parallelflowsrcmainjavafrjtoolsreactorflowflowparallelflowjava)
    - [RecoverableFlow](#recoverableflowsrcmainjavafrjtoolsreactorflowflowrecoverableflowjava)
    - [RetryableFlow](#retryableflowsrcmainjavafrjtoolsreactorflowflowretryableflowjava)
    - [NoOpFlow](#noopflowsrcmainjavafrjtoolsreactorflowflownoopflowjava)

# Usage

## How to use

TODO

## Result report

TODO

# Features to implement

- [ ] Default parameters for builders
- [ ] Use a more generic way to recover/retry flow on errors (actually is mandatory to
  extend [FlowFunctionalException](./src/main/java/fr/jtools/reactorflow/exception/FlowFunctionalException.java)
  or [FlowTechnicalException](./src/main/java/fr/jtools/reactorflow/exception/FlowTechnicalException.java) in order to
  be able to retry on Functional or Technical errors)

# Examples

## [StepFlow](./src/main/java/fr/jtools/reactorflow/flow/StepFlow.java)

You can build a [StepFlow](./src/main/java/fr/jtools/reactorflow/flow/StepFlow.java)
using [StepFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/StepFlowBuilder.java) class.

## [SequentialFlow](./src/main/java/fr/jtools/reactorflow/flow/SequentialFlow.java)

You can build a [SequentialFlow](./src/main/java/fr/jtools/reactorflow/flow/SequentialFlow.java)
using [SequentialFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/SequentialFlowBuilder.java) class.

## [ConditionalFlow](./src/main/java/fr/jtools/reactorflow/flow/ConditionalFlow.java)

You can build a [ConditionalFlow](./src/main/java/fr/jtools/reactorflow/flow/ConditionalFlow.java)
using [ConditionalFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/ConditionalFlowBuilder.java) class.

## [SwitchFlow](./src/main/java/fr/jtools/reactorflow/flow/SwitchFlow.java)

You can build a [SwitchFlow](./src/main/java/fr/jtools/reactorflow/flow/SwitchFlow.java)
using [SwitchFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/SwitchFlowBuilder.java) class.

## [ParallelFlow](./src/main/java/fr/jtools/reactorflow/flow/ParallelFlow.java)

You can build a [ParallelFlow](./src/main/java/fr/jtools/reactorflow/flow/ParallelFlow.java)
using [ParallelFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/ParallelFlowBuilder.java) class.

## [RecoverableFlow](./src/main/java/fr/jtools/reactorflow/flow/RecoverableFlow.java)

You can build a [RecoverableFlow](./src/main/java/fr/jtools/reactorflow/flow/RecoverableFlow.java)
using [RecoverableFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/RecoverableFlowBuilder.java) class.

## [RetryableFlow](./src/main/java/fr/jtools/reactorflow/flow/RetryableFlow.java)

You can build a [RetryableFlow](./src/main/java/fr/jtools/reactorflow/flow/RetryableFlow.java)
using [RetryableFlowBuilder](./src/main/java/fr/jtools/reactorflow/builder/RetryableFlowBuilder.java) class.

## [NoOpFlow](./src/main/java/fr/jtools/reactorflow/flow/NoOpFlow.java)

You can build a [NoOpFlow](./src/main/java/fr/jtools/reactorflow/flow/NoOpFlow.java) using `NoOpFlow.named(String name)`
method.
