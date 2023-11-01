package de.uulm.sp.fmc.as4moco;

import de.uulm.sp.fmc.as4moco.solver.SolverResponse;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public record SolvingRun(File cnfFile, Instant start, Instant end, double duration, SolverResponse solverResponse) {
}
