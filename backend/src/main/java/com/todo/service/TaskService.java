package com.todo.service;

import com.todo.model.Task;
import com.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByFilter(String tag, String priority, String status, String search, String listName) {
        List<Task> tasks = taskRepository.findAll();
        
        if (tag != null && !tag.isEmpty()) {
            tasks = tasks.stream().filter(t -> tag.equals(t.getTag())).collect(Collectors.toList());
        }
        if (priority != null && !priority.isEmpty()) {
            tasks = tasks.stream().filter(t -> priority.equals(t.getPriority())).collect(Collectors.toList());
        }
        if (listName != null && !listName.isEmpty()) {
            tasks = tasks.stream().filter(t -> listName.equals(t.getListName())).collect(Collectors.toList());
        }
        if (status != null) {
            if ("completed".equals(status)) {
                tasks = tasks.stream().filter(Task::getCompleted).collect(Collectors.toList());
            } else if ("pending".equals(status)) {
                tasks = tasks.stream().filter(t -> !t.getCompleted()).collect(Collectors.toList());
            } else if ("overdue".equals(status)) {
                String today = LocalDateTime.now().toLocalDate().toString();
                tasks = tasks.stream().filter(t -> !t.getCompleted() && t.getDueDate() != null && t.getDueDate().compareTo(today) < 0).collect(Collectors.toList());
            }
        }
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            tasks = tasks.stream().filter(t -> 
                (t.getText() != null && t.getText().toLowerCase().contains(s)) ||
                (t.getNotes() != null && t.getNotes().toLowerCase().contains(s)) ||
                (t.getDescription() != null && t.getDescription().toLowerCase().contains(s))
            ).collect(Collectors.toList());
        }
        
        // Sort by orderIndex, then by priority
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                int cmp = Integer.compare(t1.getOrderIndex(), t2.getOrderIndex());
                if (cmp != 0) return cmp;
                String p1 = t1.getPriority();
                String p2 = t2.getPriority();
                int v1 = "high".equals(p1) ? 1 : "medium".equals(p1) ? 2 : "low".equals(p1) ? 3 : 4;
                int v2 = "high".equals(p2) ? 1 : "medium".equals(p2) ? 2 : "low".equals(p2) ? 3 : 4;
                return v1 - v2;
            }
        });
        
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
                Task nextTask = cloneTaskForRepeat(task, nextDate);
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
        task.setDescription(taskDetails.getDescription());
        task.setListName(taskDetails.getListName());
        task.setOrderIndex(taskDetails.getOrderIndex());
        task.setColor(taskDetails.getColor());
        task.setLocation(taskDetails.getLocation());
        task.setUrl(taskDetails.getUrl());
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
                Task nextTask = cloneTaskForRepeat(task, nextDate);
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
        
        String today = LocalDateTime.now().toLocalDate().toString();
        long overdue = allTasks.stream()
            .filter(t -> !t.getCompleted() && t.getDueDate() != null && t.getDueDate().compareTo(today) < 0)
            .count();
        long dueToday = allTasks.stream()
            .filter(t -> !t.getCompleted() && today.equals(t.getDueDate()))
            .count();
        
        // Count by list
        Map<String, Long> listCounts = allTasks.stream()
            .collect(Collectors.groupingBy(t -> t.getListName() != null ? t.getListName() : "默认", Collectors.counting()));
        
        // Count by tag
        Map<String, Long> tagCounts = allTasks.stream()
            .filter(t -> t.getTag() != null && !t.getTag().isEmpty())
            .collect(Collectors.groupingBy(Task::getTag, Collectors.counting()));
        
        return Map.of(
            "total", (long) allTasks.size(),
            "completed", completed,
            "pending", pending,
            "overdue", overdue,
            "dueToday", dueToday,
            "listCounts", listCounts,
            "tagCounts", tagCounts
        );
    }
    
    public List<String> getLists() {
        List<Task> allTasks = taskRepository.findAll();
        Set<String> lists = new HashSet<>();
        lists.add("默认");
        for (Task t : allTasks) {
            if (t.getListName() != null && !t.getListName().isEmpty()) {
                lists.add(t.getListName());
            }
        }
        return new ArrayList<>(lists);
    }
    
    public List<Task> reorderTasks(List<Long> ids) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Optional<Task> opt = taskRepository.findById(ids.get(i));
            if (opt.isPresent()) {
                Task task = opt.get();
                task.setOrderIndex(i);
                task.setUpdatedAt(LocalDateTime.now());
                tasks.add(taskRepository.save(task));
            }
        }
        return tasks;
    }

    private Task cloneTaskForRepeat(Task original, String nextDate) {
        Task next = new Task();
        next.setText(original.getText());
        next.setTag(original.getTag());
        next.setPriority(original.getPriority());
        next.setDueDate(nextDate);
        next.setRepeat(original.getRepeat());
        next.setReminder(original.getReminder());
        next.setListName(original.getListName());
        next.setDescription(original.getDescription());
        next.setColor(original.getColor());
        next.setCompleted(false);
        next.setCreatedAt(LocalDateTime.now());
        next.setUpdatedAt(LocalDateTime.now());
        return next;
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
