# AClient

> **AI-assisted development project**

A [Sources Code](https://github.com/Arvie1290/AClient) Mods, lightweight, client built exclusively for **Fabric 1.21.11**. Developed just for fun :3, AClient introduces an advanced, completely detached Freecam engine with good input and smart physics retention.
[Releases](https://github.com/Arvie1290/AClient/releases/) Download here!.

## Requirements

- Minecraft 1.21.11
- Fabric Loader
- Fabric API

---

## Features

### Freecam
* **Detached Camera:** A complete detached cinematic camera system. Fly around and scout your world freely while your physical body stays frozen in place.
* **Absolute Input Isolation:** Pressing movement keys or moving your mouse will only manipulate the Freecam view—zero input leaks to your physical player.
* **Player body continues to obey gravity while Freecam is active:** United from ordinary freecam mods, your character body still interacts with world physics. If the blocks beneath your feet are destroyed, your player will naturally fall down matching vanilla gravity!

### Controls and Customization
* **Dynamic Sprint Modifier (Ctrl + Movement):** Boost your flight speed instantly on the fly by holding down Left or Right Control keys while flying.
* **Interactive Click GUI:** A fully animated config overlay to manage all parameters seamlessly.
* **Precision Fine-Tuning Sliders:**
  * Speed of Freecam - Fine-tune your baseline movement from a crawl (0.01) up to hyper-speed (100.00).
  * Sprint Freecam (Ctrl + Movement) - Customize your exact speed boost modifier from 0.01 to 50.00 (Default: 2.25).
  * Freecam Sensitivity - Seamless look tracking scaled perfectly with native mouse scaling values (Default: 1.00).

### System and Safety Cutoffs
* **Dimension Change Safety:** Automatically deactivates Freecam gracefully when crossing dimensions (Portals, Ender Pearls) to prevent world-loading crashes and coordinate synchronization errors.
* **Damage Cutoff:** Instantly snaps your camera back to first-person view the exact split-second your physical body takes any form of damage.
* **Configuration Saving:** Integrated JSON auto-save engine (aclient_config.json). All custom keybinds, slider adjustments, and toggles are automatically saved to disk and preserved across game restarts.

---

## Default Keybindings

| Action | Default Key | Description |
| :--- | :---: | :--- |
| Open Click GUI Menu | INSERT | Toggles the client's overlay configuration matrix |
| Freecam Activation | *Not Bound* | Toggle via Click GUI or assign a custom keybind inside the menu |
| Sprint Fly Mode | Left / Right CTRL | Adds your configured sprint speed value dynamically |
| Vertical Ascent | SPACE | Floats the Freecam camera straight upwards |
| Vertical Descent | LEFT SHIFT | Lowers the Freecam camera straight downwards |

---

## Developer Setup and Building

To compile, build, or run the development client environment on your local machine, use the provided Gradle build scripts or native shortcut launchers:

### Run Client In Development Mode

#### Standard Terminal Method:
```bash
./gradlew clean runClient
```

#### Fast One-Click Launchers:

* **For Linux (runClient_Linux.bash):**
  Simply double-click the file to launch!
  Note: If it doesn't run, Right Click the file > Go to Properties/Permissions > Check/Enable "Allow executing file as program".
* **For Windows (runClient_Windows.bat):**
  Just double-click the file and you're ready to debug!

### Build Production Mod Jar File

```bash
./gradlew build
```

The compiled .jar file will be generated inside the build/libs/ directory.

---

> **Note From Creator:** This client mod is compiled and optimized strictly for the Fabric 1.21.11 ecosystem. Using this on other versions might break Mixin injectors or cause mapping failures! Have fun debugging! :3