#!/bin/bash
#
#    Copyright 2009-2016 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

if [ $TRAVIS_JDK_VERSION == "openjdk6" ]; then
  # Java 1.6
  export MAVEN_OPTS="-Dmaven.compiler.testTarget=1.6 -Dmaven.compiler.testSource=1.6 -Dmaven.compiler.testCompilerArgument= -Dmaven.surefire.excludeGroups=org.apache.ibatis.lang.UsesJava8,org.apache.ibatis.lang.UsesJava7"
  echo -e "Exported MAVEN_OPTS: ${MAVEN_OPTS}"
elif [ $TRAVIS_JDK_VERSION == "oraclejdk7" ] || [ $TRAVIS_JDK_VERSION == "openjdk7" ]; then
  # Java 1.7
  export MAVEN_OPTS="-Dmaven.compiler.testTarget=1.7 -Dmaven.compiler.testSource=1.7 -Dmaven.compiler.testCompilerArgument= -Dmaven.surefire.excludeGroups=org.apache.ibatis.lang.UsesJava8"
  echo -e "Exported MAVEN_OPTS: ${MAVEN_OPTS}"
fi
