package de.uulm.sp.fmc.as4moco.selection;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AlgorithmSelector {

    private final static String[] commands = new String[]{"python3", "autofolio/scripts/java_bridge.py"};
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;
    private final Process pythonProcess;

    public AlgorithmSelector() {
        executorService = Executors.newSingleThreadExecutor();
        objectMapper = new ObjectMapper();

        try {
            pythonProcess = new ProcessBuilder(commands).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bufferedReader = pythonProcess.inputReader();
        printWriter = new PrintWriter(pythonProcess.outputWriter(), true);
    }

    public Future<Message> askAutofolio(Message message){
        return executorService.submit(() -> {
            try {
                sendQuestion(message);
                return parseAnswer();
            } catch (IOException e) {
                throw new RuntimeException(e); //TODO
            }
        });
    }

    private Message parseAnswer() throws IOException {
        return objectMapper.readValue(bufferedReader.readLine(), Message.class);
    }

    private void sendQuestion(Message message) throws JsonProcessingException {
        printWriter.println(objectMapper.writeValueAsString(message));
    }

    public void closeAutofolio() throws IOException {
        executorService.shutdownNow();
        executorService.close();
        SolverHandler.killProcesses(pythonProcess.toHandle());
        bufferedReader.close();
        printWriter.close();
    }

}
