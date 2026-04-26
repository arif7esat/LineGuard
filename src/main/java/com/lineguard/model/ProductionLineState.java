package com.lineguard.model;

/**
 * States of a production line in the state machine.
 *
 * <pre>
 * IDLE ──start──► RUNNING ──pause──► PAUSED
 *                   ▲  │               │
 *                   │  └──fault──►  FAULT
 *                 reset              │
 *                   └───────────────┘
 * Any state ──stop──► STOPPED ──reset──► IDLE
 * </pre>
 */
public enum ProductionLineState {
    IDLE,
    RUNNING,
    PAUSED,
    FAULT,
    STOPPED
}
