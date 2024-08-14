![NetBeans](https://img.shields.io/badge/NetBeans-22-blue.svg)
![Java](https://img.shields.io/badge/Java-22-brightgreen.svg)

## Premises

This is a **beta version** of **FidoCadJ_NetBeans** intended for testing new features and gathering user feedback. While the core functionality is stable, some features are still under development, and users may encounter bugs or incomplete functionalities. We encourage testers to report any issues they encounter to help improve the final release.

Your feedback is invaluable and will be instrumental in refining the features and ensuring the stability of the software. Thank you for participating in this testing phase!

## Installation

| Version              | Platform         | Description                                                                                                                                               | Download Link                                                                                                                                                              |
|----------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![Windows](https://img.shields.io/badge/Windows-executable-blue)          | Windows           | This version includes an executable for easy installation. Requires Java to be installed on your machine.                                                 | [Download Windows Version](https://github.com/manufino/FidoCadJ_NetBeans/releases/download/TESTING/FidoCadJ_NB_WINDOWS.zip)                                               |
| ![Windows Portable](https://img.shields.io/badge/Windows--Portable-JAR-orange) | Windows Portable | This version includes the JAR file and the entire Java system required to run the application. Just launch `FidoCAD RUN.bat`.                              | [Download Portable Version](https://github.com/manufino/FidoCadJ_NetBeans/releases/download/TESTING/PORTABLE_WINDOWS.zip)                                                  |
| ![JAR](https://img.shields.io/badge/Platform-JAR--only-yellow)            | JAR Only          | This version contains only the JAR file for use on other platforms. Requires Java to be installed on your machine.                                         | [Download JAR Version](https://github.com/manufino/FidoCadJ_NetBeans/releases/download/TESTING/FidoCadJ_NB_JAR.zip)                                                       |


### Prerequisites

- **Java Development Kit (JDK) 22**: Ensure you have the appropriate JDK installed for development.
- **Java Runtime Environment (JRE) 22**: Required for running the compiled application if using the JAR version.
- **NetBeans IDE 22**: Download and install [NetBeans IDE 22](https://netbeans.apache.org/download/).

## Overview

**FidoCadJ_NetBeans** is a fork of the popular [FidoCadJ](https://github.com/DarwinNE/FidoCadJ) project, tailored for seamless development with NetBeans IDE version 22. This project includes various enhancements and optimizations aimed at improving the user experience, ease of compilation, and adding new features to the original FidoCadJ.

## Features

- **NetBeans IDE Integration:** The project is fully set up for easy compilation and development within NetBeans 22.
- **Improved Build Process:** Simplified build scripts that leverage NetBeans' capabilities for faster development cycles.
- **New Features:**
  - **Enhanced Object Selection:** Now, you only need to select a small part of an object to highlight it.
  - **Distinct Selection Mode (AutoCAD-Style):**
    - **Right-to-Left Selection:** All objects intersecting with the selection rectangle are selected.
    - **Left-to-Right Selection:** Only objects fully contained within the selection rectangle are selected.
  - **Layer Management:** 
    - **Toggle Layers:** Activate or deactivate layers directly from the combobox on the toolbar.
    - **Layer Color Customization:** Change layer colors directly from the combobox on the toolbar.
  - **Enhanced Menu Interface:** Added icons to all menu options for better visual navigation.











