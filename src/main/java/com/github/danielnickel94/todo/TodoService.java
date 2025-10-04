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
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text darf nicht leer sein.");
        }
        Todo t = new Todo(nextId++, text.trim());
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

    public List<Todo> listOpen() {
        return todos.stream()
                .filter(t -> !t.isDone())
                .map(t -> new Todo(t.getId(), t.getText(), t.isDone()))
                .toList();
    }

    public List<Todo> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return list(); // nichts zu filtern
        return todos.stream()
                .filter(t -> t.getText() != null && t.getText().toLowerCase().contains(q))
                .map(t -> new Todo(t.getId(), t.getText(), t.isDone()))
                .toList();
    }

    public List<Todo> sortByText(boolean asc) {
        return todos.stream()
                .sorted((a,b) -> asc
                        ? a.getText().compareToIgnoreCase(b.getText())
                        : b.getText().compareToIgnoreCase(a.getText()))
                .map(t -> new Todo(t.getId(), t.getText(), t.isDone()))
                .toList();
    }

    private Todo find(int id) {
        for (Todo t : todos) if (t.getId() == id) return t;
        return null;
    }

    public boolean save(String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            for (Todo todo : todos) {
                writer.write(todo.getId() + ";" + todo.isDone() + ";" + todo.getText());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
            return false;
        }
    }

    public boolean load(String fileName) {
        List<Todo> tmp = new ArrayList<>();
        int tmpNextId = 1;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 3);
                if (parts.length < 3) continue; // oder validieren/loggen
                int id = Integer.parseInt(parts[0].trim());
                boolean done = Boolean.parseBoolean(parts[1].trim());
                String text = parts[2];
                tmp.add(new Todo(id, text, done));
                if (id >= tmpNextId) tmpNextId = id + 1;
            }
            // Erfolgreich: jetzt erst übernehmen
            todos.clear();
            todos.addAll(tmp);
            nextId = tmpNextId;
            return true;
        } catch (IOException | NumberFormatException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
            return false;
        }
    }


}

