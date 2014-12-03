#include <SoftwareSerial.h>
#include <SPI.h>
#include "CAN.h"

// Read the report for the reason why we selected this baudrate
#define RS232_BAUD 57600

// Other defines
#define FRAME_LENGTH_WITHOUT_CRC 21

// Global variables
bool wakeUpCall = 1, sendRequest = 1, simulated = 0, valsReceived = 0;
byte rxStatus, length, frameData[8], extended, filter, startingFlag[] = { 
  0xFF,0xFF,0xFF,0xFF }, installationHeight = 5;
uint32_t frameID, oldTime = 0;

// Initiate everything
void setup() {
  // Use the defined serial port speed
  Serial.begin(RS232_BAUD);

  // Initialize CAN bus class
  // This class initializes SPI communications with MCP2515
  CAN.begin();
  delay(10);
  CAN.baudConfig();
  delay(10);

  CAN.resetFiltersAndMasks();
  CAN.toggleRxBuffer0Acceptance(true, false);
  CAN.toggleRxBuffer1Acceptance(true, false);
  CAN.setMode(NORMAL); // We send ACK's to every message that it is received.
}

// Send command for normal or simulated objects
void requestObjects(){
  byte data[] = { 0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0x00 };
  if (simulated) data[4] = 0x08;
  sendCommand(data);
}

// Send the command that set the installation height in meters
// Accepted values: 1...10 meters
void setInstallationHeight() {
  byte data[] = { 0xFF,0x01,0x00,0xAC,installationHeight,0x00,0x00,0x00 };
  sendCommand(data);
}

// Function for send a command to the radar
void sendCommand(byte data[]) {
  uint32_t frame_id = 0x3F2;
  byte length = 8;
  CAN.load_ff_0(length, &frame_id, data, false);
}

// Checks if a second has passed
//void checkIfSecondPassed() {
//  uint32_t time = millis();
//  if (time > oldTime + 1000) {
//    framesSentThisSecond = 0;
//    oldTime = time;
//  }
//}

// Checksum calculation for the frame
char checksum(const char * str) {
  uint8_t sum = 0;
  for (uint8_t i = 0; i < FRAME_LENGTH_WITHOUT_CRC; i++) {
    sum += (uint8_t)str[i];
  }
  return 'H' + sum % 16;
}

// Runtime loop
void loop() {
  // Check if it's time to perform the action
  //checkIfSecondPassed();

  if (valsReceived) {
    // First we send a frame in order to wake up the sensor
    if (wakeUpCall) {
      requestObjects();
      wakeUpCall = 0;  
    }
  
    // Read the status of the RX buffers
    rxStatus = CAN.readStatus();
  
    // Depending on the RX status, read the correct buffer
    bool buffer0 = CAN.buffer0DataWaiting();
    bool buffer1 = CAN.buffer1DataWaiting();
    if (buffer0) {
      CAN.readDATA_ff_0(&length,frameData,&frameID, &extended, &filter);
    } 
    else if (buffer1) {
      CAN.readDATA_ff_1(&length,frameData,&frameID, &extended, &filter);
    }
  
    // Now that we have read the correct buffer, filter the data and send it through serial
    if (buffer0 || buffer1) {
  
      // Once sensor booted, send the request to the sensor to start detecting normal or simulated traffic 
      if(sendRequest && frameID == 0x2FF){
        requestObjects();
        setInstallationHeight();
        sendRequest = 0;
      }
  
      //if (frameID == 0x2FF) {
      //Serial.print("New cycle!\n"); 
      //}
  
      // Moving object detected
      if ((frameID >= 0x510 && frameID <= 0x54F) ||
        (frameID >= 0x590 && frameID <= 0x5CF) ||
        (frameID >= 0x610 && frameID <= 0x64F) ||
        (frameID >= 0x690 && frameID <= 0x6CF))
      {
  
        // Generate and send serial frame with header and checksum
        byte objectID = frameData[7] >> 2;
        byte objectLength = frameData[7] << 14 >> 8 | frameData[6] >> 2;
        int16_t yVelocity = frameData[6] << 14 >> 5 | frameData[5] << 1 | frameData[4] >> 7;
        int16_t xVelocity = frameData[4] << 9 >> 5 | frameData[3] >> 4;
        yVelocity = yVelocity < 0 ? -(1024 + yVelocity) : 1024 - yVelocity;
        xVelocity = xVelocity < 0 ? -(1024 + xVelocity) : 1024 - xVelocity;
        int16_t yPosition = (frameData[3] << 12 >> 2 | frameData[2] << 2 | frameData[1] >> 6) + 8192;
        int16_t xPosition = (frameData[1] << 10 >> 2 | frameData[0]) + 8192;
  
        char frame[FRAME_LENGTH_WITHOUT_CRC];
        sprintf(frame,"G%02X%02X%04X%04X%04X%04X",objectID,objectLength,xPosition,yPosition,xVelocity,yVelocity);
        Serial.print(frame);
        Serial.print(checksum(frame));
      }
    }    
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
