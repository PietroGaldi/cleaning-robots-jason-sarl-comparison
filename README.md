# cleaning-robots-jason-sarl-comparison

This repository contains the source code used to evaluate Jason and SARL frameworks on the cleaning robots scenario.

The code was developed as part of the Symbolic and Distributed Artificial Intelligence course project (Computer Science master's degree - AI Track, University of Genoa).

The original source code for the cleaning robots implementation in Jason can be found at:
https://github.com/jason-lang/jason/tree/main/examples/cleaning-robots

## Repository structure

```
cleaning-robots-jason-sarl-comparison/
├── jason_implementation/
│   └── cleaning-robots/          # Jason implementation, modified to introduce two further agents
├── sarl_implementation/
│   └── Cleaning_Robots_SARL/
│       ├── src/main/sarl/
│       │   └── cleaning_robots/  # SARL implementation, developed and compiled on SARL IDE
│       ├── .classpath            # SARL IDE configuration file
│       ├── .project              # SARL IDE configuration file
│       └── pom.xml               # Maven configuration file
├── .gitignore
└── README.md
```
