# Siege System Lifecycle Documentation

This document outlines the features, states, and validations of the **Siege System** implemented for territories, teams, and points management. The system is mostly complete and integrated with **Hytale**, with a few pending improvements (e.g., block protection for banners and proper banner blocks).

---

## SimpleClaims Mod Integration

The **SimpleClaims** mod is used to manage **territories and player claims**, and it integrates closely with the Siege System to handle ownership, access, and dependency rules.

### Key Points:

- **Territory Management**
  - Each territory in the Siege System corresponds to a **SimpleClaims claim**.
  - Claim ownership is synced with the territory owner in the Siege System.
  - Siege actions respect claim boundaries to prevent unauthorized interactions.

- **Claims & Dependencies**
  - Territories can have **parent and child claims** (dependencies).
  - The system ensures that sieges cannot start on a territory if **dependent territories are restricted** or invalid.
  - This prevents players from bypassing protection rules or breaking hierarchical dependencies.

- **Access & Protection**
  - Players not in the active siege are blocked from interacting with claimed territories.
  - The **block break protection** ensures that banners and territory structures are protected according to claim rules, while still allowing siege interactions where permitted.
  - Future updates include replacing placeholder blocks with proper banner blocks and enforcing **Hytale-specific block protection** for full integration.

- **Integration Flow**
  1. When a siege starts, the system verifies that the target territory is valid and all **SimpleClaims dependencies** are satisfied.
  2. The Siege System updates claim ownership, banner status, and siege state in sync with SimpleClaims.
  3. Points, team actions, and timer mechanics respect claim boundaries, ensuring no unauthorized interactions occur outside the active siege.

---

## Hytale Integration Notes

- Most of the system is fully integrated with Hytale.
- Pending improvements:
  - Add **block break protection** to banner blocks.
  - Replace placeholder banner blocks with proper Hytale banners.
  - Ensure all territory effects and points are fully synchronized with Hytale mechanics.

---

## Siege Lifecycle

| Action | Expected Result | Status |
|--------|----------------|--------|
| Starting a siege on a territory | Returns `true` | ✅ Done |
| Starting a siege on an already sieged territory | Returns `false` | ✅ Done |
| Ending a siege on a territory | Returns `true` | ✅ Done |
| Ending a siege on a territory with no siege | Returns `false` | ✅ Done |
| Getting a siege by territory ID | Returns non-null if active | ✅ Done |
| Getting a siege by territory ID | Returns `null` if inactive | ✅ Done |
| Checking if a territory is under siege | Returns `true/false` correctly | ✅ Done |

---

## Teams (Inside a Siege)

| Action | Expected Result | Status |
|--------|----------------|--------|
| Adding a player to the attacker team | Succeeds | ✅ Done |
| Adding a player to the defender team | Succeeds | ✅ Done |
| Adding the same player to two teams | Fails | ✅ Done |
| Checking if two players are on the same team | Returns `true/false` correctly | ✅ Done |
| Checking if a player is in the siege | Returns `true/false` correctly | ✅ Done |

> **Note:** Getting a player’s team is not needed in this system.

