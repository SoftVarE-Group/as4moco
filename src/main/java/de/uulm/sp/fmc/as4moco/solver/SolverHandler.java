package de.uulm.sp.fmc.as4moco.solver;


import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class SolverHandler {

    public static List<SolverResponse> runSolvers(SolverBudget[] runList, File cnf) { //TODO accept list of solvers, handle ending --> give function reference for callback
        ArrayList<SolverResponse> solverResponses = new ArrayList<>(runList.length);
        for (SolverBudget solverBudget : runList){
            if (Thread.currentThread().isInterrupted()) break;
            SolverInterface solver = SolverMap.getSolver(solverBudget.solver());
            try {
                SolverResponse solverResponse = handleSolver(solver, solverBudget.budget(), cnf);
                solverResponses.add(solverResponse);
                if (solverResponse.status().equals(SolverStatusEnum.OK)) break;
            } catch (IOException e) {
                solverResponses.add(new SolverResponse(solver, SolverStatusEnum.ERROR, Optional.empty()));
            }
        }
        return solverResponses;
    }

    private static  SolverResponse handleSolver(SolverInterface solver, int timeout, File cnf) throws IOException {
        List<String> commands = new ArrayList<>(solver.getParameters(cnf));
        commands.add(0, solver.getExecutable());
        ProcessBuilder processBuilder = new ProcessBuilder(commands).redirectErrorStream(true);
        processBuilder.directory(solver.getFolder());
        processBuilder.environment().putAll(solver.getEnvironment(timeout));
        final Process ps = processBuilder.start();

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

    public static void killProcesses(ProcessHandle ps)  {
        ps.descendants().forEach(SolverHandler::killProcesses);
        ps.destroy();
    }
    
}
