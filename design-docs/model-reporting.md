This spec defines some work to add some basic reporting on the configuration model, to give insight into:

- What is available in the model?
- What are certain values in the model set to?
- Where did a particular model element or value come from?
- What are the dependencies between the part of the model?

# Story: Model report shows display value for model elements with no children

Change the `model` report to show some display value for model elements with no children, eg the result of
`toString()` on its value.

- Tasks, source sets, etc
- Simple values on `@Managed` types

## Notes

- `ModelReport` is the task that renders the report
- `ModelNode` represents a model element

# Story: Model report shows type information for model elements

Change the `model` report to show some information about the views that are available for each element.

- Component model
- `@Managed` types

# Story: Model report shows details of rule that created a model element

Change the `model` report to show the descriptor of the creator rule for each element.

Possibly don't show this when same as the parent element (see how it looks)

Possibly add a 'show details' command-line option to enable this.

# Story: Model report shows details of rules that affected a model element

Change the `model` report to show the descriptor of those rules that affected each element.

Will need to collect this in the model nodes.

# Story: Model report shows hidden nodes

Add a 'show hidden elements' command-line option to show hidden nodes.

# Story: Add report that shows dependencies between model elements

Add a new report that shows the dependency graph between model elements.

# Story: Report shows details about which plugin defined a rule

Improve the display value for rules defined in plugins, to show the plugin id instead of the detailed
method descriptor.

Perhaps use a 'show details' command-line option to enable display of the method descriptor.
