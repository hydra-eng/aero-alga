#include "config.h"
#include <Arduino.h>

void initActuators() {
    ledcAttach(PIN_ACTUATOR_PUMP, PWM_FREQ, PWM_RESOLUTION);
    ledcAttach(PIN_ACTUATOR_LED, PWM_FREQ, PWM_RESOLUTION);

    // Initial default states: Pump 55%, LED Off
    ledcWrite(PIN_ACTUATOR_PUMP, (55 * 255) / 100);
    ledcWrite(PIN_ACTUATOR_LED, 0);
}

void setPumpSpeed(int speedPct) {
    speedPct = constrain(speedPct, 0, 100);
    int duty = (speedPct * 255) / 100;
    ledcWrite(PIN_ACTUATOR_PUMP, duty);
}

void setLedAssist(bool enable) {
    ledcWrite(PIN_ACTUATOR_LED, enable ? 255 : 0);
}
