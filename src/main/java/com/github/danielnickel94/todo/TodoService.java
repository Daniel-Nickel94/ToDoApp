package com.github.danielnickel94.todo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TodoService {
    private final List<Todo> todos = new ArrayList<>();
    private int nextId = 1;

    public int add(String text) {
        Todo t = new Todo(nextId++, text);
        todos.add(t);
        return t.getId();
    }

    public boolean updateText(int id, String newText) {
        Todo t = find(id);
        if (t == null) return false;
        t.setText(newText);
        return true;
    }

    public boolean remove(int id) {
        Todo t = find(id);
        return t != null && todos.remove(t);
    }

    public boolean markDone(int id) {
        Todo t = find(id);
        if (t == null) return false;
        t.setDone(true);
        return true;
    }

    public List<Todo> list() {
        return List.copyOf(todos); // read-only copy
    }

    private Todo find(int id) {
        for (Todo t : todos) if (t.getId() == id) return t;
        return null;
    }

    public void save(String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            for (Todo todo : todos) {
                writer.write(todo.getId() + ";" + todo.isDone() + ";" + todo.getText());
                writer.newLine();
            }
            System.out.println("Todos gespeichert in " + fileName);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
        }
    }

    public void load(String fileName) {
        todos.clear();
        nextId = 1;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 3);
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    boolean done = Boolean.parseBoolean(parts[1]);
                    String text = parts[2];
                    todos.add(new Todo(id, text, done));
                    if (id >= nextId) {
                        nextId = id + 1;
                    }
                }
            }
            System.out.println("Todos geladen aus " + fileName);
        } catch (IOException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }
    }

}

