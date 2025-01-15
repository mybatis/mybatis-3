MyBatis SQL Mapper Framework for Java
=====================================

[![build](https://github.com/mybatis/mybatis-3/actions/workflows/ci.yaml/badge.svg)](https://github.com/mybatis/mybatis-3/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-3/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-3?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mybatis_mybatis-3&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=mybatis_mybatis-3)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis/mybatis.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis/)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-mybatis-brightgreen.svg)](https://stackoverflow.com/questions/tagged/mybatis)
[![Project Stats](https://www.openhub.net/p/mybatis/widgets/project_thin_badge.gif)](https://www.openhub.net/p/mybatis)

![mybatis](https://mybatis.org/images/mybatis-logo.png)

The MyBatis SQL mapper framework makes it easier to use a relational database with object-oriented applications.
MyBatis couples objects with stored procedures or SQL statements using an XML descriptor or annotations.
Simplicity is the biggest advantage of the MyBatis data mapper over object relational mapping tools.

Essentials
----------

* [See the docs](https://mybatis.org/mybatis-3)
* [Download Latest](https://github.com/mybatis/mybatis-3/releases)
* [Download Snapshot](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis/)

Contributions
-------------

See [here](CONTRIBUTING.md)

Tests
-----

Mybatis-3 code runs more expressive testing depending on jdk usage and platform.

By default, we set ```<excludedGroups>TestcontainersTests,RequireIllegalAccess</excludedGroups>``` which will exclude a subset of tests with @Tag('TestcontainersTests') and @Tag('RequireIllegalAccess').

When we run on ci platform, we further make adjustments as needed.  See [here](.github/workflows/ci.yaml) for details.

As of 12/28/2024, using combined system + jdk will result in given number of tests ran.  This will change as tests are added or removed over time.

without adjusting settings (ie use as is, platform does not matter)

- any OS + jdk 17 = 1899 tests
- any OS + jdk 21 = 1899 tests
- any OS + jdk 23 = 1899 tests
- any OS + jdk 24 = 1899 tests
- any OS + jdk 25 = 1899 tests

our adjustments for GH actions where platform does matter

- windows + jdk 17 = 1899 tests
- windows + jdk 21 = 1899 tests
- windows + jdk 23 = 1899 tests
- windows + jdk 24 = 1899 tests
- windows + jdk 25 = 1899 tests

- linux + jdk 17 = 1934 tests
- linux + jdk 21 = 1934 tests
- linux + jdk 23 = 1934 tests
- linux + jdk 24 = 1934 tests
- linux + jdk 25 = 1934 tests

- mac + jdk 17 = 1899 tests
- mac + jdk 21 = 1899 tests
- mac + jdk 23 = 1899 tests
- mac + jdk 24 = 1899 tests
- mac + jdk 25 = 1899 tests
