# Siege System Lifecycle Documentation

This document outlines the features, states, and validations of the **Siege System** implemented for territories, teams, and points management. All core functionality has been implemented and verified.

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

---

## Points

| Action | Expected Result | Status |
|--------|----------------|--------|
| Adding banner capture points | Increases team points | Hytale Integration |
| Adding kill points | Increases team points | Hytale Integration |
| Adding point to a team | Updates points correctly | ✅ Done |
| Getting team total points | Returns correct value | ✅ Done |
| Comparing two teams’ points | Returns correct winner | ✅ Done |

---

## Timer & State

| Action | Expected Result | Status |
|--------|----------------|--------|
| Starting the siege timer | Sets the start time | ✅ Done |
| Siege reports expired | When duration exceeded | ✅ Done |
| Siege reports not expired | Before duration | ✅ Done |
| Setting banner active | Marks banner as active | ✅ Done |
| Setting banner inactive | Marks banner as inactive | ✅ Done |
| Checking banner state | Returns correct value | ✅ Done |

---

## Outcome

| Action | Expected Result | Status |
|--------|----------------|--------|
| Determining winner | Returns attacker team if attacker has more points | ✅ Done |
| Determining winner | Returns defender team if defender has more points | ✅ Done |
| Determining winner | Returns no winner on tie | ✅ Done |

---

## Territory Effects

| Action | Expected Result | Status |
|--------|----------------|--------|
| Setting territory owner | Updates owner correctly | ✅ Done |
| Getting territory owner | Returns correct owner | ✅ Done |
| Setting territory color | Updates color correctly | ✅ Done |
| Setting territory tax rate | Updates tax rate correctly | ✅ Done |

---

## Validation

| Action | Expected Result | Status |
|--------|----------------|--------|
| Siege cannot start on invalid territory | Validation enforced | ✅ Done |
| Banner cannot be placed if territory is already sieged | Validation enforced | ✅ Done |
| Player cannot interact with siege if not in active siege | Validation enforced | ✅ Done |

---

> **Status Legend:**  
> ✅ Done – Fully implemented and verified  
> ⚠️ Hytale Integration – Pending integration with Hytale-specific systems
