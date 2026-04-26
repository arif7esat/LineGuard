package com.lineguard.statemachine;

import com.lineguard.exception.InvalidStateTransitionException;
import com.lineguard.model.ProductionLineState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Defines valid state transitions for a production line and guards against
 * invalid ones.
 *
 * <pre>
 * IDLE    ──start──► RUNNING
 * RUNNING ──pause──► PAUSED
 * RUNNING ──fault──► FAULT
 * PAUSED  ──resume─► RUNNING
 * FAULT   ──reset──► IDLE
 * RUNNING ──stop───► STOPPED
 * PAUSED  ──stop───► STOPPED
 * IDLE    ──stop───► STOPPED
 * STOPPED ──reset──► IDLE
 * </pre>
 */
@Component
public class ProductionLineStateMachine {

    /** Allowed transitions: event → (allowed source states → target state). */
    private static final Map<String, Map<Set<ProductionLineState>, ProductionLineState>> TRANSITIONS =
            Map.of(
                "start",  Map.of(Set.of(ProductionLineState.IDLE),    ProductionLineState.RUNNING),
                "pause",  Map.of(Set.of(ProductionLineState.RUNNING), ProductionLineState.PAUSED),
                "resume", Map.of(Set.of(ProductionLineState.PAUSED),  ProductionLineState.RUNNING),
                "fault",  Map.of(Set.of(ProductionLineState.RUNNING, ProductionLineState.PAUSED),
                                                                       ProductionLineState.FAULT),
                "reset",  Map.of(Set.of(ProductionLineState.FAULT, ProductionLineState.STOPPED),
                                                                       ProductionLineState.IDLE),
                "stop",   Map.of(Set.of(ProductionLineState.IDLE,
                                        ProductionLineState.RUNNING,
                                        ProductionLineState.PAUSED),   ProductionLineState.STOPPED)
            );

    /**
     * Applies the given event to the current state and returns the new state.
     *
     * @param current the current state of the production line
     * @param event   the event to apply (start / pause / resume / fault / reset / stop)
     * @return the resulting state after the transition
     * @throws InvalidStateTransitionException if the transition is not allowed
     */
    public ProductionLineState transition(ProductionLineState current, String event) {
        Map<Set<ProductionLineState>, ProductionLineState> eventTransitions =
                TRANSITIONS.get(event.toLowerCase());

        if (eventTransitions == null) {
            throw new InvalidStateTransitionException(
                    "Unknown event '" + event + "'.");
        }

        for (Map.Entry<Set<ProductionLineState>, ProductionLineState> entry : eventTransitions.entrySet()) {
            if (entry.getKey().contains(current)) {
                return entry.getValue();
            }
        }

        throw new InvalidStateTransitionException(
                "Cannot apply event '" + event + "' to a line in state '" + current + "'.");
    }
}
