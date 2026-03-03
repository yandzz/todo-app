package com.todo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskRequest {
    @JsonProperty("text")
    private String text = "";
    @JsonProperty("tag")
    private String tag = "";
    @JsonProperty("priority")
    private String priority = "medium";
    @JsonProperty("dueDate")
    private String dueDate = "";
    @JsonProperty("repeat")
    private String repeat = "";
    @JsonProperty("reminder")
    private String reminder = "";
    @JsonProperty("notes")
    private String notes = "";
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
