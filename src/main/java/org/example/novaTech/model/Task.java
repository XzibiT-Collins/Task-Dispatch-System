package org.example.novaTech.model;

import lombok.*;
import org.example.novaTech.utils.TaskStatusEnum;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Task implements Comparable<Task> {
    private UUID id;
    private String name;
    private int priority;
    private int retryCount;
    private Instant createdTimestamp;
    private String payload;
    private TaskStatusEnum status;
    private boolean processed=false;

    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority, this.priority);
    }
}