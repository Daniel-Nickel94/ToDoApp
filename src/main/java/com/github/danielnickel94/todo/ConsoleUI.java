package com.github.danielnickel94.todo;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private static final String DEFAULT_CSV = "todos.csv";

    private final TodoService service;

    public ConsoleUI(TodoService service) {
        this.service = service;
    }

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readIntInRange(sc, "Auswahl: ", 1, 8);
                System.out.println();

                switch (choice) {
                    case 1 -> handleList();
                    case 2 -> handleAdd(sc);
                    case 3 -> handleUpdate(sc);
                    case 4 -> handleRemove(sc);
                    case 5 -> handleMarkDone(sc);
                    case 6 -> handleSave(sc);
                    case 7 -> handleLoad(sc);
                    case 8 -> {
                        System.out.println("Beenden. Bis bald!");
                        running = false;
                    }
                }
                System.out.println();
            }
        }
    }

    // Menüaktionen

    private void handleList() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Anzeigen:");
        System.out.println("1) Alle");
        System.out.println("2) Nur offene");
        System.out.println("3) Suchen (enthält...)");
        System.out.println("4) Alphabetisch (A→Z)");
        System.out.println("5) Alphabetisch (Z→A)");
        int sub = readIntInRange(sc, "Auswahl: ", 1, 5);

        List<Todo> toShow = switch (sub) {
            case 1 -> service.list();
            case 2 -> service.listOpen();
            case 3 -> {
                String q = readNonEmptyLine(sc, "Suchtext: ");
                yield service.search(q);
            }
            case 4 -> service.sortByText(true);
            case 5 -> service.sortByText(false);
            default -> service.list();
        };

        if (toShow.isEmpty()) {
            System.out.println("keine Erledigungen vorhanden.");
            return;
        }
        System.out.println("Erledigungen:");
        toShow.forEach(t -> System.out.println(" - " + t));
    }

    private void handleAdd(Scanner sc) {
        String text = readNonEmptyLine(sc, "Neue Erledigung: ");
        try {
            int id = service.add(text);
            System.out.println("Hinzugefügt mit ID " + id + ".");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void handleUpdate(Scanner sc) {
        int id = readExistingTodoId(sc);
        if (id == -1) return; // keine Todos vorhanden
        String neu = readNonEmptyLine(sc, "Neuer Text: ");
        boolean ok = service.updateText(id, neu);
        System.out.println(ok ? "Aktualisiert." : "ID nicht gefunden.");
    }

    private void handleRemove(Scanner sc) {
        int id = readExistingTodoId(sc);
        if (id == -1) return;
        boolean ok = service.remove(id);
        System.out.println(ok ? "Gelöscht." : "ID nicht gefunden.");
    }

    private void handleMarkDone(Scanner sc) {
        int id = readExistingTodoId(sc);
        if (id == -1) return;
        boolean ok = service.markDone(id);
        System.out.println(ok ? "Als erledigt markiert." : "ID nicht gefunden.");
    }

    private void handleSave(Scanner sc) {
        String file = readLineAllowEmpty(sc, "Dateiname zum Speichern (Enter für \"" + DEFAULT_CSV + "\"): ");
        String target = file.isBlank() ? DEFAULT_CSV : file.trim();
        boolean ok = service.save(target);
        if (ok) {
            System.out.println("Gespeichert nach: " + target);
        } else {
            System.out.println("Speichern fehlgeschlagen.");
        }
    }


    private void handleLoad(Scanner sc) {
        String file = readLineAllowEmpty(sc, "Dateiname zum Laden (Enter für \"" + DEFAULT_CSV + "\"): ");
        String source = file.isBlank() ? DEFAULT_CSV : file.trim();
        boolean ok = service.load(source);
        if (ok) {
            System.out.println("Geladen von: " + source);
            handleList();
        } else {
            System.out.println("Laden abgebrochen. Vorheriger Stand bleibt erhalten.");
        }
    }


    /* ===================== Eingabe-Helfer ===================== */

    /** Liest eine nicht-leere Zeile (trimmt) und fragt bei Leer-/Nur-Whitespace erneut. */
    private static String readNonEmptyLine(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("Eingabe darf nicht leer sein. Bitte erneut versuchen.");
        }
    }

    /** Liest eine Zeile, darf leer sein (z. B. für optionalen Dateinamen). */
    private static String readLineAllowEmpty(Scanner sc, String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine();
        return line == null ? "" : line;
    }

    /** Liest eine Ganzzahl robust; bei Fehlern wird erneut gefragt. */
    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine();
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException e) {
                System.out.println("Bitte eine gültige Zahl eingeben.");
            }
        }
    }

    /** Liest eine Ganzzahl im Bereich [min, max]. */
    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int v = readInt(sc, prompt);
            if (v >= min && v <= max) return v;
            System.out.printf("Bitte eine Zahl zwischen %d und %d eingeben.%n", min, max);
        }
    }

    /**
     * Liest eine existierende Todo-ID aus der Liste. Gibt -1 zurück, wenn die Liste leer ist.
     * Fragt bei unbekannter ID erneut.
     */
    private int readExistingTodoId(Scanner sc) {
        // wir lesen über service.list(); keine Änderungen an der Service-API
        // (Achtung: keine Seiteneffekte, nur Anzeige + Auswahl)
        // Diese Methode ist in der UI-Klasse, daher Zugriff über this.service
        // → einfach die Liste holen:
        List<Todo> todos = new java.util.ArrayList<>(/* defensive copy */ service.list());
        if (todos.isEmpty()) {
            System.out.println("Es gibt noch keine Erledigungen.");
            return -1;
        }
        System.out.println("Vorhandene IDs:");
        todos.forEach(t -> System.out.println(" - " + t.getId() + ": " + t.getText()));
        while (true) {
            int id = readInt(sc, "ID eingeben: ");
            boolean exists = todos.stream().anyMatch(t -> t.getId() == id);
            if (exists) return id;
            System.out.println("Diese ID existiert nicht. Bitte erneut versuchen.");
        }
    }

    /* ===================== Anzeige ===================== */

    private static void printMenu() {
        System.out.println("=== TodoApp ===");
        System.out.println("1) Anzeigen");
        System.out.println("2) Hinzufügen");
        System.out.println("3) Verändern");
        System.out.println("4) Löschen");
        System.out.println("5) Erledigen");
        System.out.println("6) Speichern");
        System.out.println("7) Laden");
        System.out.println("8) Beenden");
    }

}
