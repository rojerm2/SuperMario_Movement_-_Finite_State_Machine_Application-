This project implements a **Finite State Machine (FSM)** in Java to manage Mario's movement logic and animations. Using an FSM keeps the state transitions clean and prevents illegal move combinations.

## Core States
*   **Idle**: Standing still.
*   **Moving**: Ground movement.
*   **Jumping-Idle**: Vertical jump from a standstill.
*   **Jumping-Moving**: Jumping while moving horizontally.
*   **Crouching**: Ducking/Low profile.

---

## State Transition Table
The logic follows this mapping to ensure smooth transitions:

| Current State | Input / Trigger | Next State |
| :--- | :--- | :--- |
| **Idle** | Press Left/Right | Moving |
| **Idle** | Press Jump | Jumping-Idle |
| **Moving** | Release Keys | Idle |
| **Moving** | Press Jump | Jumping-Moving |
| **Jumping-*** | Land on Ground | Idle |
| **Idle** | Press Down | Crouching |

---

## Diagram
<img width="3005" height="3521" alt="Mario Sprite - Finite State Machine Application" src="https://github.com/user-attachments/assets/b0a124bc-7ead-4ac0-ab4f-9c85cf6deec7" />

