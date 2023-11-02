package de.uulm.sp.fmc.as4moco.solver;

import java.io.File;
import java.util.*;

public interface SolverInterface {

    File getFolder();

    String getExecutable();

    default List<String> getParameters(File cnf){
        return Collections.singletonList(cnf.getAbsolutePath());
    }

    default SolverResponse parseOutput(String combinedOutput, int statusCode) {
        if (statusCode == 0){
            Optional<Double> count = combinedOutput.lines().filter(e -> e.startsWith("c s exact"))
                    .map(SolverInterface::mapMultiStringToDouble)
                    .filter(Optional::isPresent).map(Optional::get).findAny(); //TODO
            if (count.isPresent()) return new SolverResponse(this, SolverStatusEnum.OK, count);
        }
        return new SolverResponse(this, SolverStatusEnum.ERROR, Optional.empty());
    }

    default Map<String, String> getEnvironment(int timeout){
        return Map.ofEntries(
                Map.entry("STAREXEC_WALLCLOCK_LIMIT", String.valueOf(timeout)),
                Map.entry("STAREXEC_CPU_LIMIT", String.valueOf(timeout)),
                Map.entry("STAREXEC_MAX_MEM", String.valueOf(4000)),
                Map.entry("STAREXEC_MAX_WRITE", String.valueOf(20))
        );
    }

    private static Optional<Double> mapMultiStringToDouble(String string){
        return Arrays.stream(string.split(" ")).map(s -> {
            try {
                return Double.valueOf(s);
            }catch (NumberFormatException e){
                return null;
            }
        }).filter(Objects::nonNull).findAny();
    }

}
