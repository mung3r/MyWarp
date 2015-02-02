Contributing
============

Thank you for your interest in contributing to MyWarp! We appreciate your effort, but to make sure that the inclusion of your patch is a smooth process, we ask that you make note of the following guidelines.

Please note that MyWarp is build against **Java 6**. All contributions must be licensed under the GNU General Public License v3.

Coding Style
---------
MyWarp follows the [Google coding conventions](https://google-styleguide.googlecode.com/svn-history/r130/trunk/javaguide.html) with two modifications:
1. The column limit is set to 120 characters. This replaces point 4.4, while the exceptions defined there still apply.
2. Switch cases can come without a `default` statement if, and only if, they cover all possible cases. This replaces point 4.8.4.3.
3. All files must have the license header that can be found in `config/checkstyle/header.txt`.
4. The `@author` tag in java-docs is forbidden.

The build process automatically checks most of these conventions using Checkstyle.

>**Note:** You can use our code styles for [Eclipse](https://code.google.com/p/google-styleguide/source/browse/trunk/eclipse-java-google-style.xml) or [IntelliJ IDEA](https://code.google.com/p/google-styleguide/source/browse/trunk/intellij-java-google-style.xml) to let your IDE format the code correctly for you.


Code Conventions
---------
* Use [Optionals](https://code.google.com/p/guava-libraries/wiki/UsingAndAvoidingNullExplained) instead of returning `null`.
* Method parameters accepting `null` must be annotated with `@Nullable` (from javax.*), all methods and parameters are `@Nonnull` by default.
* Use [Google Preconditions](https://code.google.com/p/guava-libraries/wiki/PreconditionsExplained) for null- and argument checking.
* If a class needs to log something, create a private static final instance of `java.util.logging.Logger` with the classes name as the logger's name.
