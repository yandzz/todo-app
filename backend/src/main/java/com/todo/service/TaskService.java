package com.todo.service;

import com.todo.model.Task;
import com.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByFilter(String tag, String priority, String status, String search) {
        List<Task> tasks = taskRepository.findAll();
        
        if (tag != null && !tag.isEmpty()) {
            tasks = tasks.stream().filter(t -> tag.equals(t.getTag())).toList();
        }
        if (priority != null && !priority.isEmpty()) {
            tasks = tasks.stream().filter(t -> priority.equals(t.getPriority())).toList();
        }
        if (status != null) {
            if ("completed".equals(status)) {
                tasks = tasks.stream().filter(t -> t.getCompleted()).toList();
            } else if ("pending".equals(status)) {
                tasks = tasks.stream().filter(t -> !t.getCompleted()).toList();
            }
        }
        if (search != null && !search.isEmpty()) {
            tasks = tasks.stream().filter(t -> t.getText().contains(search)).toList();
        }
        
        return tasks;
    }

    public Task createTask(Task task) {
        task.setCompleted(false);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        Task saved = taskRepository.save(task);
        
        // Create next repeat task if needed
        if (task.getRepeat() != null && !task.getRepeat().isEmpty() && task.getDueDate() != null) {
            String nextDate = getNextDate(task.getDueDate(), task.getRepeat());
            if (nextDate != null) {
                Task nextTask = new Task();
                nextTask.setText(task.getText());
                nextTask.setTag(task.getTag());
                nextTask.setPriority(task.getPriority());
                nextTask.setDueDate(nextDate);
                nextTask.setRepeat(task.getRepeat());
                nextTask.setReminder(task.getReminder());
                nextTask.setCompleted(false);
                taskRepository.save(nextTask);
            }
        }
        
        return saved;
    }

    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setText(taskDetails.getText());
        task.setCompleted(taskDetails.getCompleted());
        task.setTag(taskDetails.getTag());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        task.setRepeat(taskDetails.getRepeat());
        task.setReminder(taskDetails.getReminder());
        task.setNotes(taskDetails.getNotes());
        task.setUpdatedAt(LocalDateTime.now());
        
        return taskRepository.save(task);
    }

    public Task toggleTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setCompleted(!task.getCompleted());
        task.setCompletedAt(task.getCompleted() ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());
        
        Task saved = taskRepository.save(task);
        
        // Create next repeat task if completed and has repeat
        if (task.getCompleted() && task.getRepeat() != null && !task.getRepeat().isEmpty() && task.getDueDate() != null) {
            String nextDate = getNextDate(task.getDueDate(), task.getRepeat());
            if (nextDate != null) {
                Task nextTask = new Task();
                nextTask.setText(task.getText());
                nextTask.setTag(task.getTag());
                nextTask.setPriority(task.getPriority());
                nextTask.setDueDate(nextDate);
                nextTask.setRepeat(task.getRepeat());
                nextTask.setReminder(task.getReminder());
                nextTask.setCompleted(false);
                taskRepository.save(nextTask);
            }
        }
        
        return saved;
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Map<String, Object> getStats() {
        List<Task> allTasks = taskRepository.findAll();
        long completed = allTasks.stream().filter(Task::getCompleted).count();
        long pending = allTasks.size() - completed;
        
        String today = java.time.LocalDate.now().toString();
        long overdue = allTasks.stream()
            .filter(t -> !t.getCompleted() && t.getDueDate() != null && t.getDueDate().compareTo(today) < 0)
            .count();
        long dueToday = allTasks.stream()
            .filter(t -> !t.getCompleted() && today.equals(t.getDueDate()))
            .count();
            
        return Map.of(
            "total", (long) allTasks.size(),
            "completed", completed,
            "pending", pending,
            "overdue", overdue,
            "dueToday", dueToday
        );
    }

    private String getNextDate(String currentDate, String repeat) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime date = LocalDateTime.parse(currentDate + "T00:00:00");
            
            switch (repeat) {
                case "daily":
                    return date.plusDays(1).format(formatter);
                case "weekly":
                    return date.plusWeeks(1).format(formatter);
                case "monthly":
                    return date.plusMonths(1).format(formatter);
                case "yearly":
                    return date.plusYears(1).format(formatter);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }
}
