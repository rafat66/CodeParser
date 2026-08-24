# CodeParser
Eclipse-based Java code parser plugin that analyzes Java projects and generates XML files containing software metrics, object-oriented code elements, and relationships.

**CodeParser** is an Eclipse-based Java code parser plugin that analyzes Java projects and generates XML files containing software metrics, object-oriented code elements, and relationships.

The plugin uses the **Eclipse JDT (Java Development Tools) AST parser** to analyze Java source code and extract structural and software engineering information from Java projects.

---

## Overview

CodeParser is designed to support the analysis of Java software systems by automatically extracting important source-code information.

The parser analyzes Java projects available in the Eclipse workspace and generates XML representations of:

- Packages
- Classes
- Interfaces
- Attributes
- Methods
- Method parameters
- Local variables
- Comments
- Inheritance
- Implemented interfaces
- Method invocations
- Attribute accesses
- Assignments
- Method exceptions
- Software metrics

The generated XML files can be used as input for further software visualization, software architecture analysis, reverse engineering, and software metrics research.

---

## Features

### Java Project Analysis

CodeParser automatically detects open Java projects in the Eclipse workspace and analyzes their source code.

### Package Detection

The parser identifies Java packages and represents them in the generated XML structure.

Example:

```xml
<Package PackageName="com.example.model">
    ...
</Package>
