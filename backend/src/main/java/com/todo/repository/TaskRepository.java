package com.todo.repository;

import com.todo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCompleted(Boolean completed);
    List<Task> findByTag(String tag);
    List<Task> findByPriority(String priority);
    List<Task> findByTagAndCompleted(String tag, Boolean completed);
}
