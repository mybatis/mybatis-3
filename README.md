# MyBatis SQL Mapper Framework for Java

[![build](https://github.com/mybatis/mybatis-3/actions/workflows/ci.yaml/badge.svg)](https://github.com/mybatis/mybatis-3/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-3/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-3?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mybatis_mybatis-3&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mybatis_mybatis-3)
[![Maven Central](https://img.shields.io/maven-central/v/org.mybatis/mybatis.svg)](https://central.sonatype.com/artifact/org.mybatis/mybatis)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis/mybatis.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis/)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-mybatis-brightgreen.svg)](https://stackoverflow.com/questions/tagged/mybatis)
[![Project Stats](https://www.openhub.net/p/mybatis/widgets/project_thin_badge.gif)](https://www.openhub.net/p/mybatis)

![mybatis](https://mybatis.org/images/mybatis-logo.png)

The MyBatis SQL mapper framework makes it easier to use a relational database with object-oriented applications.
MyBatis couples objects with stored procedures or SQL statements using an XML descriptor or annotations.
Simplicity is the biggest advantage of the MyBatis data mapper over object relational mapping tools.

## Essentials

- [See the docs](https://mybatis.org/mybatis-3)
- [Download Latest](https://github.com/mybatis/mybatis-3/releases)
- [Download Snapshot](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis/)

## Contributions

See [here](CONTRIBUTING.md)

## Tests

MyBatis-3 executes an extensive test suite based on the JDK version and operating system.

By default, we set `<excludedGroups>TestcontainersTests,RequireIllegalAccess</excludedGroups>` which will exclude a subset of tests with @Tag('TestcontainersTests') and @Tag('RequireIllegalAccess').

When running on the CI platform, further adjustments are applied as needed. See [here](.github/workflows/ci.yaml) for details.

As of December 28, 2024, running the combined system tests with various JDK versions results in the following test counts (these numbers may change over time):

**Using default settings (platform does not affect the count):**

- Any OS + JDK 17: 1899 tests
- Any OS + JDK 21: 1899 tests
- Any OS + JDK 23: 1899 tests
- Any OS + JDK 24: 1899 tests
- Any OS + JDK 25: 1899 tests

**For GitHub Actions (platform differences are considered):**

- Windows + JDK 17: 1899 tests
- Windows + JDK 21: 1899 tests
- Windows + JDK 23: 1899 tests
- Windows + JDK 24: 1899 tests
- Windows + JDK 25: 1899 tests
- Linux + JDK 17: 1934 tests
- Linux + JDK 21: 1934 tests
- Linux + JDK 23: 1934 tests
- Linux + JDK 24: 1934 tests
- Linux + JDK 25: 1934 tests
- Mac + JDK 17: 1899 tests
- Mac + JDK 21: 1899 tests
- Mac + JDK 23: 1899 tests
- Mac + JDK 24: 1899 tests
- Mac + JDK 25: 1899 tests
