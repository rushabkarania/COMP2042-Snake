# Final class design

`../Design.vpp` is the editable Visual Paradigm 18.1 project. `../Design.pdf` contains its three native diagram exports, combined into one PDF without redrawing the diagrams. The former Graphviz source has been replaced.

The coursework brief, page 3, requires a clearly labelled high-level diagram of the final game's classes, interfaces, relationships and multiplicity, omitting routine attributes and methods. The final source has 25 top-level production classes in `com.comp2042`; all are represented. Two significant nested types are also included: `GameController.TurnBuffer` and `SnakeBodyPainter.Surface`. Framework types, enums, records and the anonymous rendering adapter are omitted at this level.

## Reading the views

1. **Application and control:** launch, window coordination, controller, view and session; the controller's owned turn buffer and sound helper.
2. **Game model and collectibles:** session references, owned run objects, placement dependencies, hedge layout checks and the five subclasses of Coin.
3. **Presentation and rendering:** assets, icons, sprite selection and the rendering interface boundary.

Repeated classes refer to the same elements in the editable model. Solid navigable associations describe retained references; filled diamonds indicate ownership. Multiplicities describe an active game, with `0..1` used where initialisation may not yet have happened. A field can be shared without being composition: Game is retained by both GameSession and GameView. Snake keeps its Obstacle reference; Coin only receives Snake and Obstacle during placement. Those temporary uses are dashed dependencies, without multiplicity. A hedge field can be referenced by multiple successive/injected Snake objects, hence `0..*` at the Snake end of that link.

Generalisation triangles point towards Coin. Surface is the interface accepted by SnakeBodyPainter. SnakeRenderer creates an anonymous implementation of Surface; SnakeRenderer itself does not realise that interface. This distinction is intentional.

## Authoring and verification

The model was created inside the running Visual Paradigm desktop application using its supported OpenAPI, saved as a native `.vpp`, then opened again in Visual Paradigm. Classes and connectors are editable UML model elements, not a pasted image. Layout, labels, relationship types and multiplicities were checked against the final Java sources.

Checks cover the native project database integrity, three class-diagram views, 27 class/interface model elements, correct ownership of the two nested types, no Attribute/Operation model elements, and text coverage of all 25 production class names in the three-page landscape A3 PDF. The game implementation was unchanged for this documentation update; the existing 158 passing game checks are recorded in Validation.md.

This environment has Visual Paradigm Enterprise's legitimate evaluation edition. Its export watermarks are preserved. For an unwatermarked submission, open Design.vpp with an appropriately licensed university installation and export the three class diagrams to PDF. No custom plugin is needed to open or edit the saved project.

The design authoring and source audit used AI assistance, as part of the implementation assistance already declared in GardenRevision.md.
