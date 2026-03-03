package com.todo.controller;

import com.todo.model.Task;
import com.todo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/tasks")
    public List<Task> getTasks(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return taskService.getTasksByFilter(tag, priority, status, search);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        if (task != null) {
            return ResponseEntity.ok(task);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Map<String, Object> payload) {
        Task task = new Task();
        task.setText((String) payload.getOrDefault("text", ""));
        task.setTag((String) payload.getOrDefault("tag", ""));
        task.setPriority((String) payload.getOrDefault("priority", "medium"));
        task.setDueDate((String) payload.getOrDefault("dueDate", ""));
        task.setRepeat((String) payload.getOrDefault("repeat", ""));
        task.setReminder((String) payload.getOrDefault("reminder", ""));
        task.setNotes((String) payload.getOrDefault("notes", ""));
        return taskService.createTask(task);
    }

    @PutMapping("/tasks/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Task task = taskService.getTaskById(id);
        if (task != null) {
            task.setText((String) payload.getOrDefault("text", task.getText()));
            task.setTag((String) payload.getOrDefault("tag", task.getTag()));
            task.setPriority((String) payload.getOrDefault("priority", task.getPriority()));
            task.setDueDate((String) payload.getOrDefault("dueDate", task.getDueDate()));
            task.setRepeat((String) payload.getOrDefault("repeat", task.getRepeat()));
            task.setReminder((String) payload.getOrDefault("reminder", task.getReminder()));
            task.setNotes((String) payload.getOrDefault("notes", task.getNotes()));
            return taskService.updateTask(id, task);
        }
        return null;
    }

    @PatchMapping("/tasks/{id}/toggle")
    public Task toggleTask(@PathVariable Long id) {
        return taskService.toggleTask(id);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return taskService.getStats();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
