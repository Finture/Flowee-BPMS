# Testing Guidelines

* [Best Practices for Writing Test Cases](#best-practices-for-writing-test-cases)
* [Mockito / ByteBuddy Configuration (Java 25 Compatibility)](#mockito--bytebuddy-configuration-java-25-compatibility)
* [Groovy 5.0.6 (Script Task Tests)](#groovy-506-script-task-tests)
* [AssertJ 3.x Assertion Patterns](#assertj-3x-assertion-patterns)
* [MockedStatic Best Practices (ServiceLoader and Infrastructure Classes)](#mockedstatic-best-practices-serviceloader-and-infrastructure-classes)
* [Servlet Mock Pattern (javax vs jakarta)](#servlet-mock-pattern-javax-vs-jakarta)
* [HTTP Endpoint Testing in Spring Boot 4.0](#http-endpoint-testing-in-spring-boot-40)
* [Current Engine Test Suite Status](#current-engine-test-suite-status)
* [Running Integration Tests](#running-integration-tests)
* [Limiting the Number of Engine Unit Tests](#limiting-the-number-of-engine-unit-tests)

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **JDK** | **21+** (LTS) | Enforced by `maven-enforcer-plugin`. JDK 17 is no longer supported. |
| Maven | 3.9+ | Required for Java 21 class file support |
| Maven Wrapper | 3.9.7 | Updated from 3.8.8. `./mvnw -version` should report JDK 21 and Maven 3.9.7 |

Verify your environment before running tests:

```shell
./mvnw -version   # Confirm Maven 3.9.7 and JDK 21
java -version     # Confirm JDK 21
```

## Mockito / ByteBuddy Configuration (Java 25 Compatibility)

The test suite uses **Mockito 5.23.0** with **ByteBuddy 1.18.9** for mock object generation. ByteBuddy requires special configuration to support Java 25 bytecode:

### Required JVM Flag

The `-Dnet.bytebuddy.experimental=true` system property **must** be present in all test JVM arguments. This is already configured in the parent POM's Surefire and Failsafe plugin configurations:

```xml
<argLine>-Xmx968m -Dnet.bytebuddy.experimental=true</argLine>
```

Without this flag, ByteBuddy will fail with errors like `Unsupported class file major version 69` when running on Java 25.

### Custom argLine Overrides — Use `@{argLine}` Syntax

If your module overrides `<argLine>` in its Surefire or Failsafe configuration, you **must** use the `@{argLine}` late-evaluation syntax to preserve the parent's JVM flags:

```xml
<!-- Correct: preserves parent argLine (including -Dnet.bytebuddy.experimental=true) -->
<argLine>@{argLine} -Xmx2g</argLine>

<!-- Incorrect: replaces parent argLine entirely, losing ByteBuddy flag -->
<argLine>-Xmx2g</argLine>
```

The `@{argLine}` syntax (as opposed to `${argLine}`) uses Maven's late property evaluation, resolving the property after JaCoCo's `prepare-agent` goal has run. An empty `<argLine></argLine>` default is defined in `parent/pom.xml` to prevent unresolved property errors in modules without JaCoCo.

### Current Test Dependency Versions

| Dependency | Version | Managed In |
|------------|---------|------------|
| Mockito | 5.23.0 | `parent/pom.xml` (`version.mockito`) |
| AssertJ | 3.27.7 | `parent/pom.xml` (`version.assertj`) |
| ByteBuddy | 1.18.9 | `internal-dependencies/pom.xml` (`version.byte-buddy`) |
| Groovy | 5.0.6 | `parent/pom.xml` (`version.groovy`) |

Do not override these versions in individual modules. If your module has a hardcoded Mockito, AssertJ, or Groovy version, replace it with the parent-managed property (e.g., `${version.mockito}`).

## Groovy 5.0.6 (Script Task Tests)

The engine test suite uses **Groovy 5.0.6** (upgraded from 4.0.22) for Groovy script task tests. Groovy 5.x is required for Java 25 / ByteBuddy 1.18.9 compatibility.

### Python Script Tests — Disabled

22 Python script test methods are disabled with `@Ignore` because a compatible Python script engine (e.g., Jython) is not available for the Java 25 runtime:

| Test Class | Methods Disabled | File |
|------------|-----------------|------|
| `ScriptTaskTest` | 8 | `engine/src/test/java/.../ScriptTaskTest.java` |
| `ExternalScriptTaskTest` | 14 | `engine/src/test/java/.../ExternalScriptTaskTest.java` |

These tests are skipped (not failed) and do not block the build. They can be re-enabled when a Java 25-compatible Python script engine is available.

## AssertJ 3.x Assertion Patterns

When writing or updating engine tests with AssertJ 3.27.7, follow these patterns:

### Do not use `.asList()` for collection assertions

AssertJ 3.x provides direct collection assertion methods. Avoid the deprecated `.asList()` intermediate conversion:

```java
// Correct — direct assertion on the collection:
assertThat(resultList).containsExactly("a", "b", "c");

// Incorrect — unnecessary .asList() conversion:
assertThat(resultList).asList().containsExactly("a", "b", "c");
```

### Use `.containsExactly()` for ordered collection comparison

When verifying that a collection contains exactly the expected elements in order, use `.containsExactly()` rather than `.isEqualTo()`:

```java
// Correct — verifies elements and order:
assertThat(actualList).containsExactly("a", "b", "c");

// Incorrect — reference/equality comparison may fail on distinct instances:
assertThat(actualList).isEqualTo(expectedList);
```

## MockedStatic Best Practices (ServiceLoader and Infrastructure Classes)

When using Mockito's `MockedStatic` to mock infrastructure classes like `ServiceLoader`, follow these practices to avoid interfering with library internals.

### Problem: Broad Static Mocks Interfere with Library Internals

`MockedStatic` intercepts **all** invocations of the mocked class within the JVM. If the mocked class is used internally by libraries (e.g., SLF4J, AssertJ, Jackson), the mock can break those libraries:

```java
// PROBLEMATIC: Mocks ALL ServiceLoader.load() calls
MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class);
mock.when(() -> ServiceLoader.load(eq(MyService.class), any()))
  .thenReturn(mockLoader);
// SLF4J's internal ServiceLoader.load() calls now return null → NPE
```

### Solution 1: Narrow the Mock Scope with a Custom Answer

Use a custom `Answer` that returns mocks only for specific service types and calls the real method for everything else:

```java
MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class, invocation -> {
  Object[] args = invocation.getArguments();
  if (args.length >= 1) {
    Class<?> serviceType = (Class<?>) args[0];
    if (serviceType == MyService.class) {
      return mockLoader;
    }
  }
  // Call real method for all other ServiceLoader.load() calls
  return invocation.callRealMethod();
});
```

**Caveat**: `ServiceLoader.load()` is annotated with `@CallerSensitive`. When called from a mock, the caller context changes, and `callRealMethod()` may fail with `IllegalCallerException`. In such cases, use Solution 2.

### Solution 2: Pre-Initialize Libraries Before Mocking

Force initialization of libraries that use the mocked class internally **before** setting up the mock. Once initialized, these libraries cache their results and won't call the mocked method again:

```java
@Before
public void setUp() {
  // Pre-initialize SLF4J (triggers ServiceLoader.load() for logback binding)
  org.slf4j.LoggerFactory.getILoggerFactory();

  // Pre-initialize AssertJ (triggers ServiceLoader.load() for configuration)
  try {
    Class.forName("org.assertj.core.configuration.ConfigurationProvider");
  } catch (ClassNotFoundException e) {
    // ignore
  }

  // Now safe to mock ServiceLoader
  mock = Mockito.mockStatic(ServiceLoader.class, ...);
}
```

### Solution 3: Prefer Instance Mocking or Dependency Injection

When possible, avoid static mocking altogether. Prefer:
- **Instance mocking**: Mock specific `ServiceLoader` instances rather than the static `load()` method
- **Dependency injection**: Refactor code to accept `ServiceLoader` instances as parameters, making tests easier to control
- **Wrapper classes**: Create a thin wrapper around `ServiceLoader.load()` that can be mocked at the instance level

### Summary

| Approach | When to Use | Trade-offs |
|----------|-------------|------------|
| Custom Answer with `callRealMethod()` | Mocked method is not `@CallerSensitive` | Clean, but fails for `@CallerSensitive` methods |
| Pre-initialization | Mocked method is `@CallerSensitive` | Quick fix, but relies on library caching behavior |
| Instance mocking / DI | New code or refactoring opportunities | Best long-term solution, requires code changes |

## Servlet Mock Pattern (javax vs jakarta)

### Problem

Spring Framework 7.0 changed its mock servlet classes (`MockFilterConfig`, `MockHttpServletRequest`, `MockFilterChain`, `MockHttpServletResponse`, `MockServletContext`, `MockHttpSession`) from `javax.servlet.*` to `jakarta.servlet.*`. Production code still uses `javax.servlet.*` interfaces. Spring Mock objects **cannot** be used in tests — the types are incompatible.

### Solution: Mockito Mocks of `javax.servlet.*` Interfaces

Replace Spring Mock objects with Mockito mocks and track state manually.

#### Response Mock with State Tracking

```java
abstract static class TestHttpServletResponse implements HttpServletResponse {
  abstract String getErrorMessage();
}

protected TestHttpServletResponse createMockResponse() {
  Map<String, String> headers = new HashMap<>();
  AtomicInteger status = new AtomicInteger(200);
  AtomicReference<String> error = new AtomicReference<>();

  TestHttpServletResponse response = mock(TestHttpServletResponse.class);
  doAnswer(invocation -> {
    headers.put(invocation.getArgument(0), invocation.getArgument(1));
    return null;
  }).when(response).setHeader(anyString(), anyString());
  doAnswer(invocation -> {
    headers.put(invocation.getArgument(0), invocation.getArgument(1));
    return null;
  }).when(response).addHeader(anyString(), anyString());
  doAnswer(invocation -> {
    status.set(invocation.getArgument(0));
    return null;
  }).when(response).setStatus(anyInt());
  stubSendError(response, status, error);
  when(response.getStatus()).thenAnswer(invocation -> status.get());
  when(response.getHeader(anyString())).thenAnswer(invocation -> headers.get(invocation.getArgument(0)));
  when(response.getErrorMessage()).thenAnswer(invocation -> error.get());
  return response;
}

private void stubSendError(TestHttpServletResponse response, AtomicInteger status, AtomicReference<String> error) {
  try {
    doAnswer(invocation -> {
      status.set(invocation.getArgument(0));
      error.set(invocation.getArgument(1));
      return null;
    }).when(response).sendError(anyInt(), anyString());
  } catch (IOException ignored) {
    // stubbing only — exception is not real
  }
}
```

**Why `TestHttpServletResponse`?** The `javax.servlet.http.HttpServletResponse` interface has no `getErrorMessage()` method. Spring's `MockHttpServletResponse` had one. The abstract class adds it for test assertions.

#### Session Mock with Attribute Tracking

```java
protected HttpSession createMockSession(String id) {
  HttpSession session = mock(HttpSession.class);
  Map<String, Object> attrs = new HashMap<>();
  when(session.getId()).thenReturn(id);
  when(session.getAttribute(any())).thenAnswer(invocation -> attrs.get(invocation.getArgument(0)));
  doAnswer(invocation -> {
    attrs.put(invocation.getArgument(0), invocation.getArgument(1));
    return null;
  }).when(session).setAttribute(anyString(), any());
  return session;
}
```

#### Filter Config Mock

```java
FilterConfig config = mock(FilterConfig.class);
when(config.getServletContext()).thenReturn(mock(ServletContext.class));
```

#### Request Mock

```java
HttpServletRequest request = mock(HttpServletRequest.class);
when(request.getServletPath()).thenReturn("");
when(request.getPathInfo()).thenReturn(null);
when(request.getRemoteAddr()).thenReturn("127.0.0.1");
when(request.getServletContext()).thenReturn(mock(ServletContext.class));
```

### Migration Checklist

- [ ] Remove imports of `org.springframework.mock.web.Mock*`
- [ ] Add imports of `javax.servlet.*` (not `jakarta.servlet.*`)
- [ ] Replace `new MockHttpServletRequest()` with `mock(HttpServletRequest.class)`
- [ ] Replace `new MockHttpServletResponse()` with `createMockResponse()` using the pattern above
- [ ] Replace `new MockFilterConfig()` with `mock(FilterConfig.class)`
- [ ] Replace `new MockFilterChain()` with `mock(FilterChain.class)`
- [ ] Replace `new MockHttpSession()` with `createMockSession()` using the pattern above
- [ ] Add `TestHttpServletResponse` abstract class if test assertions need `getErrorMessage()`
- [ ] Add state-tracking stubs (headers, status, error, session attributes) as shown above

### Reference

See `webapps/assembly/src/test/java/com/finture/bpm/webapp/impl/security/filter/csrf/CsrfPreventionFilterTest.java` for a complete working example.

## HTTP Endpoint Testing in Spring Boot 4.0

### `TestRestTemplate` Removed

`TestRestTemplate` was **removed in Spring Boot 4.0**. It is no longer available for injection or use in integration tests. All tests that previously used `TestRestTemplate` must be migrated to the `RestTemplate`-based pattern described below.

### Replacement Pattern: `@LocalServerPort` + `RestTemplate` + `catchThrowable()`

The standard pattern for testing HTTP endpoints in Spring Boot 4.0 uses three components:

1. **`@LocalServerPort`** — injects the random port assigned by `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
2. **`RestTemplate`** — standard Spring HTTP client (instantiated directly, not autowired).
3. **`catchThrowable()`** — AssertJ utility to capture exceptions thrown by `RestTemplate` on error responses (4xx/5xx).

#### Testing Success Responses (2xx)

`RestTemplate` returns a `ResponseEntity` for successful responses, just like `TestRestTemplate` did:

```java
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = MyApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MyEndpointTest {

  @LocalServerPort
  private int port;

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  public void testEndpointReturnsOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + "/api/resource", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("expected content");
  }
}
```

#### Testing Error Responses (4xx/5xx)

Unlike `TestRestTemplate`, `RestTemplate` **throws exceptions** on error responses. Use `catchThrowable()` to capture and assert on the exception:

```java
@Test
public void testEndpointReturns404() {
  Throwable thrown = catchThrowable(() ->
      restTemplate.getForEntity("http://localhost:" + port + "/api/missing", String.class));

  assertThat(thrown).isInstanceOf(HttpClientErrorException.NotFound.class);
}

@Test
public void testEndpointReturns403() {
  Throwable thrown = catchThrowable(() ->
      restTemplate.getForEntity("http://localhost:" + port + "/api/forbidden", String.class));

  assertThat(thrown).isInstanceOf(HttpClientErrorException.Forbidden.class);
}

@Test
public void testEndpointReturns500() {
  Throwable thrown = catchThrowable(() ->
      restTemplate.getForEntity("http://localhost:" + port + "/api/error", String.class));

  assertThat(thrown).isInstanceOf(HttpServerErrorException.InternalServerError.class);
}
```

#### Exception Type Reference

| HTTP Status | Exception Type |
|-------------|---------------|
| 400 Bad Request | `HttpClientErrorException.BadRequest` |
| 401 Unauthorized | `HttpClientErrorException.Unauthorized` |
| 403 Forbidden | `HttpClientErrorException.Forbidden` |
| 404 Not Found | `HttpClientErrorException.NotFound` |
| 409 Conflict | `HttpClientErrorException.Conflict` |
| 500 Internal Server Error | `HttpServerErrorException.InternalServerError` |

All exception types are in `org.springframework.web.client`.

### Test Configuration for Webapp Tests

When testing webapp endpoints with `@SpringBootTest(webEnvironment = RANDOM_PORT)`, ensure the test `application.properties` includes:

1. **H2 in-memory database** — required for engine startup:
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
   spring.datasource.driver-class-name=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   ```

2. **Flowee BPMS engine properties** — required for engine initialization:
   ```properties
   flowee-bpms.bpm.database.schema-update=true
   flowee-bpms.bpm.database.type=h2
   flowee-bpms.bpm.process-engine-name=testEngine
   flowee-bpms.bpm.history-level=auto
   flowee-bpms.bpm.generic-properties.properties.enforce-history-time-to-live=false
   ```

3. **`spring-boot-starter-jdbc` test dependency** — required in the module's `pom.xml` for H2 datasource auto-configuration:
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-jdbc</artifactId>
     <scope>test</scope>
   </dependency>
   ```

4. **Exclude security auto-configuration** (if not testing security):
   ```properties
   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
   ```

### Migration Checklist

When migrating tests from `TestRestTemplate` to `RestTemplate`:

- [ ] Remove `@Autowired TestRestTemplate` field
- [ ] Add `@LocalServerPort private int port` field
- [ ] Add `private final RestTemplate restTemplate = new RestTemplate()` field
- [ ] Update URL construction to use `"http://localhost:" + port + "/path"`
- [ ] For error response tests: wrap call in `catchThrowable()` and assert on exception type
- [ ] For success response tests: no change needed (same `ResponseEntity` API)
- [ ] Add required imports: `@LocalServerPort`, `RestTemplate`, `HttpClientErrorException`, `catchThrowable`

## Current Engine Test Suite Status

After Wave 10 fixes, the full engine test suite status is:

| Metric | Count |
|--------|-------|
| Total tests | 15,867 |
| Passed | 15,754 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 113 |

The 113 skipped tests include 22 Python script tests (disabled via `@Ignore`), plus integration tests and optional feature tests that are excluded by default.

# Best Practices for Writing Test Cases

* write JUnit4-style tests, not JUnit3
* Project `flowee-bpms-engine`: If you need a process engine object, use the JUnit rule `com.finture.bpm.engine.test.util.ProvidedProcessEngineRule`. It ensures that the process engine object is reused across test cases and that certain integrity checks are performed after every test. For example:
  ```
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void testThings() {
    ProcessEngine engine = engineRule.getProcessEngine();

    ...
  }
  ```
* Project `flowee-bpms-engine`: As an alternative to the above, you can extend extend the `com.finture.bpm.engine.test.util.PluggableProcessEngineTest` class.
  The class already provides an instance of the `ProvidedProcessEngineRule`, as well as the `ProcessEngineTestRule` that
  provides some additional custom assertions and helper methods.
  * However, if you need to make modifications to the `ProcessEngineConfiguration`, then please use the `ProcessEngineBootstrapRule`
    as described below. 
* Project `flowee-bpms-engine`: If you need a process engine with custom configuration, use the JUnit rule `com.finture.bpm.engine.test.util.ProcessEngineBootstrapRule` and chain it with `com.finture.bpm.engine.test.util.ProvidedProcessEngineRule` like so:
  ```
  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      // apply configuration options here
  });
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);
  ```

## Spring compatibility validation

Release validation covers the Spring Boot 4 / Spring Framework 7 integration line only. Removed legacy Spring modules and profiles are no longer part of the supported test matrix.

## Spring Boot backend build/runtime validation

For the supported Spring Boot/Jakarta backend line, validate the starter and runtime modules directly from the repository root:

```shell
./mvnw clean install -f spring-boot-starter/pom.xml -DskipTests
./mvnw test -f spring-boot-starter/pom.xml
./mvnw verify -f spring-boot-starter/pom.xml -Pintegration-test-spring-boot-starter
./mvnw clean install -f distro/run/pom.xml -DskipTests
./mvnw clean verify -f distro/run/qa/pom.xml -Pintegration-test-camunda-run
```

Treat these commands as build/runtime gates only. For Spring Boot 4 migration validation, attach additional smoke evidence for `/camunda/app/**`, `/camunda/api/**`, login/logout, CSRF/session behavior, Liquibase startup, H2/PostgreSQL and the external task client starter.

For Spring Boot 4 migration work, use `spring-boot-starter/SPRING_BOOT_4_MIGRATION_PLAN.md` as the authoritative module order. Start with dependency/BOM and low-dependency module gates before running the full starter reactor:

```shell
./mvnw -version
./mvnw validate -f spring-boot-starter/pom.xml -DskipTests
./mvnw test -f spring-boot-starter/starter-client/spring/pom.xml
./mvnw test -f spring-boot-starter/starter-test/pom.xml
./mvnw -f engine-spring/pom.xml -pl core-6 test -DskipITs
./mvnw clean install -f spring-boot-starter/starter/pom.xml -DskipTests
./mvnw test -f spring-boot-starter/starter-client/spring-boot/pom.xml
```

After Boot 4 dependency alignment, capture dependency leakage evidence with:

```shell
./mvnw dependency:tree -f spring-boot-starter/pom.xml -DskipTests -Dverbose
```

Inspect the output for unsupported legacy Spring artifacts, `javax.servlet`, Tomcat 9 and dependency overrides that conflict with the selected Boot 4 BOM.

Then expand through the dependency chain and capture pass/fail evidence for each gate:

```shell
./mvnw clean install -f spring-boot-starter/starter-rest/pom.xml -DskipTests
./mvnw clean install -f spring-boot-starter/starter-webapp-core/pom.xml -DskipTests
./mvnw clean install -f spring-boot-starter/starter-webapp/pom.xml -DskipTests
./mvnw clean install -f spring-boot-starter/starter-security/pom.xml -DskipTests
./mvnw verify -f spring-boot-starter/starter-qa/integration-test-simple/pom.xml -Pintegration-test-spring-boot-starter
./mvnw verify -f spring-boot-starter/starter-qa/integration-test-request-scope/pom.xml -Pintegration-test-spring-boot-starter
./mvnw verify -f spring-boot-starter/starter-qa/integration-test-plugins/pom.xml -Pintegration-test-spring-boot-starter
./mvnw verify -f spring-boot-starter/starter-qa/integration-test-liquibase/pom.xml -Pintegration-test-spring-boot-starter
./mvnw verify -f spring-boot-starter/starter-qa/integration-test-webapp/pom.xml -Pintegration-test-spring-boot-starter
./mvnw clean install -f distro/run/pom.xml -DskipTests
./mvnw clean verify -f distro/run/qa/pom.xml -Pintegration-test-camunda-run
```

Treat the first block as early compile/dependency smoke. Treat the second block as progressively broader module/runtime gates; do not continue to later commands while an earlier module gate has unresolved blockers.

# Running Integration Tests

The integration test suites are located under `qa/`. There you'll find a folder named XX-runtime for 
each server runtime we support. These projects are responsible for taking a runtime container 
distribution (ie. Apache Tomcat, WildFly AS ...) and configuring it for integration testing. The 
actual integration tests are located in the `qa/integration-tests-engine-jakarta` and `qa/integration-tests-webapps` modules.
 * *integration-tests-engine-jakarta*: This module contains the supported Spring Boot 4 / Jakarta EE 11 integration testsuite that tests the integration of the process engine within a particular runtime container. For example, such tests will ensure that if you use the Job Executor Service inside a Jakarta EE Container, you get a proper CDI request context spanning multiple EJB invocations or that EE resource injection works as expected. These integration tests are executed in-container, using [JBoss Arquillian](http://arquillian.org/).
 * *integration-tests-webapps*: This module tests the Flowee BPMS Platform webapplications inside the runtime containers. These integration tests run inside a client / server setting: the webapplication is deployed to the runtime container, the runtime container is started and the tests running inside a client VM perform requests against the deployed applications.

In order to run the integration tests, first perform a full install build. Then navigate to the `qa` folder.

We have different maven profiles for selecting
* *Runtime containers & environments*: tomcat, wildfly
* *The testsuite*: engine-integration-jakarta, webapps-integration
* *The database*: h2,h2-xa,db2,sqlserver,oracle,postgresql,postgresql-xa,mysql (Only h2 and 
  postgresql are supported in engine-integration-jakarta tests)

In order to configure the build, compose the profiles for runtime container, testsuite, database. Example:

```
mvn clean install -Pengine-integration-jakarta,wildfly,h2
```

If you want to test against an XA database, just add the corresponding XA database profile to the mvn cmdline above. Example:

```
mvn clean install -Pengine-integration-jakarta,wildfly,postgresql,postgresql-xa
```

You can select multiple testsuites but only a single database and a single runtime container. This is valid:

```
mvn clean install -Pengine-integration-jakarta,webapps-integration,tomcat,postgresql
```

There is a special profile for the WildFly Application Servers:

* WildFly Domain mode: `mvn clean install -Pengine-integration-jakarta,h2,wildfly-domain`

# Testing a Given Database

Flowee BPMS supports all database technologies listed on [Supported Database Products](https://finture.com/flowee-bpms/docs/manual/latest/introduction/supported-environments/#supported-database-products), and in all environments, they are operating in as specified. Support means we guarantee the Flowee BPMS Platform integrates well with the database technology’s JDBC behavior (there are some [documented](https://finture.com/flowee-bpms/docs/manual/latest/user-guide/process-engine/database/) limitations, e.g., isolation level `READ_COMMITTED` is required for all databases). We test a database technology with a specific database, i.e., we test it in one environment, not all possible environments that you can imagine (e.g., we test Postgres on local Docker containers, but not as hosted databases on AWS or Azure).

# No Maven? No problem!

This project provides a [Maven Wrapper](https://github.com/takari/maven-wrapper). This feature is useful for developers
to build and test the project with the same version that Flowee BPMS uses. It's also useful for developers that don't want
to install Maven at all. By executing the `mvnw` script (Unix), or `mvnw.cmd` script (Windows), a Maven distro will be 
downloaded and installed in the `$USER_HOME/.m2/wrapper/dists` folder of the system. You can check the download URL in
the [.mvn/wrapper/maven-wrapper.properties](.mvn/wrapper/maven-wrapper.properties) file.

The Maven Wrapper requires Maven commands to be executed from the root of the project. As the Flowee BPMS Platform project
is a multi-module (Maven Reactor) project, this is also a good best practice to apply.

To build the whole project, or just a module, one of the following commands may be executed:

```shell
# build the whole project
./mvnw clean install

# build the engine module
./mvnw clean install -f engine/pom.xml

# run the rolling-update IT tests with the H2 database
./mvnw verify -f qa/test-db-rolling-update/pom.xml -Prolling-update,h2
```

> Note: Above the `mvn -f` command line option is recommended over the `mvn -pl` option. The reason is that `-pl` will
build only the specified module, and will ignore any sub-modules that it might contain (unless the `-amd` option is also
added). As the Flowee BPMS Platform project has a multi-tiered module hierarchy (e.g. the [qa](qa/) module has modules of 
it's own), the `mvn -f` command option is simpler. 

## What about database technology X in environment Y?

To make a statement regarding Flowee BPMS Platform support, we need to understand if technology X is one of the technologies we already support or different technology. Several databases may share the same or a similar name, but they can still be different technologies: For example, IBM DB2 z/OS behaves quite differently from IBM DB2 on Linux, Unix, Windows. Amazon Aurora Postgres is different from a standard Postgres.

If you want to make sure that a given database works well with the Flowee BPMS Platform, you can run the test suite against this database.

In the `pom.xml` file located in the `./database` folder, several database profiles are defined with a matching database driver.

To run the test suite against a given database, select the `database` profile and your desired database profile and provide the connection parameters:

```
mvn test -Pdatabase,postgresql -Ddatabase.url=jdbc:postgresql:pgdb -Ddatabase.username=pguser -Ddatabase.password=pgpassword
```

## Testing a Flowee BPMS-supported Database with Testcontainers

It is also possible to use Testcontainers to run the test suite agains a given database. To ensure that your database 
Docker image can be used this way, please perform the following steps:

1. Ensure that your Docker image is compatible with Testcontainers;
1. Provide the repository name of your Docker image in the [testcontainers.properties](./engine/src/test/resources/testcontainers.properties) file;
   * If you use a private Docker repository, please include it in the Docker image name (e.g. private.registry.org/postgres)
1. In the `pom.xml` file located in the `./database` folder, check out the `database.tc.url` property to ensure that 
   the Docker tags match.
1. Make sure that the `testcontainers` profile is added to your Maven `settings.xml` (you can find it [here](settings/maven/nexus-settings.xml)).

At the moment, Testcontainers can be used with the Flowee BPMS-supported versions of the following databases. Please make 
sure that the database image is configured according to [this guide](https://finture.com/flowee-bpms/docs/manual/latest/user-guide/process-engine/database/database-configuration/#isolation-level-configuration):
* PostgreSQL
* MySQL
* MS-SQL 2017/2019 ([MSSQL-specific configuraion guide](https://finture.com/flowee-bpms/docs/manual/latest/user-guide/process-engine/database/mssql-configuration/))

To execute the process engine test suite with a certain database (e.g. PostgreSQL), you should call Maven in the 
engine directory with
```shell
mvn clean test -Ppostgresql,testcontainers
```

# Limiting the Number of Engine Unit Tests

Due to the fact that the number of unit tests in the Flowee BPMS engine increases daily and that you might just want to test a certain subset of tests the maven-surefire-plugin is configured in a way that you can include/exclude certain packages in your tests.

There are two properties that can be used for that: ``test.includes`` and ``test.excludes``

When using the includes only the packages listed will be include and with excludes the other way around.
For example calling Maven in the engine directory with
```
mvn clean test -Dtest.includes=bpmn
```
will test all packages that contain "bpmn". This will include e.g. ``*test.bpmn*`` and ``*api.bpmn*``. If you want to limit this further you have to get more concrete. Additionally, you can combine certain packages with a pipe:
```
mvn clean test -Dtest.includes=bpmn|cmmn
```
will execute all bpmn and cmmn tests.

The same works for excludes. Also, you can combine both:
```
mvn clean test -Dtest.includes=bpmn -Dtest.excludes=bpmn.async
```
Please note that excludes take precedence over includes.

To make it easier for you we created some profiles with predefined in- and excludes:
- testBpmn
- testCmmn
- testBpmnCmmn
- testExceptBpmn
- testExceptCmmn
- testExceptBpmnCmmn

So simply call
```
mvn clean test -PtestExceptBpmn
```
and all the bpmn testcases won't bother you any longer.
