package de.uulm.sp.fmc.as4moco.solver;


import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;
import de.uulm.sp.fmc.as4moco.solver.solvers.TestSolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class SolverHandler {

    private final static String pathToSolver = "workingSolvers/";
    private final static Map<String, SolverInterface> solvers = Map.ofEntries(
            Map.entry("test", new TestSolver())//todo add solvers
    );

    public static List<SolverResponse> runSolvers(SolverBudget[] runList) { //TODO accept list of solvers, handle ending --> give function reference for callback
        ArrayList<SolverResponse> solverResponses = new ArrayList<>(runList.length);
        for (SolverBudget solverBudget : runList){
            if (Thread.currentThread().isInterrupted()) break;
            SolverInterface solver = getSolver(solverBudget.solver());
            try {
                SolverResponse solverResponse = handleSolver(solver, solverBudget.budget());
                solverResponses.add(solverResponse);
                if (solverResponse.status().equals(SolverStatusEnum.OK)) break;
            } catch (IOException e) {
                solverResponses.add(new SolverResponse(solver, SolverStatusEnum.ERROR, Optional.empty()));
            }
        }
        return solverResponses;
    }

    private static  SolverResponse handleSolver(SolverInterface solver, int timeout) throws IOException {
        List<String> commands = new ArrayList<>(solver.getParameters());
        commands.add(0, pathToSolver + solver.getExecutableName());
        final Process ps = new ProcessBuilder(commands).redirectErrorStream(true).start();

        try {
            if (!ps.waitFor(timeout, TimeUnit.SECONDS)) {
                killProcesses(ps.toHandle());
                return new SolverResponse(solver, SolverStatusEnum.TIMEOUT, Optional.empty());
            }
        } catch (InterruptedException e ){
            killProcesses(ps.toHandle());
            return new SolverResponse(solver, SolverStatusEnum.ERROR, Optional.empty());
        }

        StringBuilder val = new StringBuilder();
        String line;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()))) {
            while (!Thread.currentThread().isInterrupted() && (line = in.readLine()) != null) {
                val.append(line).append("\n");
            }
        }
        return solver.parseOutput(val.toString(), ps.exitValue());
    }


    public static SolverInterface getSolver(String name){
        return solvers.get(name);
    }

    public static void killProcesses(ProcessHandle ps)  {
        ps.descendants().forEach(SolverHandler::killProcesses);
        ps.destroy();
    }
    
}
