# XEStoNESTConverter - Documentation
### Version: 2.0.0
### Description
This project contains code for conversion from a *.xes file
to a list of `NESTSequentialWorkflowObject`s.
NESTSequentialWorkflowObject is a class from the
__ProCAKE framework__ developed at Trier University.
For information on the ProCAKE framework
[see here](https://procake.pages.gitlab.rlp.net/procake-wiki/).
For information on the XES standart
[see here](https://xes-standard.org).

### Usage
To convert a `String` containing an XES log, first an
implementation of the abstract class `XEStoNESTConverter`
has to be initialized using a `DataModel`:

```java
Model model = ModelFactory.getDefaultModel();
AbstractXEStoNESTConverter converter = new XEStoNESTsAXConverter(model);
```

The converter used in this example is the __SAX parser__
based converter `XEStoNESTsAXConverter`, which currently
is the only implementation of `XEStoNESTConverter`.

After initializing the converter, the
`configure(boolean addGlobals)` method can be called
to configure the converter.
As of now, the only argument is a boolean `addGlobals`,
specifying whether global trace or event attributes
are to be added to traces or events if not present.
See in the example below:

```java
converter.configure(true);
```

After initialization and configuration, the
`convert(String xes)` method can be called, returning
an `ArrayList` of `NESTSequentialWorkflowObject`s. The
argument xes should be a `String` containing a
[valid XES log](#validation).

Below you can see the full example:

```java
Model model = ModelFactory.getDefaultModel();
AbstractXEStoNESTConverter converter = new XEStoNESTsAXConverter(model);
converter.configure(true);
String xes = Files.readString("path/to/file.xes", StandardCharset.UTF_8);
ArrayList<NESTSequentialWorkflowObject> workflows = converter.convert(xes);
```

### Validation

The file containing the schema the XES should be
valid against can be found at `main/resources/OCv1.xsd`.

If the Converter encounters a validation error, an error
message is passed to the logger and null is returned by
the `convert` method.