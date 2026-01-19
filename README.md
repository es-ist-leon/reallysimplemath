# 🎮 RETRO MATH - 8-BIT EDITION

Ein visuelles Mathe-Lernspiel mit **3D 8-bit Grafik** und **Microservices-Architektur**.

```
╔══════════════════════════════════════════════════════════╗
║   ██████╗ ███████╗████████╗██████╗  ██████╗              ║
║   ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔═══██╗             ║
║   ██████╔╝█████╗     ██║   ██████╔╝██║   ██║             ║
║   ██╔══██╗██╔══╝     ██║   ██╔══██╗██║   ██║             ║
║   ██║  ██║███████╗   ██║   ██║  ██║╚██████╔╝             ║
║   ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝ ╚═════╝              ║
║                                                          ║
║   ███╗   ███╗ █████╗ ████████╗██╗  ██╗                   ║
║   ████╗ ████║██╔══██╗╚══██╔══╝██║  ██║                   ║
║   ██╔████╔██║███████║   ██║   ███████║                   ║
║   ██║╚██╔╝██║██╔══██║   ██║   ██╔══██║                   ║
║   ██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║                   ║
║   ╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝                   ║
║                                                          ║
║              8-BIT EDITION - MICROSERVICES               ║
╚══════════════════════════════════════════════════════════╝
```

---

## 🚀 Schnellstart

```bash
# Kompilieren
./build.sh

# Starten
java -jar build/RetroMathGame.jar
```

---

## 🏗️ Microservices-Architektur

Das Spiel verwendet eine modulare Microservices-Architektur mit Event-driven Communication:

```
                    ┌─────────────────────┐
                    │   ServiceManager    │
                    │  (Orchestrierung)   │
                    └──────────┬──────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
    ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
    │TaskService│        │ScoreService│        │AudioService│
    │ (Aufgaben)│        │  (Punkte)  │        │  (Sound)  │
    └─────┬─────┘        └─────┬─────┘        └─────┬─────┘
          │                    │                    │
          └────────────────────┼────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │      EventBus       │
                    │  (Message Broker)   │
                    └──────────┬──────────┘
                               │
          ┌────────────────────┴────────────────────┐
          │                                         │
    ┌─────┴─────┐                            ┌─────┴─────┐
    │RenderService│                          │ UIService │
    │  (3D 8-bit) │                          │   (GUI)   │
    └───────────┘                            └───────────┘
```

### Services

| Service | Verantwortung |
|---------|---------------|
| **TaskService** | Generiert mathematische Aufgaben |
| **ScoreService** | Verwaltet Punkte, Level, Statistiken |
| **AudioService** | 8-bit Chiptune Soundeffekte |
| **RenderService** | Isometrische 3D-Visualisierung |
| **UIService** | Benutzeroberfläche & Input |
| **EventBus** | Asynchrone Service-Kommunikation |

---

## 🎨 8-Bit 3D Features

### Grafik-Engine
- **Isometrische Projektion** für 3D-Effekt
- **Voxel-basierte Objekte** (3D-Blöcke)
- **NES-inspirierte Farbpalette** (22 Farben)
- **Pixel-Schrift** (8x8 Pixel pro Zeichen)

### Visuelle Effekte
- ✨ CRT-Scanlines
- 🌟 Vignette-Effekt
- 💫 Glow-Text
- 🎬 Bounce & Shake Animationen

### Sound
- 🎵 Chiptune Square-Wave Sounds
- 🔊 Dynamisch generierte 8-bit Töne
- 🎶 Erfolgs- und Fehler-Melodien

---

## 📚 Übungstypen

| Typ | Visualisierung | Beschreibung |
|-----|----------------|--------------|
| **Addition** | Zwei Gruppen von Blöcken | Zähle alle Blöcke zusammen |
| **Subtraktion** | Graue vs. farbige Blöcke | Graue Blöcke werden abgezogen |
| **Multiplikation** | Gitter aus Blöcken | Reihen × Spalten |
| **Division** | Blöcke auf Podesten | Wie viele pro Gruppe? |
| **Gemischt** | Zufällig | Alle Typen gemischt |

---

## 📁 Projektstruktur

```
mathgame/
├── RetroMathGame.java      # Hauptklasse
├── build.sh                # Build-Skript
│
├── core/
│   ├── Service.java        # Service Interface
│   └── ServiceManager.java # Service Orchestrierung
│
├── events/
│   ├── EventBus.java       # Message Broker
│   └── GameEvent.java      # Event-Klasse
│
├── rendering/
│   ├── Retro3DRenderer.java # 3D Isometric Engine
│   └── RetroFont.java       # 8-bit Pixel Font
│
├── services/
│   ├── TaskService.java    # Aufgaben-Logik
│   ├── ScoreService.java   # Punkte-System
│   ├── AudioService.java   # Sound-Effekte
│   ├── RenderService.java  # 3D Szenen
│   └── UIService.java      # Benutzeroberfläche
│
└── build/
    └── RetroMathGame.jar   # Ausführbare JAR
```

---

## 🎮 Spielsteuerung

| Aktion | Taste/Button |
|--------|--------------|
| Antwort eingeben | Zahlen tippen |
| Antwort prüfen | `Enter` oder `PRÜFEN` |
| Neue Aufgabe | `NEU` Button |
| Sound an/aus | `♪ SOUND` Button |
| Modus wählen | Dropdown-Menü |

---

## 🏆 Spielsystem

- **Punkte**: Level × 10 + Streak-Bonus
- **Streak**: Aufeinanderfolgende richtige Antworten
- **Level-Up**: Nach 5 richtigen Antworten
- **Schwierigkeit**: Steigt mit jedem Level

---

## ⚙️ Technische Details

- **Sprache**: Java 8+
- **GUI Framework**: Swing
- **Audio**: javax.sound.sampled
- **Pattern**: Microservices, Event-Driven, Publish-Subscribe
- **Keine externen Abhängigkeiten**

---

## 📖 Erweiterung

Neue Services können einfach hinzugefügt werden:

```java
public class MyService implements Service {
    @Override
    public void start() {
        EventBus.getInstance().subscribe("MY_EVENT", this::handle);
    }
    
    private void handle(GameEvent event) {
        // Event verarbeiten
    }
}

// Registrieren
serviceManager.registerService(new MyService());
```

---

## 📜 Lizenz

MIT License - Frei verwendbar für Lernzwecke.

---

**Viel Spaß beim Lernen! 🎉**
