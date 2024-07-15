package de.uulm.sp.fmc.as4moco.data;

import de.uulm.sp.fmc.as4moco.solver.SolverResponse;
import de.uulm.sp.fmc.as4moco.solver.SolverStatusEnum;
import de.uulm.sp.fmc.as4moco.solver.SolverType;

import java.math.BigDecimal;
import java.util.Optional;

public record SolverRunInstance(Optional<String> solver, SolverStatusEnum status, Optional<BigDecimal> solution, double runtime, SolverType solverType) {

    public SolverRunInstance(SolverResponse solverResponse, double runtime) {
        this(solverResponse.solver(), solverResponse.status(), solverResponse.solution(), runtime, solverResponse.solverType());
    }
}
