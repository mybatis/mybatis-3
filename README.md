MyBatis SQL Mapper Framework for Java
=====================================

[![build](https://github.com/mybatis/mybatis-3/workflows/Java%20CI/badge.svg)](https://github.com/mybatis/mybatis-3/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/mybatis/mybatis-3/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/mybatis-3?branch=master)
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

Mybatis-core is now being auto formatted.  Given nature of some code logic with mybatis, it is more appropriate to force a formatting structure manually for snippets such as sql statements.  To do so, add following blocks around code.

- ```// @formatter:off``` to start the block of unformatted code
- ```// @formatter:on``` to end the block of unformatted code

If comment sections need same behaviour such as javadocs, note that the entire block must be around entire comment as direct usage does not properly indicate that formatter treats it all as one comment block regardless.

Tests
-----

Mybatis-3 code runs more expressive testing depending on jdk usage and platform.

By default, we set ```<excludedGroups>TestcontainersTests</excludedGroups>``` which will exclude a subset of tests with @Tag('TestcontainersTests').  Further, if pre jdk 16, we will further exclude record classes from executions further reducing tests.

When using jdk 16+, we adjust the rule to ```<excludedGroups>TestcontainersTests,RequireIllegalAccess</excludedGroups>```.

When we run on ci platform, we further make adjustments as needed.  See [here](.github/workflows/ci.yaml) for details.

As of 2/20/2023, using combined system + jdk will result in given number of tests ran.  This will change as tests are added or removed over time.

without adjusting settings (ie use as is, platform does not matter)

- any OS + jdk 11 = 1730 tests
- any OS + jdk 17 = 1710 tests
- any OS + jdk 19 = 1710 tests
- any OS + jdk 20 = 1710 tests
- any OS + jdk 21 = 1710 tests

our adjustments for GH actions where platform does matter

- windows + jdk 11 = 1730 tests
- windows + jdk 17 = 1710 tests
- windows + jdk 19 = 1710 tests
- windows + jdk 20 = 1710 tests
- windows + jdk 21 = 1710 tests

- linux + jdk 11 = 1765 tests
- linux + jdk 17 = 1745 tests
- linux + jdk 19 = 1745 tests
- linux + jdk 20 = 1745 tests
- linux + jdk 21 = 1745 tests

- mac + jdk 11 = 1730 tests
- mac + jdk 17 = 1710 tests
- mac + jdk 19 = 1710 tests
- mac + jdk 20 = 1710 tests
- mac + jdk 21 = 1710 tests
