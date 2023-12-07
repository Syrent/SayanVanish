# About
VelocityVanish is one of the best vanish plugins and only vanish plugin that supports Velocity (It also can run on spigot servers without any problem).
More information can be found at [Why VelocityVanish](https://github.com/Syrent/VelocityVanish/wiki/Why-VelocityVanish%3F)? [wiki page](https://github.com/Syrent/VelocityVanish/wiki).

# Compiling
Compilation requires JDK 8 and up.   
To compile the plugin, run `./gradlew build` from the terminal.   
Once the plugin compiles, grab the jar from `/bin` folder.   

# Links
The precompiled JAR can be downloaded for free from:

* GitHub releases: https://github.com/Syrent/VelocityVanish/releases
* Hangar (Recommended): https://hangar.papermc.io/Syrent/VelocityVanish
* Modrinth: https://modrinth.com/plugin/velocityvanish


Legacy (Not recommended):
* Spigot: https://www.spigotmc.org/resources/velocityvanish-1-8-1-19-2-no-database-required.105992/
* Polymart: https://polymart.org/resource/velocityvanish.3067



Other links:

* Wiki: https://github.com/Syrent/VelocityVanish/wiki
* Paper's forum: https://forums.papermc.io/threads/velocityvanish-modern-vanish-system-for-your-server-with-velocity-support.552/

# Contributing
If this plugin has helped you in any way, and you would like to return something back to make the plugin even better, there is a lot of ways to contribute:

* Open a **pull request** containing a new feature or a bug fix, which you believe many users will benefit from.
Make detailed high-quality bug reports. The difference between a bug getting fixed in 1 week vs 1 hour is in quality of the report (typically providing correct steps to reproduce that actually work).
* Help improve the wiki by opening a Wiki change issue, where you can improve existing descriptions, add information you found missing, fix typos / grammar mistakes or add more examples of usage of functions.

## Integration
You can easily integrate VelocityVanish into your project by following the instructions below.

### Gradle
[![](https://jitpack.io/v/Syrent/VelocityVanish.svg)](https://jitpack.io/#Syrent/VelocityVanish)

Add the Jitpack repository to your `build.gradle` file:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```
Add the VelocityVanish dependency:
```gradle
dependencies {
    implementation 'com.github.Syrent:VelocityVanish:Tag'
}
```
* Replace Tag with the desired version of VelocityVanish.

### Maven
Add the Jitpack repository to your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add the VelocityVanish dependency:
```xml
<dependency>
    <groupId>com.github.Syrent</groupId>
    <artifactId>VelocityVanish</artifactId>
    <version>Tag</version>
</dependency>
```
* Replace Tag with the desired version of VelocityVanish.
