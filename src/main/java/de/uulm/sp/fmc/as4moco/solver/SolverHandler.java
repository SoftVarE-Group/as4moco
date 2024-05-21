package de.uulm.sp.fmc.as4moco.solver;


import de.uulm.sp.fmc.as4moco.data.HandledSet;
import de.uulm.sp.fmc.as4moco.data.SolverRunInstance;
import de.uulm.sp.fmc.as4moco.selection.messages.SolverBudget;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class SolverHandler {

    public static HandledSet runSolvers(SolverBudget[] runList, File cnf) throws InterruptedException {
        ArrayList<SolverRunInstance> solverResponses = new ArrayList<>(runList.length);
        for (SolverBudget solverBudget : runList){
            if (Thread.currentThread().isInterrupted()) break;
            SolverInterface solver = SolverMap.getSolver(solverBudget.solver());
            System.out.printf("Run solver %s%n", solverBudget.solver());
            Instant before = Instant.now();
            try {
                SolverResponse solverResponse = handleSolver(solver, solverBudget.budget(), cnf);
                Duration duration = Duration.between(before, Instant.now());
                System.out.println("Solver finished: "+solverResponse);
                solverResponses.add(new SolverRunInstance(solverResponse, duration.toMillis() / 1000d));
                if (solverResponse.status().equals(SolverStatusEnum.OK)) break;
            } catch (IOException e) {
                Duration duration = Duration.between(before, Instant.now());
                System.out.println("Solver Error!");
                solverResponses.add(new SolverRunInstance(SolverMap.getName(solver), SolverStatusEnum.ERROR, Optional.empty(), duration.toMillis() / 1000d));
            } catch (InterruptedException e) {
                Duration duration = Duration.between(before, Instant.now());
                System.out.println("Solver interrupted!");
                solverResponses.add(new SolverRunInstance(SolverMap.getName(solver), SolverStatusEnum.ERROR, Optional.empty(), duration.toMillis() / 1000d));
                throw new InterruptedException();
            }
        }
        return new HandledSet(solverResponses);
    }

    private static  SolverResponse handleSolver(SolverInterface solver, int timeout, File cnf) throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>(solver.getParameters(cnf));
        commands.addFirst(Path.of(solver.getFolder().getAbsolutePath(), solver.getExecutable()).toString());
        ProcessBuilder processBuilder = new ProcessBuilder(commands).redirectErrorStream(true);
        processBuilder.directory(solver.getFolder().getAbsoluteFile());
        Map<String, String> newEnv = solver.getEnvironment(timeout);
        String buffer;
        if ( (buffer = processBuilder.environment().get("STAREXEC_MAX_MEM")) != null) newEnv.put("STAREXEC_MAX_MEM", "" + Math.floor(Integer.parseInt(buffer) * 0.45) );
        if ( (buffer = processBuilder.environment().get("STAREXEC_MAX_WRITE")) != null) newEnv.put("STAREXEC_MAX_WRITE", "" + Math.floor(Integer.parseInt(buffer) * 0.45) );
        processBuilder.environment().putAll(newEnv);
        final Process ps = processBuilder.start();

        try {
            if (!ps.waitFor(timeout, TimeUnit.SECONDS)) {
                killProcesses(ps.toHandle());
                return new SolverResponse(SolverMap.getName(solver), SolverStatusEnum.TIMEOUT, Optional.empty());
            }
        } catch (InterruptedException e ){
            killProcesses(ps.toHandle());
            throw new InterruptedException();
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
