package org.example.novaTech.jsonExport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.novaTech.utils.TaskStatusEnum;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ExportTaskStatus implements Runnable{
    private final Logger logger = Logger.getLogger(ExportTaskStatus.class.getName());
    private final ConcurrentHashMap<UUID, TaskStatusEnum> taskStatusMap;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExportTaskStatus(ConcurrentHashMap<UUID, TaskStatusEnum> taskStatusMap) {
        this.taskStatusMap = taskStatusMap;
    }

    @Override
    public void run() {
        File outputFile = new File("jsonExports/ExportTask-"+ Instant.now().toString().replace(":","-") +".json");


        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile,taskStatusMap);
            logger.info("Task Status exported to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.severe("Error exporting task status to file: " + e.getMessage());
        }

    }
}
