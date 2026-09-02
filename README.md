# CodeParser
Eclipse-based Java code parser plugin that analyzes Java projects and generates XML files containing software metrics, object-oriented code elements, and relationships.

<img src="Images/CodeParser0.png" alt="CodeParser" width="500">

**CodeParser** is an Eclipse-based Java code parser plugin that analyzes Java projects and generates XML files containing software metrics, object-oriented code elements, and relationships.

The plugin uses the **Eclipse JDT (Java Development Tools) AST parser** to analyze Java source code and extract structural and software engineering information from Java projects.

---

## 🎥 CodeParser Tutorial

> **▶️ Tutorial:** Click the image below to watch the complete CodeParser tutorial.

<a href="https://drive.google.com/file/d/1ddQ4qqoJxnbrWJM5qvo9BQ64MOkCyCsU/view?usp=sharing">
  <img src="Images/CodeParser.jpg" alt="CodeParser Tutorial" width="500">
</a>

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

<img src="Images/MPs.png" alt="Software code metrics for mobile photo software" width="340">

<a href="https://github.com/rafat66/CodeParser/blob/main/CodeParser/XML/ArgoUML%20software%20-%20codeMetrics.xml">
Listing 2. Software code metrics for ArgoUML software.
</a>

<br>

<a href="https://github.com/rafat66/CodeParser/blob/main/CodeParser/XML/Drawing%20shapes%20software.xml">
Listing 3. The code file of drawing shapes software as an XML file.
</a>

---

## 📚 Research Background

CodeParser builds on research related to the parsing, analysis, and visualization of object-oriented software systems. In particular, the project is related to the work presented in [1], which introduces **ScaMaha**, a tool for parsing, analyzing, and visualizing object-oriented software systems. The extraction of software elements and relationships in CodeParser can provide structured XML data that can be used as input for software visualization and analysis.

The project is also related to requirements traceability research presented in [2], which investigates the recovery and visualization of traceability links between requirements and source code in object-oriented software systems.

### References

[1] R. Al-Msie’deen, “ScaMaha: A Tool for Parsing, Analyzing, and Visualizing Object-Oriented Software Systems,” *International Journal of Computing and Digital Systems*, vol. 17, no. 1, pp. 1–20, 2025.  
[PDF](https://rafat66.github.io/Al-Msie-Deen/img/ScaMaha.pdf)

[2] R. Al-Msie’deen, “Requirements Traceability: Recovering and Visualizing Traceability Links Between Requirements and Source Code of Object-oriented Software Systems,” *International Journal of Computing and Digital Systems*, vol. 14, no. 1, pp. 279–295, 2023.  
[PDF](https://rafat66.github.io/Al-Msie-Deen/img/RT.pdf)
