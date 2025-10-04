package com.github.danielnickel94.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TodoServiceTest {

    /* ====================== Basis-Operationen ====================== */

    @Test
    void add_shouldCreateTodoWithIncrementingId_andDefaultDoneFalse() {
        var s = new TodoService();
        int id1 = s.add("Einkaufen");
        int id2 = s.add("Putzen");

        assertEquals(1, id1);
        assertEquals(2, id2);

        var todos = s.list();
        assertEquals(2, todos.size());
        assertEquals("Einkaufen", todos.get(0).getText());
        assertFalse(todos.get(0).isDone());
    }

    @Test
    void add_shouldRejectEmptyOrBlankText() {
        var s = new TodoService();
        assertThrows(IllegalArgumentException.class, () -> s.add(""));
        assertThrows(IllegalArgumentException.class, () -> s.add("   "));
        assertThrows(IllegalArgumentException.class, () -> s.add(null));
        assertTrue(s.list().isEmpty());
    }

    @Test
    void updateText_shouldChangeText_whenIdExists() {
        var s = new TodoService();
        int id = s.add("Alt");
        assertTrue(s.updateText(id, "Neu"));
        assertEquals("Neu", s.list().get(0).getText());
    }

    @Test
    void updateText_shouldReturnFalse_whenIdUnknown() {
        var s = new TodoService();
        s.add("x");
        assertFalse(s.updateText(999, "egal"));
    }

    @Test
    void remove_shouldDeleteTodo_whenIdExists() {
        var s = new TodoService();
        int id = s.add("Test");
        assertTrue(s.remove(id));
        assertTrue(s.list().isEmpty());
    }

    @Test
    void remove_shouldReturnFalse_whenIdUnknown() {
        var s = new TodoService();
        s.add("A");
        assertFalse(s.remove(42));
        assertEquals(1, s.list().size());
    }

    @Test
    void markDone_shouldSetDoneTrue_whenIdExists() {
        var s = new TodoService();
        int id = s.add("Aufgabe");
        assertTrue(s.markDone(id));
        assertTrue(s.list().get(0).isDone());
    }

    @Test
    void markDone_shouldReturnFalse_whenIdUnknown() {
        var s = new TodoService();
        s.add("Aufgabe");
        assertFalse(s.markDone(999));
        assertFalse(s.list().get(0).isDone());
    }

    @Test
    void list_shouldBeUnmodifiableList() {
        var s = new TodoService();
        s.add("X");
        var view = s.list();
        assertThrows(UnsupportedOperationException.class, () -> view.add(new Todo(99, "hack", false)));
    }

    /* ======= Komfortfunktionen (falls in deinem Service vorhanden) ======= */
    // Falls du listOpen(), search(), sortByText() bereits hinzugefügt hast,
    // sind diese Tests aktiv; sonst kannst du sie vorerst auskommentieren.

    @Test
    void listOpen_shouldReturnOnlyNotDone() {
        var s = new TodoService();
        int a = s.add("offen 1");
        int b = s.add("offen 2");
        int c = s.add("erledigt");
        s.markDone(c);

        // erwartet: nur a,b
        List<Todo> open = s.listOpen();
        assertEquals(2, open.size());
        assertTrue(open.stream().allMatch(t -> !t.isDone()));
        assertTrue(open.stream().map(Todo::getId).toList().containsAll(List.of(a, b)));
    }

    @Test
    void search_shouldBeCaseInsensitive_andTrimInput() {
        var s = new TodoService();
        s.add("Schwiegereltern anrufen");
        s.add("Auto waschen");
        s.add("Wasser holen");

        var r1 = s.search("  WAS  ");
        assertEquals(2, r1.size()); // "waschen" + "Wasser"

        var r2 = s.search("");
        assertEquals(3, r2.size()); // leer => nichts filtern

        var r3 = s.search(null);
        assertEquals(3, r3.size()); // null => nichts filtern
    }

    @Test
    void sortByText_shouldSortAscAndDescIgnoringCase() {
        var s = new TodoService();
        s.add("brot kaufen");
        s.add("Abwaschen");
        s.add("Auto tanken");

        var asc = s.sortByText(true).stream().map(Todo::getText).toList();
        assertEquals(List.of("Abwaschen", "Auto tanken", "brot kaufen"), asc);

        var desc = s.sortByText(false).stream().map(Todo::getText).toList();
        assertEquals(List.of("brot kaufen", "Auto tanken", "Abwaschen"), desc);
    }

    /* ====================== Persistenz (CSV) ====================== */

    @Test
    void saveAndLoad_shouldPersistTodos(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("todos.csv");

        var s1 = new TodoService();
        s1.add("A");
        s1.add("B");
        assertTrue(s1.save(file.toString()));
        assertTrue(Files.exists(file));

        var s2 = new TodoService();
        assertTrue(s2.load(file.toString()));

        var list = s2.list();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getText());
        assertEquals("B", list.get(1).getText());
        assertFalse(list.get(0).isDone());
    }

    @Test
    void load_shouldBeTransactional_keepOldStateOnFailure(@TempDir Path tmp) {
        // Ausgangszustand mit 1 Todo
        var s = new TodoService();
        s.add("Bleib da");

        // lade von nicht existierender Datei → false & Zustand bleibt erhalten
        Path missing = tmp.resolve("does_not_exist.csv");
        assertFalse(s.load(missing.toString()));

        var stillThere = s.list();
        assertEquals(1, stillThere.size());
        assertEquals("Bleib da", stillThere.get(0).getText());
    }

    @Test
    void saveAndLoad_emptyList_shouldWork(@TempDir Path tmp) {
        var s = new TodoService();
        Path file = tmp.resolve("empty.csv");
        assertTrue(s.save(file.toString()));

        var s2 = new TodoService();
        assertTrue(s2.load(file.toString()));
        assertTrue(s2.list().isEmpty());
    }

    @Test
    void load_emptyFile_shouldResultInEmptyList(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("empty.csv");
        Files.createFile(file); // echte leere Datei
        var s = new TodoService();
        assertTrue(s.load(file.toString()));
        assertTrue(s.list().isEmpty());
    }

    @Test
    void nextId_afterLoad_shouldContinueFromMaxId(@TempDir Path tmp) {
        var s1 = new TodoService();
        s1.add("A"); // id 1
        s1.add("B"); // id 2
        Path file = tmp.resolve("todos.csv");
        assertTrue(s1.save(file.toString()));

        var s2 = new TodoService();
        assertTrue(s2.load(file.toString()));
        int newId = s2.add("C");
        assertEquals(3, newId);
    }

    @Test
    void ids_shouldNotBeReusedAfterRemove() {
        var s = new TodoService();
        int id1 = s.add("A");        // 1
        int id2 = s.add("B");        // 2
        assertTrue(s.remove(id1));   // lösche 1
        int id3 = s.add("C");        // sollte 3 sein, nicht 1
        assertEquals(3, id3);
    }

    @Test
    void search_noMatch_shouldReturnEmpty() {
        var s = new TodoService();
        s.add("Auto waschen");
        var r = s.search("xyz");
        assertTrue(r.isEmpty());
    }


}
