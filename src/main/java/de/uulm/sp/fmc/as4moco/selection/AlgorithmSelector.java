package de.uulm.sp.fmc.as4moco.selection;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uulm.sp.fmc.as4moco.selection.messages.Message;
import de.uulm.sp.fmc.as4moco.solver.SolverHandler;

import java.io.*;
import java.util.concurrent.*;


public class AlgorithmSelector {

    private final static String[] commands = new String[]{"/home/ubuntu//as4moco/as4mocoPy/bin/python3", "-u", "/home/ubuntu/as4moco/AutoFolio/scripts/java_bridge.py"}; //todo fix
    private final ExecutorService executorService;
    private final BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<>(10);

    private final ObjectMapper objectMapper;
    private final BufferedReader bufferedReader;
    private final BufferedWriter printWriter;
    private final Process pythonProcess;

    public AlgorithmSelector() {
        executorService = Executors.newCachedThreadPool();
        objectMapper = new ObjectMapper();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands).redirectErrorStream(true);
            processBuilder.environment().put("PYTHONUNBUFFERED","TRUE");
            pythonProcess = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bufferedReader = pythonProcess.inputReader();
        printWriter = pythonProcess.outputWriter();
        executorService.submit(this::parseAnswer);
    }

    public Future<Message> askAutofolio(Message message){
        return executorService.submit(() -> {
            try {
                sendQuestion(message);
                return blockingQueue.take();
            } catch (IOException e) {
                throw new RuntimeException(e); //TODO
            }
        });
    }

    private void parseAnswer() {
        String line;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                line = bufferedReader.readLine();
                if (line == null) break;
                blockingQueue.offer(objectMapper.readValue(line, Message.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendQuestion(Message message) throws IOException {
        printWriter.write(objectMapper.writeValueAsString(message));
        printWriter.newLine();
        printWriter.flush();
    }

    public void closeAutofolio() throws IOException {
        executorService.shutdownNow();
        SolverHandler.killProcesses(pythonProcess.toHandle());
        bufferedReader.close();
        printWriter.close();
    }

}
