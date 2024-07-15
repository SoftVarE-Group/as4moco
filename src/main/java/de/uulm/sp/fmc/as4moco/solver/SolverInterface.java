package de.uulm.sp.fmc.as4moco.solver;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public interface SolverInterface {

    File getFolder();

    String getExecutable();

    default List<String> getParameters(File cnf){
        return Collections.singletonList(cnf.getAbsolutePath());
    }

    default SolverResponse parseOutput(String combinedOutput, int statusCode) {
        if (statusCode == 0 || statusCode == 42){
            if (combinedOutput.contains("TIME LIMIT OF") && combinedOutput.contains("EXCEEDED"))
                return new SolverResponse(SolverMap.getName(this), SolverStatusEnum.TIMEOUT, Optional.empty(), SolverType.ERR);
            Optional<BigDecimal> count = combinedOutput.lines().filter(e -> e.startsWith("c s exact"))
                    .map(SolverInterface::mapMultiStringToDouble)
                    .filter(Optional::isPresent).map(Optional::get).findAny(); //TODO
            if (count.isPresent()) return new SolverResponse(SolverMap.getName(this), SolverStatusEnum.OK, count, SolverType.exact);
            else {
                count = combinedOutput.lines().filter(e -> e.startsWith("c s approx"))
                        .map(SolverInterface::mapMultiStringToDouble)
                        .filter(Optional::isPresent).map(Optional::get).findAny();
                return new SolverResponse(SolverMap.getName(this), SolverStatusEnum.OK, count, SolverType.approx);
            }
        }
        System.err.println(combinedOutput);
        return new SolverResponse(SolverMap.getName(this), SolverStatusEnum.ERROR, Optional.empty(), SolverType.ERR);
    }

    default Map<String, String> getEnvironment(int timeout){
        return Map.ofEntries(
                Map.entry("STAREXEC_WALLCLOCK_LIMIT", String.valueOf(timeout)),
                Map.entry("STAREXEC_CPU_LIMIT", String.valueOf(timeout)),
                Map.entry("STAREXEC_MAX_MEM", String.valueOf(7000)),
                Map.entry("STAREXEC_MAX_WRITE", String.valueOf(8000))
        );
    }

    private static Optional<BigDecimal> mapMultiStringToDouble(String string){
        return Arrays.stream(string.split(" ")).map(s -> {
            try {
                return BigDecimalMath.toBigDecimal(s);
            }catch (NumberFormatException e){
                return null;
            }
        }).filter(Objects::nonNull).findAny();
    }



}
