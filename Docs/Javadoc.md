# Generating the coursework Javadoc

The source comments describe class responsibilities, method inputs and return values,
including package-private gameplay and rendering helpers. `package-info.java` gives
the package overview. Four previously implicit zero-argument constructors are written
explicitly so that their documentation is included; their behaviour is unchanged.

The Maven Javadoc plugin is pinned to 3.11.2, includes package-private members, uses
UTF-8 and enables all DocLint checks. Warnings cause generation to fail instead of
silently accepting incomplete documentation.

On Windows, from the project directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Generate-Javadoc.ps1
Start-Process .\Javadoc\index.html
```

The script runs `mvnw.cmd clean javadoc:javadoc`. It checks the exit status and selected
helper pages before copying the generated site from `target/reports/apidocs` into the
coursework's root `Javadoc` folder. `clean` removes temporary build output, so package
the application again afterwards if a runnable JAR is needed. The execution-policy
override applies only to this PowerShell process.

On Linux/macOS:

```sh
./mvnw clean javadoc:javadoc && mkdir -p Javadoc && cp -R target/reports/apidocs/. Javadoc/
```

Commit source comments, the configuration, this guide, the generation script and the
complete generated `Javadoc` folder. Do not commit `target`. Regenerate after changing
source comments or APIs. Preserve README and testing history separately.

## Validation of this update

A Java syntax-tree check found no syntax errors and verified documentation coverage
for the declarations included at package access level. A token comparison against the
uploaded source confirmed unchanged executable code apart from the four equivalent
constructors. The POM parses as XML. These static checks are not a substitute for a
successful Maven Javadoc run with the project's JDK and JavaFX dependencies.

Windows validation completed on 18 November 2026 after adding the
documented SnakeBodyPainter constructor. Generate-Javadoc.ps1 finished
with BUILD SUCCESS and copied the generated site to Javadoc/index.html.
No Javadoc warnings were reported. Maven startup dependency warnings
remained separate from Javadoc generation.
