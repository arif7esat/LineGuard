package com.lineguard.statemachine;

import com.lineguard.exception.InvalidStateTransitionException;
import com.lineguard.model.ProductionLineState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductionLineStateMachine")
class ProductionLineStateMachineTest {

    private ProductionLineStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ProductionLineStateMachine();
    }

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "{0} --{1}--> {2}")
    @CsvSource({
        "IDLE,    start,  RUNNING",
        "RUNNING, pause,  PAUSED",
        "PAUSED,  resume, RUNNING",
        "RUNNING, fault,  FAULT",
        "PAUSED,  fault,  FAULT",
        "FAULT,   reset,  IDLE",
        "STOPPED, reset,  IDLE",
        "IDLE,    stop,   STOPPED",
        "RUNNING, stop,   STOPPED",
        "PAUSED,  stop,   STOPPED"
    })
    @DisplayName("valid transition")
    void validTransition(String from, String event, String expected) {
        ProductionLineState result = stateMachine.transition(
                ProductionLineState.valueOf(from.trim()), event.trim());
        assertThat(result).isEqualTo(ProductionLineState.valueOf(expected.trim()));
    }

    // -------------------------------------------------------------------------
    // Guarded transitions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cannot start a RUNNING line")
    void startRunningLine() {
        assertThatThrownBy(() -> stateMachine.transition(ProductionLineState.RUNNING, "start"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("cannot pause an IDLE line")
    void pauseIdleLine() {
        assertThatThrownBy(() -> stateMachine.transition(ProductionLineState.IDLE, "pause"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("cannot resume a RUNNING line")
    void resumeRunningLine() {
        assertThatThrownBy(() -> stateMachine.transition(ProductionLineState.RUNNING, "resume"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("cannot stop a FAULT line directly")
    void stopFaultLine() {
        assertThatThrownBy(() -> stateMachine.transition(ProductionLineState.FAULT, "stop"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("unknown event throws exception")
    void unknownEvent() {
        assertThatThrownBy(() -> stateMachine.transition(ProductionLineState.IDLE, "launch"))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("launch");
    }
}
