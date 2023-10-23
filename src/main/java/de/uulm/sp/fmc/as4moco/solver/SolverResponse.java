package de.uulm.sp.fmc.as4moco.solver;

import java.util.Optional;

public record SolverResponse(SolverInterface solver, SolverStatusEnum status, Optional<Double> solution) {
}
