#include <SoftwareSerial.h>

// Read the report for the reason why we selected this baudrate
#define RS232_BAUD 57600

// Other defines
#define FRAME_LENGTH_WITHOUT_CRC 21

// Global variables
bool simulated = 0, valsReceived = 0;
byte installationHeight = 5;

// Initiate everything
void setup() {
  // Use the defined serial speed
  Serial.begin(RS232_BAUD);
}

// Runtime loop
int16_t i = -10;
void loop() {
  if (valsReceived) {
    byte objectID = i % 63;
    byte objectLength = i % 51;
    int16_t yVelocity = i % 1024;
    int16_t xVelocity = i % 1024;
    int16_t yPosition = i % 262112;
    int16_t xPosition = i % 262112;
  
    char frame[FRAME_LENGTH_WITHOUT_CRC];
    sprintf(frame,"G%02X%02X%04X%04X%04X%04X",objectID,objectLength,xPosition,yPosition,xVelocity,yVelocity);
  
    Serial.print(frame);
    Serial.print(checksum(frame));
  
    // 170 frames per second: 1000 / 170 = 5.9 (almost integer 9)
    delay(6);
  
    i++;
  } else {
    // Read the operation mode and installation height byte
    if (Serial.available() > 0) {
      // Read the init byte:
      byte initByte = Serial.read();
      
      simulated = (bool) (initByte >> 4);
      installationHeight = (byte) (((byte)(initByte << 4)) >> 4);
  
      // What we got:
      //Serial.println("I received: ");
      //Serial.println(simulated, DEC);
      //Serial.println(installationHeight, DEC);
      valsReceived = 1;
    }
  }
}

// Checksum calculation for the frame
char checksum(const char * str) {
  uint8_t sum = 0;
  for (uint8_t i = 0; i < FRAME_LENGTH_WITHOUT_CRC; i++) {
    sum += (uint8_t)str[i];
  }
  return 'H' + sum % 16;
}














