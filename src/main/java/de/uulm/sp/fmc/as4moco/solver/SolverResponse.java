package de.uulm.sp.fmc.as4moco.solver;

import java.util.Optional;

public record SolverResponse(SolverStatusEnum status, Optional<Long> solution) {
}
