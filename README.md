MyBatis SQL Mapper Framework for Java
=====================================

[![build](https://github.com/mybatis/mybatis-3/actions/workflows/ci.yaml/badge.svg)](https://github.com/mybatis/mybatis-3/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-3/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-3?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mybatis_mybatis-3&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mybatis_mybatis-3)
[![Maven Central](https://img.shields.io/maven-central/v/org.mybatis/mybatis.svg)](https://central.sonatype.com/artifact/org.mybatis/mybatis)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Docs](https://img.shields.io/badge/docs-mybatis.org-blue?logo=github)](https://mybatis.org/mybatis-3/)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-mybatis-brightgreen.svg)](https://stackoverflow.com/questions/tagged/mybatis)
[![Project Stats](https://www.openhub.net/p/mybatis/widgets/project_thin_badge.gif)](https://www.openhub.net/p/mybatis)

![mybatis](https://mybatis.org/images/mybatis-logo.png)

The MyBatis SQL mapper framework makes it easier to use a relational database with object-oriented applications.
MyBatis couples objects with stored procedures or SQL statements using an XML descriptor or annotations.
Simplicity is the biggest advantage of the MyBatis data mapper over object relational mapping tools.

Contributions
-------------

See [here](CONTRIBUTING.md)

Tests
-----

Run the default test suite with `./mvnw test`. By default, the Maven configuration excludes tests tagged `TestcontainersTests` or `RequireIllegalAccess`.

The [Java CI workflow](.github/workflows/ci.yaml) defines the tested operating systems and JDK versions. It activates the `testContainers` profile on Linux, which includes `TestcontainersTests` while continuing to exclude `RequireIllegalAccess`.

To include the container-backed tests locally, run `./mvnw test -PtestContainers` with a working container runtime.
