# Debug Plugin

This is a plugin which adds tools to help developers debug [Chunky](https://github.com/chunky-dev/chunky).

## Installation

1. Get the latest plugin release.
2. In the Chunky Launcher, expand `Advanced Settings` and click on `Manage Plugins`.
3. In the `Plugin Manager` window, click on `Add` and select the `.jar`.
4. In IntelliJ, open the `File` menu and select `Project Structure`.
5. Click on `Modules` and select the `Dependencies` tab.
6. Add the `.jar` as a dependency, then select it and click the `Edit` symbol.
7. Remove the `Classes` section so only `Sources` remain.
8. Add `java.lang.AssertionError` as an exception breakpoint in the debugger.
