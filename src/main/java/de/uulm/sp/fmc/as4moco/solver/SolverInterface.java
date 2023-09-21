package de.uulm.sp.fmc.as4moco.solver;

import java.util.List;

public interface SolverInterface {

    String getName();

    String getExecutableName();

    List<String> getParameters();

    SolverResponse parseOutput(String combinedOutput, int statusCode);


}
