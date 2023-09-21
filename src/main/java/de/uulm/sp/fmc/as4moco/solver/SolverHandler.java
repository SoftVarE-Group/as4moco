package de.uulm.sp.fmc.as4moco.solver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SolverHandler {

    private final static String pathToSolver = "workingSolvers/";
    private final ExecutorService executorService;


    public SolverHandler(){
        executorService = Executors.newSingleThreadExecutor();
    }

    public Future<SolverResponse> runSolver(SolverInterface solverInterface, int timeout){
        return executorService.submit(() -> {
           try {
               return handleSolver(solverInterface, timeout);
           } catch (IOException | InterruptedException e) {
               return new SolverResponse(SolverStatusEnum.ERROR, Optional.empty());
           }
        });
    }

    private SolverResponse handleSolver(SolverInterface solver, int timeout) throws IOException, InterruptedException {
        List<String> commands = new ArrayList<>(solver.getParameters());
        commands.add(0, pathToSolver+solver.getExecutableName());
        final Process ps = new ProcessBuilder(commands).redirectErrorStream(true).start();

        if (!ps.waitFor(timeout, TimeUnit.SECONDS)) {
            killProcesses(ps.toHandle());
            return new SolverResponse(SolverStatusEnum.TIMEOUT, Optional.empty());
        }
        StringBuilder val = new StringBuilder();
        String line;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()))) {
            while (! Thread.currentThread().isInterrupted() && (line = in.readLine()) != null) {
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
