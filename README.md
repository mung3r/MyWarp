![MyWarp](http://dev.bukkit.org/media/images/55/821/MyWarp_horLogo.png)
==========
[![Build Status](https://travis-ci.org/TheE/MyWarp.svg)](https://travis-ci.org/TheE/MyWarp)

MyWarp is a dynamic and social warp plugin for Minecraft. It allows you to:

* Create private and public warps,
* Invite and uninvite players or whole permission-groups to warps or make them public so everyone can use them,
* List all warps you can use and profit from the intelligent mechanism that determines warp-names based on name-parts,
* Use creation-limits to regulate how many warps of a type a user can make,
* Create warmups and/or cooldowns to regulate warp-usage even more,
* Regulate access to warps based on worlds,
* Create warp signs and connect them to buttons or levers,
* Charge your users for using, creating, listing or any other warp-related task via Vault,
* Translate every message the plugin outputs, either so it fits your servers theme of even into a whole new language,
* Show warps on Dynmap using the built-in support.

MyWarp is open source and is available under the GNU General Public License v3.

Compiling
---------

You can compile MyWarp as long as you have the [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) for Java 6 or newer. You only need one version of JDK installed.

The build process uses Gradle, which you do *not* need to download. MyWarp is a multi-module project with two modules:

* `mywarp-core` contains MyWarp
* `mywarp-bukkit` is the Bukkit plugin

### To compile...

#### On Windows

1. Shift + right click the folder with MyWarp's files and click "Open command prompt".
2. `gradlew build`

#### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with MyWarp's files (`cd /folder/of/mywarp/files`)
2. `./gradlew build`

### Then you will find...

You will find:

* The core MyWarp API in **mywarp-core/build/libs**
* MyWarp for Bukkit in **mywarp-bukkit/build/libs**

If you want to use MyWarp, use the `-all` version which includes MyWarp and all necessary libraries.

Contributing
------------

We accept contributions, especially through pull requests on GitHub. Submissions must be licensed under the GNU General Public License v3.

Please read CONTRIBUTING.md for important guidelines to follow.
