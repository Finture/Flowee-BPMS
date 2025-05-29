# How to contribute

* [Ways to contribute](#ways-to-contribute)
* [Browse our issues](#browse-our-issues)
* [Build from source](#build-from-source)
* [Create a pull request](#create-a-pull-request)
* [Contribution checklist](#contribution-checklist)
* [Contributor License Agreement (CLA)](#contributor-license-agreement-cla)
* [Commit message conventions](#commit-message-conventions)
* [Review process](#review-process)

# Ways to contribute

We would love you to contribute to this project. You can do so in various ways.

## File bugs or feature requests

Found a bug in the code or have a feature that you would like to see in the future? [Search our open issues](https://github.com/Finture/Flowee-BPMS/issues) if we have it on the radar already or [create a new issue otherwise](https://github.com/Finture/Flowee-BPMS/issues/new/choose).

Try to apply our best practices for creating issues:

* Only Raise an issue if your request requires a code change in Flowee BPMS 7
  * If you want to contact the Flowee BPMS customer support, please see our [Contact Form](https://finture.com/en/flowee-bpms/#formularz).
  * If you have an understanding question or need help building your solution, check out our [user forum](https://forum.camunda.io/).
* Create a high-quality issue:
  * Give enough context so that a person who doesn't know your project can understand your request
  * Be concise, only add what's needed to understand the core of the request
  * If you raise a bug report, describe the steps to reproduce the problem
  * Specify your environment (e.g. Flowee BPMS version, Flowee BPMS modules you use, ...)
  * Provide code. For a bug report, create a test that reproduces the problem. For feature requests, create mockup code that shows how the feature might look like. Fork our [unit test Github template](https://github.com/camunda/camunda-engine-unittest) to get started quickly.


## Write code

You can contribute code that fixes bugs and/or implements features. Here is how it works:

1. Select a ticket that you would like to implement. Have a look at [our backlog](https://github.com/Finture/Flowee-BPMS/issues) if you need inspiration. Be aware that some of the issues need good knowledge of the surrounding code.
2. Check your code changes against our [contribution checklist](#contribution-checklist)
3. [Create a pull request](https://github.com/Finture/Flowee-BPMS/pulls). Note that you can already do this before you have finished your implementation if you would like feedback on your work in progress.


# Browse our issues

In this repository, we manage the [issues](https://github.com/Finture/Flowee-BPMS/issues) for the following Flowee BPMS code repositories and projects:

* https://github.com/Finture/Flowee-BPMS

We use [labels](https://github.com/Finture/Flowee-BPMS/labels) to mark and group our issues for easier browsing. We define the following label prefixes:

* `bot:` labels that control a github app, workflow, ...
* `ci:` labels that control the CI for a pull request
* `group:` Arbitrary labels that we can define to group tickets. If you create this, please add a DRI to the description to make sure someone has ownership, e.g. to decide if we still need the label
* `potential:` Issues that we are potentially releasing with the given version. This is not a guarantee and does not express high confidence.
* `hacktoberfest-` labels for hacktoberfest contributions. This prefix cannot be changed. It is a rule of Hacktoberfest to name it like that.
* `scope:` The technical scope in which the ticket makes changes.
* `type:` Issue type. Every issue should have exactly one of these labels. They are automatically added when you create a new issue from a template.
* `version:` Issues that will be released (with high confidence) with the given version.


# Build from source

In order to build our codebase from source, add the following to your Maven `settings.xml`.

```xml
<profiles>
  <profile>
    <id>flowee-bpms</id>
    <repositories>
      <repository>
        <id>flowee-bpms-nexus</id>
        <name>flowee-bpms-nexus</name>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
        <url>https://artifacts.finture.com/artifactory/public/</url>
      </repository>
    </repositories>
  </profile>
</profiles>
<activeProfiles>
  <activeProfile>flowee-bpms</activeProfile>
</activeProfiles>
```

An entire repository can then be built by running `mvn clean install` in the root directory.
This will build all sub modules and execute unit tests.
Furthermore, you can restrict the build to just the module you are changing by running the same command in the corresponding directory.
Check the repository's or module's README for additional module-specific instructions.
The `webapps` module requires NodeJS.
You can exclude building them by running `mvn clean install -pl '!webapps,!webapps/assembly,!webapps/assembly-jakarta'`.

Integration tests (e.g. tests that run in an actual application server) are usually not part of the default Maven profiles. If you think they are relevant to your contribution, please ask us in the ticket, on the forum or in your pull request for how to run them. Smaller contributions usually do not need this.

# Create a pull request

In order to show us your code, you can create a pull request on Github. Do this when your contribution is ready for review, or if you have started with your implementation and want some feedback before you continue. It is always easier to help if we can see your work in progress.

A pull request can be submitted as follows: 

1. [Fork the Flowee BPMS repository](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo) you are contributing to
1. Commit and push your changes to a branch in your fork
1. [Submit a Pull Request to the Flowee BPMS repository](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request-from-a-fork). As the *base* branch (the one that you contribute to), select `master`. This should also be the default in the Github UI.
1. In the pull request description, reference the github issue that your pull request addresses.

# Contribution checklist

Before submitting your pull request for code review, please go through the following checklist:

1. Is your code formatted according to our code style guidelines?
    * Java: Please check our [Java Code Style Guidelines](https://github.com/Finture/Flowee-BPMS/wiki/Coding-Style-Java). You can also import [our template and settings files](https://github.com/Finture/Flowee-BPMS/tree/master/settings) into your IDE before you start coding.
    * Javascript: Your code is automatically formatted whenever you commit.
1. Is your code covered by unit tests?
    * Ask us if you are not sure where to write the tests or what kind of tests you should write.
    * Java: Please follow our [testing best practices](https://github.com/Finture/Flowee-BPMS/wiki/Testing-Best-Practices-Java).
    * Have a look at other tests in the same module for how it works.
    * In rare cases, it is not feasible to write an automated test. Please ask us if you think that is the case for your contribution.
1. Do your commits follow our [commit message conventions](#commit-message-conventions)?

# Contributor License Agreement (CLA)

By contributing to this project, you agree to Contributor License Agreement (CLA). This is a legal document that defines the terms under which you contribute to the project. It ensures that Finture Sp. z o.o. can use your contributions in accordance with the project's license.

Version: 2025-07-08

Individual/Employee Contributor License Agreement
This Individual/Employee Contributor License Agreement (hereinafter referred to as the “Agreement”) is between the individual, submitting Agreement, further referred to as “Contributor” and Finture Sp. z o.o., Skierniewicka 10A, 01-230 Warszawa, further referred to as "Finture Sp. z o.o." (together referred to as the “Parties”). The purpose of this Agreement is to set forth the terms and conditions under which Finture Sp. z o.o. may use software that the Contributor wishes to contribute to Flowee BPMS for use within one or more of its software development projects.

By contributing to Flowee BPMS, the Contributor agrees to enter into a binding Agreement based on the below stipulated terms and conditions. Before contribution, the Contributor will read the terms and conditions of this Agreement and any terms of use provided herewith. The Contributor warrants that they are authorized to complete this process as set forth above.

The Contributor agrees to the following terms and conditions:
1. Definitions:
- In this Agreement, the Contributor shall mean the owner of the Contribution.
- The term "Contribution" shall mean any original work of authorship and any modifications or addition thereof, including software source code, software object code, or the respective documentation, which the Contributor makes available or submits to Finture Sp. z o.o. or any of its Affiliates for inclusion into software development projects managed or owned by Finture Sp. z o.o.. Contributions shall not include any software or documentation which have been explicitly marked to indicate that it is not a Contribution to Flowee BPMS.
- “Submit” shall mean any form of communication sent to Finture Sp. z o.o., including but not limited to communication on mailing lists, source code control, or issue tracking systems.
- “Submission Date” shall mean the date on which the Contributor first time submits their Contributions to Flowee BPMS. 
2. Grant of Copyright License: The Contributor hereby grants to Finture Sp. z o.o. and recipients of software distributed by Finture Sp. z o.o., the worldwide, non-exclusive, transferable, irrevocable, no-charge, perpetual, royalty-free right to use, to reproduce, prepare derivative works of, publicly display, publicly perform, sublicense, and distribute the Contributions and any derivative works thereof. Finture Sp. z o.o. shall be put in the position of an owner of the Contribution as far as legally possible. The Contributor also grants Finture Sp. z o.o. the right to make changes and amendments to the Contribution(s) submitted. The Contributor agrees that Finture Sp. z o.o. will not explicitly name the Contributor in connection with their Contribution and that they will not assert any moral rights in their contribution against Flowee BPMS or Flowee BPMS´s sublicensees.

3. Grant of Patent License: The Contributor hereby grants to Finture Sp. z o.o. and recipients of software distributed by Finture Sp. z o.o., the worldwide, non-exclusive, transferable, irrevocable, no-charge, perpetual, royalty-free patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the work, where such license applies only to those patent claims licensable by the Contributor that is necessarily infringed by his Contribution(s) alone or by the combination of their Contribution(s) with the work to which such Contribution(s) was submitted.

4. Without limitation, the grants in Clauses 2 and 3 are made concerning any copyright, patent, or other intellectual property or moral rights Contributor may have in or to the Contributions.

5. The Contributor agrees that Finture Sp. z o.o. may exercise all ownership rights associated with the Contribution, including but not limited to the right to conduct litigation.

6. Representations and Warranties:

- The Contributor agrees that they are legally entitled to enter into this Agreement and to grant Finture Sp. z o.o. the rights and licenses described in this Agreement.
- If the Contribution was done as an Employee, the Contributor warrants, that they are legally entitled to grant the above licenses. If the Contributor´s employer has rights to intellectual property that the Contributor created and included in his Contribution, the Contributor warrants that they have received permission to make Contributions on behalf of that employer.
- Subject to the exception in Section 6 (ii) for submissions in which the Contributor´s employer has rights, the Contributor represents that they are the original author, creator, or inventor of each of the Contributions, or that they otherwise own all rights granted to Finture Sp. z o.o. under this Agreement as a work made for hire, by assignment, or otherwise.
- The Contributor represents and warrants that to the best of their knowledge, each Contribution will not violate any third party's copyrights, trademarks, patents, or other intellectual property rights and that they have not been deleted or in any way removed any copyright notices or license file of any third-party license.
- The Contributor represents, that their Contributions is not making use of any work that is published under a copyleft license. A non-exhaustive overview of such licenses can be found on the Free Software Foundation’s website.
7. The Contributor agrees to notify Finture Sp. z o.o. if any circumstance should arise which would make any of the foregoing representations inaccurate in any respect.

8. The rights granted to Finture Sp. z o.o. under this Agreement are effective on the Submission Date, even if the submission took place before the date this Agreement was submitted.

# Commit message conventions

The messages of all commits must conform to the style:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Example:

```
feat(engine): Support BPEL

- implements execution for a really old standard
- BPEL models are mapped to internal ActivityBehavior classes

related to #123
```

Have a look at the [commit history](https://github.com/Finture/Flowee-BPMS/commits/master) for real-life examples.


## \<type\>

One of the following:

* feat (feature)
* fix (bug fix)
* docs (documentation)
* style (formatting, missing semi colons, …)
* refactor
* test (when adding missing tests)
* chore (maintain)
 
## \<scope\>

The scope is the module that is changed by the commit. E.g. `engine` in the case of https://github.com/Finture/Flowee-BPMS/tree/master/engine.

## \<subject\>

A brief summary of the change. Use imperative form (e.g. *implement* instead of *implemented*).  The entire subject line shall not exceed 70 characters.

## \<body\>

A list of bullet points giving a high-level overview of the contribution, e.g. which strategy was used for implementing the feature. Use present tense here (e.g. *implements* instead of *implemented*). A line in the body shall not exceed 80 characters. For small changes, the body can be omitted. 

## \<footer\>

# Review process

We usually check for new community-submitted pull requests once a week. We will then assign a reviewer from our development team and that person will provide feedback as soon as possible. 

Note that due to other responsibilities (our own implementation tasks, releases), feedback can sometimes be a bit delayed. Especially for larger contributions, it can take a bit until we have the time to assess your code properly.

During review we will provide you with feedback and help to get your contribution merge-ready. However, before requesting a review, please go through our [contribution checklist](#contribution-checklist).

Once your code is merged, it will be shipped in the next alpha and minor releases. We usually build alpha releases once a month and minor releases once every six months. If you are curious about the exact next minor release date, check our [release announcements](https://finture.com/flowee-bpms/docs/enterprise/announcement/) page.
