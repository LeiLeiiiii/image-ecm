# AGENTS.md - Guidelines for Agentic Coding Agents

## Project Overview
This is a Java Spring Boot project with multiple modules using Maven as the build system. The project follows a microservices architecture with various Spring Cloud components.

## Build, Lint, and Test Commands

### Maven Build Commands
- **Full build**: `mvn clean package`
- **Skip tests**: `mvn clean package -DskipTests`
- **Compile only**: `mvn compile`
- **Install to local repo**: `mvn install`

### Test Commands
- **Run all tests**: `mvn test`
- **Run single test class**: `mvn test -Dtest=TestClassName`
- **Run single test method**: `mvn test -Dtest=TestClassName#methodName`
- **Skip tests**: `mvn package -DskipTests`
- **Generate test reports**: `mvn surefire-report:report`

### Code Quality and Analysis
- **Checkstyle**: `mvn checkstyle:check` (configured in pom.xml)
- **FindBugs**: `mvn findbugs:findbugs` 
- **PMD**: `mvn pmd:pmd` (uses Alibaba P3C rules)
- **Jacoco Coverage**: `mvn jacoco:report`
- **Site generation**: `mvn site`

### Module-Specific Commands
From the root pom.xml, modules include:
- ecm-common
- ecm-util  
- ecm-api
- ecm

To build specific module: `mvn -pl module-name package`

## Code Style Guidelines

### File Structure
- **Maximum file length**: 1500 lines (Checkstyle FileLength)
- **Maximum line length**: 150 characters (Checkstyle LineLength)
- **File encoding**: UTF-8
- **Must end with newline**: NewlineAtEndOfFile check

### Import Rules
- ❌ **NO star imports** (`import java.util.*;`) - severity: error
- ❌ **NO redundant imports** - severity: error  
- ❌ **NO unused imports** - severity: error
- ✅ Explicit imports only

### Naming Conventions
- **Packages**: `^[a-z]+(\.[a-z][a-z0-9]*)*$` (all lowercase)
- **Classes/Interfaces**: `^[A-Z][a-zA-Z0-9]*$` (PascalCase)
- **Methods**: `^[a-z][a-zA-Z0-9]*$` (camelCase)
- **Parameters**: `^[a-z][a-zA-Z0-9]*$` (camelCase)
- **Member Variables**: `^[a-z][a-zA-Z0-9]*$` (camelCase)
- **Constants**: `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` (UPPER_SNAKE_CASE)
- **Local/Final Variables**: `^[a-z][a-zA-Z0-9]*$` (camelCase)
- **Static Variables**: `^[a-z][a-zA-Z0-9]*$` (camelCase)

### Method Complexity Limits
- **Maximum parameters**: 5 (ParameterNumber check)
- **Maximum method length**: 300 lines (MethodLength check)
- **Maximum cyclomatic complexity**: 20 (CyclomaticComplexity check)
- **Maximum NPath complexity**: 20 (NPathComplexity check)
- **Maximum boolean expression complexity**: 8 (BooleanExpressionComplexity check)
- **Maximum ||, &&, ^ in expression**: 8
- **Maximum return statements**: 10 (ReturnCount check)
- **Maximum if nesting depth**: 5 (NestedIfDepth check)
- **Maximum for nesting depth**: 4 (NestedForDepth check)
- **Maximum try nesting depth**: 5 (NestedTryDepth check)

### Whitespace and Formatting
- ❌ **NO tab characters** - use spaces only (TabCharacter check)
- ❌ **NO whitespace after** typecast parentheses (TypecastParenPad check)
- ❌ **NO whitespace after** method name before opening paren (MethodParamPad check)
- ❌ **NO whitespace after** opening parenthesis (ParenPad check)
- ❌ **NO whitespace before** closing parenthesis (ParenPad check)
- ✅ Proper operator wrapping (OperatorWrap check)
- ✅ Proper whitespace around operators (WhitespaceAround check)
- ✅ Proper whitespace after operators (WhitespaceAfter check)
- ❌ **NO unnecessary parentheses** (UnnecessaryParentheses check)

### Javadoc Requirements
- ✅ **Required** for all public classes and interfaces (JavadocType check)
- ✅ **Required** for all public methods (MissingJavadocMethod check)
- ❌ **NOT required** to check first sentence for period (checkFirstSentence=false)
- ❌ **Allowed** missing @param tags (allowMissingParamTags=true)
- ❌ **Allowed** missing @return tag (allowMissingReturnTag=true)
- ❌ **Allowed** empty Javadoc (checkEmptyJavadoc=true)
- ✅ **Required** for fields unless matching ignore pattern (JavadocVariable check)
- 🚫 Ignored patterns for JavadocVariable: `.*Business|.*Service|.*Handler|.*Mapper|.*Template|.*Client|.*Util.*|LOG|LOGGER`

### Code Style Prohibitions
- ❌ **NO System.out/err prints** (RegexpSingleline warning)
- ❌ **NO printStackTrace** (Regexp illegalPattern=true)
- ❌ **NO System.out.println** (Regexp illegalPattern=true)
- ❌ **NO TODO comments** (TodoComment severity=error)
- ❌ **NO uncommented main methods** (except Application/Test classes) (UncommentedMain)
- ❌ **NO magic numbers** (MagicNumber module)
- ❌ **NO switch statements** (IllegalToken LITERAL_SWITCH)
- ❌ **NO fall-through in switch** (FallThrough module)
- ❌ **NO catching java.lang.Exception** (IllegalCatch)
- ❌ **NO == or != for String comparison** (StringLiteralEquality)
- ❌ **NO simplified boolean expressions** (SimplifyBooleanExpression, SimplifyBooleanReturn)
- ❌ **NO unmodified loop control variables** (ModifiedControlVariable)
- ❌ **NO redundant throws** (RedundantThrows)
- ❌ **NO unnecessary semicolons** (EmptyStatement)
- ❌ **NO trailing comments** (TrailingComment)
- ❌ **NO multiple variable declarations per line** (MultipleVariableDeclarations)
- ❌ **NO multiple string literals** (MultipleStringLiterals)
- ❌ **NO arrays in C style** (int[] vs int[]) - must be Java style (ArrayTypeStyle)
- ❌ **NO uppercase L in long literals** - must be uppercase L (UpperEll)

### Class Design Rules
- ❌ **NO public static final modifier disorder** (ModifierOrder)
- ❌ **NO redundant modifiers** (RedundantModifier)
- ✅ **Required explicit initialization** of fields (ExplicitInitialization)
- ❌ **NO invisible fields** (all fields must have package/protected access) (VisibilityModifier)
- ❌ **NO package without declaration** (PackageDeclaration)
- ❌ **NO equals without hashCode override** (EqualsHashCode)
- ❌ **NO equals without null check** (EqualsAvoidNull)
- ✅ **Required package declaration** 
- ✅ **Final classes with only private constructors** (FinalClass)
- ✅ **Interfaces are types only** (InterfaceIsType)
- ❌ **NO clone() without super.clone()** (SuperClone)
- ❌ **NO finalize() without super.finalize()** (SuperFinalize)
- ❌ **NO abstract classes without abstract methods** (AbstractClassWithoutAbstractMethod)
- ❌ **NO anonymous inner classes length > name length** (AnonymousInnerClasses)
- ❌ **NO hidden fields** (variables hiding class fields) (HiddenField)
- ❌ **NO inner assignments** (InnerAssignment: String s = Integer.toString(i = 2);)
- ❌ **NO overloaded methods in different declaration order** (OverloadMethodsDeclarationOrder)

### Additional Restrictions
- 🚫 **NO Javadoc missing for interface/method** (already covered)
- 🚫 **NO empty blocks** (EmptyBlock)
- 🚫 **NO unnecessary braces** (NeedBraces)
- 🚫 **NO nested blocks** (AvoidNestedBlocks - except in switch)
- 🚫 **NO left curly at wrong position** (LeftCurly option=eol)
- 🚫 **NO right curly at wrong position** (RightCurly option=same)
- 🚫 **NO empty statements** (EmptyStatement)
- 🚫 **NO non-final variables that never change** (FinalLocalVariable)

## Module Structure
Based on pom.xml analysis:
- **ecm-common**: Common utilities and shared code
- **ecm-util**: Utility functions and helpers
- **ecm-api**: API interfaces and DTOs
- **ecm**: Main service implementation

## Technology Stack
- **Java Version**: 1.8 (configured in properties)
- **Spring Boot**: 2.6.3
- **Spring Cloud**: Alibaba Nacos for service discovery and config
- **MyBatis**: ORM framework
- **Redis**: Caching and messaging
- **Elasticsearch**: Full-text search (7.14.0)
- **RabbitMQ**: Message queue (spring-boot-starter-amqp)
- **WebSocket**: Real-time communication
- **Swagger/OpenAPI**: springfox-boot-starter 3.0.0
- **Validation**: jakarta.validation 2.0.2
- **Logging**: Spring Boot starter logging
- **Testing**: JUnit 5, Mockito, Podam

## Best Practices for Agents
1. **Always run mvn checkstyle:check before submitting code**
2. **Write meaningful Javadoc for all public classes and methods**
3. **Follow exact naming conventions as specified**
4. **Keep methods small and focused (<300 lines, preferably much smaller)**
5. **Limit method parameters to 5 or fewer**
6. **Use explicit imports, never star imports**
7. **Handle exceptions properly, never catch generic Exception**
8. **Use proper null checks before String.equals()**
9. **Configure Checkstyle/FindBugs/PMD in your IDE for real-time feedback**
10. **Run unit tests frequently with mvn test**
11. **Follow the existing code patterns in the repository**
12. **When in doubt, examine similar existing code in the same module**

## Common Directory Structures
- `src/main/java` - Production Java source
- `src/main/resources` - Configuration, mappers, properties
- `src/test/java` - Test source
- `src/test/resources` - Test configuration

## Working with Specific Modules
When working in a specific module:
1. Check that module's pom.xml for dependencies
2. Follow the existing package structure in that module
3. Use the appropriate DTOs and services from ecm-common and ecm-util
4. Refer to ecm-api for API contracts when implementing endpoints

This guide should help agents produce code that fits seamlessly with the existing codebase and passes all quality checks.