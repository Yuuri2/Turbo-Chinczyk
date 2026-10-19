# 🎲 Turbo Chińczyk

**Turbo Chińczyk** to dynamiczna, wieloosobowa odmiana klasycznej gry w Chińczyka (Ludo), wzbogacona o system ekonomiczny, ulepszenia tactical/power-up, specjalne pola specjalne oraz zaawansowane strategie.

---

## 📋 Spis Treści
- [Główne Założenia](#-główne-założenia)
- [Pętla Gry (Game Loop)](#-pętla-gry-game-loop)
- [Ekonomia i Kredyty](#-ekonomia-i-kredyty)
- [Sklep i Ulepszenia](#-sklep-i-ulepszenia)
- [Pola Specjalne](#-pola-specjalne)
- [Architektura i Plan Technologiczny](#-architektura-i-plan-technologiczny)

---

## 🎮 Główne Założenia

* **Liczba graczy:** 4 (Żółty, Zielony, Czerwony, Niebieski).
* **Zasada jednego pola:** Na jednym polu może stać maksymalnie **jeden** pionek.
* **Baza i Zwycięstwo:**
  * Gracz, który wprowadzi wszystkie swoje pionki do bazy, kończy grę i staje się nietykalny (nie można wchodzić z nim w interakcje).
  * Gra kończy się w momencie, gdy na planszy pozostanie tylko jeden gracz z niepełną bazą.

---

## 🔄 Pętla Gry (Game Loop)

1. **Start:** Gra losuje gracza rozpoczynającego. Następne tury następują zgodnie z ruchem wskazówek zegara.
2. **Struktura Tury:**
   * 🛍️ **Faza Zakupu:** Gracz może wydać zebrane kredyty na potężne ulepszenia.
   * 🎲 **Faza Ruchu:** Gracz rzuca kością i wykonuje ruch na planszy.
3. **Zasada Szóstki ($6$):**
   * Wyrzucenie 6 pozwala wystawić nowego pionka ze strefy oczekiwania na pole startowe (ciemniejsze pole w kolorze gracza) **LUB** poruszyć się już istniejącym pionkiem.
   * Po wyrzuceniu 6 gracz otrzymuje **dodatkowy rzut** (kolejne rzuty trwają aż do wyrzucenia liczby innej niż 6).
4. **Zbijanie i Przeskakiwanie:**
   * Dopuszczalne jest przeskakiwanie pionków innych graczy.
   * Lądowanie na polu zajętym przez przeciwnika skutkuje **zbiciem** jego pionka i odesłaniem go do strefy oczekiwania.

---

## 💰 Ekonomia i Kredyty

Kredyty to waluta pozwalająca na kupowanie ulepszeń odmieniających losy rozgrywki. Liczba kredytów gracza **nigdy nie może spaść poniżej 0**.

### ➕ Pozyskiwanie Kredytów
| Akcja | Nagroda |
| :--- | :--- |
| **Zbicie pionka przeciwnika** | `+10 pkt` |
| **Wprowadzenie pionka do bazy** | `+10 pkt` |
| **Rezygnacja z wejścia do bazy** *(wykonanie dodatkowego okrążenia tym samym pionkiem)* | `+50 pkt` |

### ➖ Utrata Kredytów
| Akcja | Kara |
| :--- | :--- |
| **Zbicie własnego pionka przez przeciwnika** | `-10 pkt` |

---

## 🛒 Sklep i Ulepszenia

Gracz może przeznaczyć kredyty na zakupy na początku swojej tury w Fazie Zakupu.

```
+------------------+--------+-------------------------------------------------------------+
| Ulepszenie       | Koszt  | Opis działania                                              |
+------------------+--------+-------------------------------------------------------------+
| 🧱 Mur           | 15 pkt | Blokuje przejście. Wymaga 3 uderzeń (rzutów dających ruch   |
|                  |        | za mur), by go zniszczyć (HP: 3 -> 0). Można go ominąć      |
|                  |        | lądując dokładnie na nim.                                   |
|                  |        |                                                             |
| 🕵️ Szpieg         | 40 pkt | Promocja możliwa tylko na własnym kolorze. Infiltruje bazę  |
|                  |        | wroga, cofając jego pionek do oczekiwania. Po akcji wraca  |
|                  |        | przed wejście do bazy jako zwykły pionek.                   |
|                  |        |                                                             |
| 🎲 Potrójny Rzut | 20 pkt | Umożliwia 3 rzuty w fazie ruchu zamiast 1.                  |
|                  |        | Wyrzucone 6 nie wliczają się w limit 3 rzutów.              |
|                  |        |                                                             |
| 🌋 Trzęsienie    | 100pkt | Wywołuje kataklizm na obszarze jednego z graczy. Zbiła      |
|    Ziemi         |        | wszystkie pionki poza bazą i niszczy umieszczone tam mury. |
+------------------+--------+-------------------------------------------------------------+
```

---

## 🟩 Pola Specjalne

* 🟫 **Pole Ciemne (Startowe):** Miejsce startowe dla danego gracza przy wyrzuceniu 6.
* ⬛ **Powrót:** Cofnięcie do najbliższego pola startowego dowolnego gracza.
* 🟧 **Skok:** Przejście bezpośrednio na następne pomarańczowe pole.

---

## 🛠️ Architektura i Plan Technologiczny

### Proposed Stack:
* **Frontend:** HTML5 / TypeScript / React lub Svelte
* **Backend:** Node.js z **Socket.IO** lub **Colyseus** (real-time multiplayer framework)
* **Baza Danych:** SQLite (opcjonalnie do przechowywania statystyk / kont)

### Dodatkowe Funkcjonalności & Pomysły:
- 📲 **Szybkie dołączanie:** Generowanie i skanowanie kodów QR.
- 🚪 **System Lobby:** Tworzenie i dołączanie do pokojów gry.
- 🤖 **Boty i Strategie:** Implementacja SI priorytetyzującej:
  - *Zarabianie punktów*
  - *Maksymalizację liczby pionków na planszy*
  - *Maksymalizację pokonanego dystansu*

---
