# Build Issue Resolution

## Problem
The command `mvn clean install` failed with 98 compilation errors:
```
Fatal error compiling: java.lang.ExceptionInInitializerError: 
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

All errors were related to missing Lombok-generated methods (getters, setters, builders):
- `cannot find symbol: method getType()`
- `cannot find symbol: method builder()`
- etc.

## Root Cause
**Lombok 1.18.36 is not fully compatible with Java 25.** 

The user had Java 25.0.1 installed, but the project targets Java 17 LTS. Lombok's annotation processor failed to generate getter/setter/builder methods when running under Java 25, causing widespread compilation failures.

## Solution
**Use Java 17 LTS instead of Java 25.**

### Steps Taken:

1. **Verified Java 17 was already installed** via Homebrew:
   ```bash
   brew info openjdk@17
   # Location: /usr/local/Cellar/openjdk@17/17.0.18
   ```

2. **Updated pom.xml** to explicitly configure Lombok annotation processor:
   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <version>3.11.0</version>
       <configuration>
           <release>17</release>
           <compilerArgs>
               <arg>-parameters</arg>
           </compilerArgs>
           <annotationProcessorPaths>
               <path>
                   <groupId>org.projectlombok</groupId>
                   <artifactId>lombok</artifactId>
                   <version>1.18.36</version>
               </path>
           </annotationProcessorPaths>
       </configuration>
   </plugin>
   ```

3. **Successfully built with Java 17**:
   ```bash
   JAVA_HOME="/usr/local/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home" \
       mvn clean install
   
   # Result: BUILD SUCCESS
   ```

4. **Created helper files** for easier Java usage:
   - `set-java.sh` - Script to check Java environment and display project info
   - Updated `demo/run.sh` - Auto-detects and uses Java 17 if installed via Homebrew
   - Updated `HOWTORUN.md` - Added Java 17 requirement and troubleshooting section

## How to Build Going Forward

### Option 1: Use the Startup Script (Recommended)
```bash
./demo/run.sh
```
The script automatically uses Java 17 if installed via Homebrew.

### Option 2: Check Java Environment
```bash
# Check your Java environment and project configuration
source set-java.sh

# Then build
mvn clean install
```

### Option 3: Prefix Maven Commands
```bash
JAVA_HOME="/usr/local/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home" \
    mvn clean install
```

## Verification
After the fix, the build succeeded:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  4.238 s
[INFO] Finished at: 2026-01-26T18:33:50+02:00
```

All 34 source files compiled successfully, and Lombok correctly generated all getter/setter/builder methods.

## Key Takeaway
**Always use Java 17 LTS for this project.** Java 21 and Java 25 have known Lombok compatibility issues that prevent successful compilation.
