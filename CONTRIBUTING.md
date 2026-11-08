## If you have a question or need any help...

Please use [the mailing list](https://groups.google.com/group/mybatis-user) instead of creating issues on the tracker. Thank you!

## Reporting a bug

- Create a new issue on [the tracker](https://github.com/mybatis/mybatis-3/issues).
- The best way to report a bug is to create a failing test case. Please see the [Contributing code](CONTRIBUTING.md#contributing-code) section.

## Proposing a new feature

- It is a good idea to discuss your changes on [the mailing list](https://groups.google.com/group/mybatis-user) to get feedback from the community.
- If you have a patch with unit tests, send a pull request. Please see the [Contributing code](CONTRIBUTING.md#contributing-code) section.

## Improving documentation

- Reference guides are under [src/site](https://github.com/mybatis/mybatis-3/tree/master/src/site) in Markdown (`markdown` directories) or [XDoc](https://maven.apache.org/doxia/references/xdoc-format.html) (`xdoc` directories), including translated guides. Submit documentation changes through the same pull request process described in [Contributing code](CONTRIBUTING.md#contributing-code).

## Contributing code

### Formatting

MyBatis core is now formatted automatically. Given the nature of some MyBatis code, particularly SQL statements, it can be useful to preserve formatting in selected snippets. To do so, add the following comments around the code.

- ```// @formatter:off``` to start the block of unformatted code
- ```// @formatter:on``` to end the block of unformatted code

If comment sections need same behaviour such as javadocs, note that the entire block must be around entire comment as direct usage does not properly indicate that formatter treats it all as one comment block regardless.

### Copyright and License

- You are the author of your contributions and will always be.
- Everything you can find in this project is licensed under the Apache Software License 2.0
- Every contribution you do must be licensed under the Apache Software License 2.0. Otherwise we will not be able to accept it.
- Please make sure that all the new files you create hold the following header:

```
/*
 *    Copyright [year] the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
```

### How to send your modifications as a pull request

The best way to submit a patch is to send a pull request.  
Here are the steps of a typical workflow.

1. Fork the repository on GitHub.
2. Clone your fork to your local machine.
3. Create a topic branch with a descriptive name.
4. Make changes with unit tests in the topic branch.
5. Push commits to your fork on GitHub.
6. Send a [pull request](https://help.github.com/articles/using-pull-requests).

For steps 1 to 3, please read [this GitHub help](https://help.github.com/articles/fork-a-repo) if you are not familiar with these operations.  
Steps 4 and 5 are basic [git](https://git-scm.com/) operations. Please see the [online documentation](https://git-scm.com/documentation) for its usage.

For how to write a unit test, please see the [unit test](https://github.com/mybatis/mybatis-3/wiki/Unit-Test) page.
