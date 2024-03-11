# as4moco
Algorithm selection for model counting

as4moco uses algorithm selection and automated algorithm configuration decrease its solving time for #SAT queries. It is based on [AutoFolio](https://github.com/automl/AutoFolio), an algorithm selection framework. 

# Requirements
- Java 21
- Python 3 (tested for Python 3.10)
- Ubuntu 22.04 (other version may require a recompilation of the used solver binaries)

## How to install
1.  Install [modified AutoFolio](https://github.com/RSD6170/AutoFolio/)
    1. Create a new virtual environment: ``python3 -m venv <Name of Virtual Environment>``
    2. Clone modified AutoFolio: ``git clone https://github.com/RSD6170/AutoFolio/``
    3. Install the modified AutoFolio package: ``<path to venv>/bin/python3 -m pip install <path to cloned repository>``
2. Clone this repository: ``git clone https://github.com/SoftVarE-Group/as4moco``
3. Modify [AlgorithmSelector.java](src/main/java/de/uulm/sp/fmc/as4moco/selection/AlgorithmSelector.java) at marked todo with path to python venv and AutoFolio repository: ``    private final static String[] commands = new String[]{"<Path to Python Venv>/bin/python3", "-u", "<Path to AutoFolio repo>/scripts/java_bridge.py"};``
## How to run
Run `Gradle::run` with your parameters

Example for Ubuntu: ``./gradlew run −−args="−modelFile </path/to/af_model.pkl> −cnfFile </path/to/cnf.dimacs>"``

New model files can be created, refer to the [example in the AutoFolio repository](https://github.com/RSD6170/AutoFolio/blob/master/examples/MCC2022_T1_randomSplits_re/Training_1/example.py).