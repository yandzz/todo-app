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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String list) {
        return taskService.getTasksByFilter(tag, priority, status, search, list);
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
        task.setDescription((String) payload.getOrDefault("description", ""));
        task.setListName((String) payload.getOrDefault("listName", "默认"));
        task.setColor((String) payload.getOrDefault("color", ""));
        task.setLocation((String) payload.getOrDefault("location", ""));
        task.setUrl((String) payload.getOrDefault("url", ""));
        
        Object orderObj = payload.get("orderIndex");
        if (orderObj != null) {
            task.setOrderIndex(((Number) orderObj).intValue());
        }
        
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
            task.setDescription((String) payload.getOrDefault("description", task.getDescription()));
            task.setListName((String) payload.getOrDefault("listName", task.getListName()));
            task.setColor((String) payload.getOrDefault("color", task.getColor()));
            task.setLocation((String) payload.getOrDefault("location", task.getLocation()));
            task.setUrl((String) payload.getOrDefault("url", task.getUrl()));
            
            Object orderObj = payload.get("orderIndex");
            if (orderObj != null) {
                task.setOrderIndex(((Number) orderObj).intValue());
            }
            
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

    @PostMapping("/tasks/batch")
    public Map<String, Object> batchOperation(@RequestBody Map<String, Object> payload) {
        String action = (String) payload.get("action");
        List<Number> ids = (List<Number>) payload.get("ids");
        
        int affected = 0;
        for (Number id : ids) {
            try {
                switch (action) {
                    case "complete":
                        Task t = taskService.getTaskById(id.longValue());
                        if (t != null) {
                            t.setCompleted(true);
                            taskService.updateTask(id.longValue(), t);
                            affected++;
                        }
                        break;
                    case "delete":
                        taskService.deleteTask(id.longValue());
                        affected++;
                        break;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return Map.of("success", true, "affected", affected);
    }

    @PostMapping("/tasks/reorder")
    public List<Task> reorderTasks(@RequestBody Map<String, Object> payload) {
        List<Number> ids = (List<Number>) payload.get("ids");
        List<Long> longIds = ids.stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        return taskService.reorderTasks(longIds);
    }

    @GetMapping("/lists")
    public List<String> getLists() {
        return taskService.getLists();
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
