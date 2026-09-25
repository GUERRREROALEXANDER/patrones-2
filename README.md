# Tidekeeper

**A Java desktop case study of Adapter and Bridge in maritime heritage conservation.**

## Real-world case

Conservators of objects recovered from shipwrecks face an unusual software integration problem: instruments can provide incompatible readings, while different conservation assessments need different report presentations.

The real-world inspiration is the Mary Rose Trust's conservation work. Its collection includes wooden and inorganic objects that require different treatments. Waterlogged wood can deform when dried without appropriate treatment; salts are a problem for inorganic objects. The Trust describes desalination baths and monitoring salt levels in the solution.

Source: [Mary Rose Trust — Conservation of artefacts](https://maryrose.org/discover/conservation/artefacts/).

Tidekeeper is a fictional teaching application inspired by that context, not software used or endorsed by the Trust. Its artifact identifiers, vendor SDKs, measurements, and thresholds are invented. Conductivity is displayed as a bath-screening measurement, not a direct measurement of artifact salt content. No real hardware, treatment control, or live museum data is connected.

## Run in Visual Studio Code

Requirements: JDK 17 or later, including `java` and `javac` on PATH. Developed and tested with JDK 26. No Maven, Gradle, database, network service, or external Java libraries are required.

1. Open this folder in Visual Studio Code.
2. Press **Ctrl+Shift+B** to run the **Run Tidekeeper** task.
3. Alternatively, run this command in the integrated PowerShell terminal:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

The command's execution-policy override only applies to that PowerShell process. It does not change your permanent Windows settings.

If Java debugging support is already installed in VS Code, use **Run and Debug > Launch Tidekeeper**. The build task works without an editor extension. No plugins or skills need to be installed.

## Demonstration

1. Keep the default oak item, legacy probe, storage assessment, and visual report.
2. Click **Run assessment**. `68;55;0.35` becomes `20 C`, `55% RH`, and `350 uS/cm`.
3. Open **Pattern trace** to inspect the concrete objects involved.
4. Select **Metric station**. Its equivalent input is `20`, `0.55`, and `350`.
5. Select **Field note (plain text)** and rerun. The assessment stays the same; the Bridge implementation changes.
6. Click **Load review example**, then **Run assessment**. The storage screen now requests review.
7. Choose **Desalination bath**, run again, and inspect the conductivity observation.
8. Use **Save report...** to export the last successful snapshot as UTF-8 HTML or text.
9. Try an invalid legacy packet such as `broken`. The error appears in the status bar and export is disabled until a successful run.

Changing selections does not regenerate a report automatically. Click **Run assessment** to create a new snapshot. Data is kept in memory; exported reports are the only persisted application output.

## Adapter

**Problem:** incompatible vendor interfaces and units cannot be used directly by the assessment service.

| Role | Class | Responsibility |
| --- | --- | --- |
| Target | `SensorSource` | Offers the common `read()` operation |
| Adaptee | `LegacyProbe` | Supplies a semicolon packet in Fahrenheit, RH percent, and mS/cm |
| Adaptee | `MetricStation` | Supplies a map in Celsius, humidity fraction, and uS/cm |
| Adapter | `LegacyProbeAdapter` | Parses a packet and converts Fahrenheit and conductivity units |
| Adapter | `MetricStationAdapter` | Reads map keys and converts humidity fraction to percent |
| Client | `AssessmentService` | Consumes `SensorSource` without knowing vendor details |

These are object adapters: each wraps an existing vendor object using composition. The simulated SDK classes expose deliberately incompatible methods and do not implement `SensorSource`.

## Bridge

**Problem:** assessment logic and report presentation are two independent dimensions. Creating classes such as `HtmlStorageAssessment` and `TextStorageAssessment` would duplicate combinations.

| Role | Class |
| --- | --- |
| Abstraction | `ConservationAssessment` |
| Refined abstractions | `StorageAssessment`, `DesalinationAssessment` |
| Implementor | `ReportRenderer` |
| Concrete implementors | `HtmlRenderer`, `PlainTextRenderer` |

`ConservationAssessment` holds a `ReportRenderer`. Its subclasses determine the observations, and its `createReport()` method delegates presentation to the implementor. Every assessment works with every renderer. The two hierarchies can grow independently without introducing an assessment subclass for every output format.

Adapter reconciles an already incompatible interface. Bridge deliberately separates independent abstraction and implementation hierarchies. The report renderer is the Bridge implementation; the sensor adapter is a separate input integration layer.

## Object-oriented design

- **Encapsulation:** SDK wrappers, adapters, and renderers keep their state private; records hold immutable domain values.
- **Abstraction:** the service depends on `SensorSource` and `ConservationAssessment`, while assessments depend on `ReportRenderer`.
- **Inheritance:** the two specialized assessments extend `ConservationAssessment`.
- **Polymorphism:** the service runs every adapter and assessment through their shared types; rendering is dynamically dispatched.
- **Composition:** adapters hold SDK objects, and the Bridge abstraction holds a renderer.
- **Separation of responsibilities:** Swing handles interaction, the service coordinates the use case, adapters normalize input, assessments create observations, and renderers format output.

## Educational rules

These are invented demonstration rules, not scientific conservation limits:

| Assessment | Demo rule |
| --- | --- |
| Storage climate | Temperature 16–22 C and humidity 45–60%, inclusive |
| Desalination bath | Conductivity at or below 500 uS/cm; iron and ceramic examples only |
| Wood in bath screen | Returns `NOT APPLICABLE` |

Storage screening assumes an already treated artifact. A reading inside a demo range does not prove an artifact is safe or treatment is complete. Conductivity trends, material-specific protocols, and specialist evaluation are outside this academic application's scope.

## Verification

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Test
```

The tests check unit normalization, malformed inputs, missing measurements, non-finite numbers, threshold boundaries, material restrictions, HTML escaping, and all eight combinations of sensor, assessment, and renderer. They also trigger the actual Swing button workflow on the event dispatch thread, check error recovery, and render `docs/dashboard-preview.png` without opening a window.

The file chooser and native desktop window should also be checked manually: resize the window, tab through inputs, save both report formats, and test declining an overwrite. Headless tests do not validate operating-system dialogs.

## Project layout

```text
src/tidekeeper/
  Main.java                 Application entry point
  model/                    Immutable domain values
  vendor/                   Simulated incompatible SDKs
  adapter/                  Adapter target and implementations
  bridge/                   Assessment and rendering hierarchies
  service/                  Use-case coordination
  ui/                       Java Swing frontend
test/tidekeeper/             Executable tests, no testing library required
docs/                       Class diagram and generated preview
.vscode/                    Build, test, and launch configuration
run.ps1                     Compile, launch, or test
```

See [the class diagram](docs/architecture.md).

## Extension exercise

To add a third vendor, implement `SensorSource` with a new adapter. To add an assessment, extend `ConservationAssessment`. To add an output format, implement `ReportRenderer`. Existing domain classes do not change; the UI composition code must expose any newly added option.
# patrones-2
