package de.uulm.sp.fmc.as4moco.solver.solvers;

import de.uulm.sp.fmc.as4moco.solver.SolverInterface;
import de.uulm.sp.fmc.as4moco.solver.SolverResponse;

import java.util.List;

public class TestSolver implements SolverInterface {

    @Override
    public String getExecutableName() {
        return null;
    }

    @Override
    public List<String> getParameters() {
        return null;
    }

    @Override
    public SolverResponse parseOutput(String combinedOutput, int statusCode) {
        return null;
    }
}
