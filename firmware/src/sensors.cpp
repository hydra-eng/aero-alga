#include "sensors.h"
#include "config.h"
#include <Wire.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <BH1750.h>
#include <SoftwareSerial.h>

static OneWire oneWire(PIN_ONEWIRE_TEMP);
static DallasTemperature tempSensor(&oneWire);
static BH1750 lightMeter(0x23);

static SoftwareSerial pmsOutSerial(PIN_PMS_OUT_RX, PIN_PMS_OUT_TX);
static SoftwareSerial mhzOutSerial(PIN_MHZ_OUT_RX, PIN_MHZ_OUT_TX);

static volatile uint32_t flowPulseCount = 0;
static uint32_t lastFlowCalcTime = 0;

void IRAM_ATTR flowPulseCounter() {
    flowPulseCount++;
}

// MH-Z19 command to request CO2 concentration
const byte mhzCmd[9] = {0xFF, 0x01, 0x86, 0x00, 0x00, 0x00, 0x00, 0x00, 0x79};

int readMHZ19(Stream &stream) {
    stream.write(mhzCmd, 9);
    byte response[9];
    memset(response, 0, 9);
    uint32_t start = millis();
    int idx = 0;
    while (millis() - start < 150 && idx < 9) {
        if (stream.available()) {
            response[idx++] = stream.read();
        }
    }
    if (idx == 9 && response[0] == 0xFF && response[1] == 0x86) {
        return (response[2] * 256) + response[3];
    }
    return -1;
}

// Read PMS5003 packet (32 bytes standard)
bool readPMS5003(Stream &stream, float &pm25, float &pm10) {
    if (!stream.available()) return false;
    if (stream.read() != 0x42 || stream.read() != 0x4D) return false;
    
    byte buf[30];
    if (stream.readBytes(buf, 30) == 30) {
        int pm25_val = (buf[10] << 8) | buf[11];
        int pm10_val = (buf[12] << 8) | buf[13];
        pm25 = (float)pm25_val;
        pm10 = (float)pm10_val;
        return true;
    }
    return false;
}

void initSensors() {
    Serial.begin(115200);

    // Hardware Serial 1 for PMS5003 Inlet
    Serial1.begin(9600, SERIAL_8N1, PIN_PMS_IN_RX, PIN_PMS_IN_TX);
    
    // Software Serial for PMS5003 Outlet
    pmsOutSerial.begin(9600);

    // Hardware Serial 2 for MH-Z19B Inlet
    Serial2.begin(9600, SERIAL_8N1, PIN_MHZ_IN_RX, PIN_MHZ_IN_TX);

    // Software Serial for MH-Z19B Outlet
    mhzOutSerial.begin(9600);

    // OneWire Temperature
    tempSensor.begin();

    // I2C BH1750
    Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
    lightMeter.begin(BH1750::CONTINUOUS_HIGH_RES_MODE);

    // Flow sensor pulse interrupt
    pinMode(PIN_FLOW_SENSOR, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(PIN_FLOW_SENSOR), flowPulseCounter, RISING);
    lastFlowCalcTime = millis();

    // ADC pins configuration
    analogReadResolution(12);
    analogSetAttenuation(ADC_11db);
}

void pollSensors(SensorTelemetry &telemetry) {
    // 1. Air Quality PMS5003
    float p25_in = 0, p10_in = 0;
    if (readPMS5003(Serial1, p25_in, p10_in)) {
        telemetry.pm25_in = p25_in;
        telemetry.pm10_in = p10_in;
    }
    float p25_out = 0, p10_out = 0;
    if (readPMS5003(pmsOutSerial, p25_out, p10_out)) {
        telemetry.pm25_out = p25_out;
        telemetry.pm10_out = p10_out;
    }

    // 2. Air Quality MH-Z19B CO2
    int co2InVal = readMHZ19(Serial2);
    if (co2InVal > 0) telemetry.co2_in = (float)co2InVal;

    int co2OutVal = readMHZ19(mhzOutSerial);
    if (co2OutVal > 0) telemetry.co2_out = (float)co2OutVal;

    // 3. Culture Health: DS18B20 Temp
    tempSensor.requestTemperatures();
    float t = tempSensor.getTempCByIndex(0);
    if (t > -50.0f && t < 100.0f) {
        telemetry.water_temp = t;
    }

    // 4. Culture Health: Analog pH (12-bit ADC -> voltage -> pH calibration)
    int rawPh = analogRead(PIN_ADC_PH);
    float vPh = (rawPh / 4095.0f) * 3.3f;
    // Standard analog probe linear calibration equation: pH = 3.5 * voltage + offset
    telemetry.ph = 3.5f * vPh + 2.1f;

    // 5. Culture Health: Turbidity (NTU proxy for biomass)
    int rawNtu = analogRead(PIN_ADC_TURBIDITY);
    float vNtu = (rawNtu / 4095.0f) * 3.3f;
    // Turbidity curve
    telemetry.turbidity_ntu = max(0.0f, -1120.4f * (vNtu * vNtu) + 5742.3f * vNtu - 4352.9f);

    // 6. Dissolved Oxygen probe
    int rawDO = analogRead(PIN_ADC_DO);
    float vDO = (rawDO / 4095.0f) * 3.3f;
    telemetry.dissolved_oxygen = vDO * 3.5f;

    // 7. Light sensor (BH1750)
    telemetry.light_lux = lightMeter.readLightLevel();

    // 8. Flow rate calculation (Pulses per second / conversion factor)
    uint32_t now = millis();
    float elapsedSec = (now - lastFlowCalcTime) / 1000.0f;
    if (elapsedSec >= 1.0f) {
        // Standard water/air hall-effect: 7.5 pulses per second = 1 L/min
        float pulses = (float)flowPulseCount;
        flowPulseCount = 0;
        lastFlowCalcTime = now;
        telemetry.flow_rate_lpm = pulses / (7.5f * elapsedSec);
    }
}
