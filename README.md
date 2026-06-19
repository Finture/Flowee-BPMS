# Flowee BPMS - The open source BPMN platform

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.finture.bpm/flowee-bpms-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.finture.bpm/flowee-bpms-parent) [![flowee bpms manual latest](https://img.shields.io/badge/manual-latest-brown.svg)](https://finture.com/flowee-bpms/docs/manual/latest/) [![License](https://img.shields.io/github/license/Finture/Flowee-BPMS?color=blue&logo=apache)](https://github.com/Finture/Flowee-BPMS/blob/master/LICENSE)

Flowee BPMS 7 is a flexible framework for workflow and process automation. Its core is a native BPMN 2.0 process engine that runs inside the Java Virtual Machine. It can be embedded inside any Java application and any Runtime Container. The supported Spring integration line is Spring Boot 4 / Spring Framework 7 / Jakarta EE 11. On top of the process engine, you can choose from a stack of tools for human workflow management, operations and monitoring.

- Web Site: https://www.finture.com/flowee-bpms/
- Getting Started: https://finture.com/flowee-bpms/docs/get-started/

## Components

Flowee BPMS provides a rich set of components centered around the BPM lifecycle.

#### Process Implementation and Execution

- Flowee BPMS Engine - The core component responsible for executing BPMN 2.0 processes.
- REST API - The REST API provides remote access to running processes.
- Spring, CDI Integration - Programming model integration that allows developers to write Java Applications that interact with running processes.

#### Process Design

- Flowee BPMS Modeler - A [standalone desktop application](https://github.com/camunda/camunda-modeler) that allows business users and developers to design & configure processes.

#### Process Operations

- Flowee BPMS Engine - JMX and advanced Runtime Container Integration for process engine monitoring.
- Flowee BPMS Cockpit - Web application tool for process operations.
- Flowee BPMS Admin - Web application for managing users, groups, and their access permissions.

#### Human Task Management

- Flowee BPMS Tasklist - Web application for managing and completing user tasks in the context of processes.

#### And there's more...

- [bpmn.io](https://bpmn.io/) - Toolkits for BPMN, CMMN, and DMN in JavaScript (rendering, modeling)
- [Community Extensions](https://finture.com/flowee-bpms/docs/manual/0.7/introduction/extensions/) - Extensions on top of Flowee BPMS provided and maintained by our great open source community

## A Framework

In contrast to other vendor BPM platforms, Flowee BPMS strives to be highly integrable and embeddable. We seek to deliver a great experience to developers that want to use BPM technology in their projects.

### Highly Integrable

Out of the box, Flowee BPMS provides infrastructure-level integration with Jakarta EE Application Servers and Servlet Containers.

### Embeddable

Most of the components that make up the platform can even be completely embedded inside an application. For instance, you can add the process engine and the REST API as a library to your application and assemble your custom BPM platform configuration.

## Contributing

Please see our [contribution guidelines](CONTRIBUTING.md) for how to raise issues and how to contribute code to our project.

## Tests

To run the tests in this repository, please see our [testing tips and tricks](TESTING.md).

## Supported Spring line

Flowee BPMS supports Spring Boot 4 / Spring Framework 7. Removed legacy Spring integration is not supported and should not be used for new builds, release validation, or Spring Boot runtime paths.

See the [Spring 6-only migration note](SPRING_6_ONLY_MIGRATION.md) for consumer dependency and QA profile changes.

## Java requirement

**Java 21 is the minimum JDK version.** All modules enforce JDK 21 via the Maven Enforcer plugin. JDK 17 is no longer supported for building or running Flowee BPMS. See the [upgrading guide](UPGRADING.md#java-21-requirement) for migration details.

## Migration

To migrate from Camunda BPM 7 to Flowee BPMS, please see our [migration guide](https://bpms.finture.com/update/723_to_07/).

The whole-project migration to Spring Boot 4 is in progress. See [Flowee-BPMS Project Migration Plan to Spring Boot 4](SPRING_BOOT_4_PROJECT_MIGRATION_PLAN.md) for the plan and current status.

## License

The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).

Flowee BPMS uses and includes third-party dependencies published under various licenses. By downloading and using Flowee BPMS artifacts, you agree to their terms and conditions. Refer to https://finture.com/flowee-bpms/docs/manual/latest/introduction/third-party-libraries/ for an overview of third-party libraries and particularly important third-party licenses we want to make you aware of.
