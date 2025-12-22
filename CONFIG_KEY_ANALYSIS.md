# Configuration Key Analysis Report

## Configuration Key: `print.service.send.data.threads.count`

### Investigation Summary
**Date:** 2025-12-22  
**Status:** NOT FOUND

### Comprehensive Search Results

#### 1. Direct Search
The configuration key `print.service.send.data.threads.count` does **NOT exist** in the current repository.

**Search Locations:**
- All `.properties` files
- All `.yml` and `.yaml` files
- All `.xml` configuration files
- All Java source files (`.java`)
- Git commit history (all branches)
- Documentation files (`.md`)

#### 2. Pattern Variations Searched
The following pattern variations were also searched without any matches:
- `print.service.send.data.threads.count` (exact match)
- `send.data.threads.count`
- `send.data.threads`
- `sendDataThreads` (camelCase)
- `sendDataThreadsCount` (camelCase)
- `threads.count`
- `threadsCount`

#### 3. Current Thread Configuration
The repository DOES contain thread pool configuration, but NOT with the specified key name:

**Location:** `/src/main/java/io/mosip/print/config/PrintConfig.java`
```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);  // Hardcoded value
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    return threadPoolTaskScheduler;
}
```

**Key Finding:** The thread pool size is currently **hardcoded to 5**, not configured via properties.

#### 4. Existing Configuration Pattern
The repository uses configuration keys following this pattern:
- `mosip.print.service.uincard.lowerleftx`
- `mosip.print.service.uincard.lowerlefty`
- `mosip.print.service.uincard.upperrightx`
- `mosip.print.service.uincard.upperrighty`
- `mosip.print.service.uincard.signature.reason`
- `mosip.print.service.uincard.password`
- `mosip.print.perso.apiKey`
- `mosip.print.perso.secretKey`
- `mosip.print.perso.api`

**Pattern:** Most configuration keys start with `mosip.print.` prefix, not just `print.service.`

#### 5. Configuration Files Checked
- `/src/main/resources/application-local1.properties` (160 lines)
- `/src/main/resources/bootstrap.properties` (11 lines)
- `/pom.xml` (Maven configuration)
- `.travis.yml` (CI configuration)
- `.github/workflows/*.yml` (GitHub Actions)

#### 6. Git History Analysis
- Searched all commits for the configuration key: **NOT FOUND**
- Searched for commits mentioning "threads": **NOT FOUND**
- Latest commits checked: 7d10716 and 7bad413

### Possible Scenarios

#### Scenario A: Configuration Never Existed
The configuration key `print.service.send.data.threads.count` may have been mentioned in:
- External documentation
- Feature requirements
- Planned functionality that was never implemented

#### Scenario B: Configuration Exists Externally
The repository uses Spring Cloud Config Server:
```properties
config.server.file.storage.uri=https://apiinternal-mosiphq.nira.go.ug/config/print/mz/develop2-v2/
```
The configuration might exist in external configuration files that are not part of this repository.

#### Scenario C: Configuration Removed
The configuration may have existed in the past but was removed. However, Git history shows no evidence of this.

### Recommendations

If you need to implement thread pool configuration:

1. **Add the configuration key** to `/src/main/resources/application-local1.properties`:
   ```properties
   print.service.send.data.threads.count=5
   ```

2. **Modify** `/src/main/java/io/mosip/print/config/PrintConfig.java`:
   ```java
   @Value("${print.service.send.data.threads.count:5}")
   private int threadPoolSize;
   
   @Bean
   public TaskScheduler taskScheduler() {
       ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
       threadPoolTaskScheduler.setPoolSize(threadPoolSize);
       threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
       return threadPoolTaskScheduler;
   }
   ```

### Conclusion

**The configuration key `print.service.send.data.threads.count` is NOT used anywhere in the current repository.**

---

### Search Commands Used
```bash
# Direct searches
grep -r "print.service.send.data.threads.count" .
grep -r "send.data.threads.count" .
grep -r "threads.count" .

# Git history
git log --all -S "print.service.send.data.threads.count"
git log --all -S "threads.count"
git log --all --oneline --grep="threads"

# File type specific
find . -name "*.properties" -exec grep -l "threads" {} \;
find . -name "*.java" -exec grep -l "threads" {} \;
```

### Files Analyzed
- Total Java files: 206
- Total configuration files: 5
- Git commits analyzed: All branches and commits
- Documentation files: 3 (README.md, configuration.md, build-and-run.md)
