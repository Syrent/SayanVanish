# Introduction

Welcome to the introduction for **SayanVanish**, a versatile vanish system that allows you to disappear and reappear on your server network at will, along with many other features.

### Supported Platforms

SayanVanish is compatible with the following platforms:

* **Minecraft**
  * **Bukkit**
  * **Proxy**
    * **Velocity**
    * **Bungeecord**

### Key Features

#### Modularity

SayanVanish is designed as a modular system. Each module serves a specific purpose, which simplifies the maintenance and extension of the plugin.

#### Extensive API

SayanVanish includes a comprehensive API, allowing other plugins to interact with it and utilize its features.

#### Integration with Other Plugins

The API provided by SayanVanish enables deep integration with other plugins, facilitating the development of additional server management tools.

***

For detailed setup and usage instructions, please refer to the subsequent sections.
# How It Works?

SayanVanish uses a modular system to operate independently of specific platforms.

### API Module

The `api` module is designed for universal use across all platforms, providing access to necessary data.

#### User Interface

The `User` interface contains all the basic data you might need. It is structured as follows:
![image](https://github.com/Syrent/SayanVanish/assets/56670179/c2fef0bb-c16b-4a12-aa51-2db30760486a)

### Database

The main `Database` interface includes all common database functions, which are implemented in various database classes such as SQL or Redis. This database is crucial for syncing data between multiple servers and SayanVanish instances across different networks.

#### Database Structure

The database structure is depicted below:
![image](https://github.com/Syrent/SayanVanish/assets/56670179/53bd3ee5-ddf4-4147-8989-fa1c0bee97dc)
![image](https://github.com/Syrent/SayanVanish/assets/56670179/89efba5a-383e-4bea-b683-769ee2acf335)


### Features

Features in SayanVanish can be used to implement complex logic with certain constraints:

1. All parameters within a feature must have default values.
2. All parameters should be basic objects like `String`, `Integer`, `Boolean`, etc.

#### Rules for Features

Additionally, adhere to the following rules:

1. Variables that should not be serialized must be annotated with `transient`.

The feature structure is illustrated as follows:
![image](https://github.com/Syrent/SayanVanish/assets/56670179/6a08bdf1-a24f-41f9-9910-9b7a251df204)


***

# How to Use the API

SayanVanish contains an extensive API to perform all necessary actions. To start, add the SayanVanish dependency to your build tool of choice, such as Maven or Gradle.

#### Adding the Repository

**In Maven**

```xml
<repository>
  <id>repo-sayandevelopment</id>
  <name>SayanDevelopment Repository</name>
  <url>https://repo.sayandev.org/repository</url>
</repository>
```

**In Gradle**

* **Groovy**

```groovy
maven {
    url "https://repo.sayandev.org/repository"
}
```

* **Kotlin**

```kotlin
maven {
    url = uri("https://repo.sayandev.org/repository")
}
```

#### Adding the Dependency

You can use different platform-specific dependencies or the main platform-independent API.

**Main API Dependency**

The dependency artifact for the main API is `sayanvanish-api`.

**Platform-Specific Dependencies**

For platform-specific dependencies, the artifact ID follows the format `sayanvanish-<platform>`. Examples include `sayanvanish-bukkit` and `sayanvanish-proxy-velocity`.

**In Maven**

```xml
<dependency>
  <groupId>org.sayandev</groupId>
  <artifactId>sayanvanish-bukkit</artifactId>
  <version>[get version from github]</version>
</dependency>
```

**In Gradle**

* **Groovy**

```groovy
implementation "org.sayandev:sayanvanish-bukkit:[get version from github]"
```

* **Kotlin**

```kotlin
implementation("org.sayandev:sayanvanish-bukkit:[get version from github]")
```

#### Accessing the API

You can access the SayanVanish API like this:

```java
SayanVanishAPI.getInstance();
```

Or you can use a platform-specific user type like this:

```java
SayanVanishAPI<BukkitUser>.getInstance();
```

Or like this:

```java
SayanVanishBukkitAPI.getInstance();
```

From this class, you can access and modify everything. For example, to add a new user:

```java
SayanVanishAPI.getInstance().addUser(User);
```

Or to access the database directly:

```java
SayanVanishAPI.getInstance().getDatabase();
```

#### Events

There are two Bukkit-specific events you can use: `BukkitUserVanishEvent` and `BukkitUserUnVanishEvent`. Both events contain the user and vanish options.

***
