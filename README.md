```
___________.__                 _________                       
\__    ___/|__| _____   ____  /   _____/__.__. ____   ____     
  |    |   |  |/     \_/ __ \ \_____  <   |  |/    \_/ ___\    
  |    |   |  |  Y Y  \  ___/ /        \___  |   |  \  \___    
  |____|   |__|__|_|  /\___  >_______  / ____|___|  /\___  >   
                    \/     \/        \/\/         \/     \/    
   _____          __                         __                
  /  _  \  __ ___/  |_  ____   _____ _____ _/  |_  ___________ 
 /  /_\  \|  |  \   __\/  _ \ /     \\__  \\   __\/  _ \_  __ \
/    |    \  |  /|  | (  <_> )  Y Y  \/ __ \|  | (  <_> )  | \/
\____|__  /____/ |__|  \____/|__|_|  (____  /__|  \____/|__|   
        \/                         \/     \/                                                                                      
```
#  JIRA to BambooHR time sync automator
Automate the manual chore of having to time register in multiple systems.

This utility automatically copy JIRA time registrations to BambooHR

It can also generate reports comparing time registration in JIRA with BambooHR: 
```
Date					JIRA	Bamboo
2025-01-01	Wednesday	Vacation
2025-01-02	Thursday 	7.4		7.4
2025-01-03	Friday   	7.4		7.4
2025-01-04	Saturday 	Weekend
2025-01-05	Sunday   	Weekend
2025-01-06	Monday   	7.4		7.4
...
```
It automatically detect vacation plans + company holidays registered in BambooHRs.

## Logic / Flow
1. Retrieve time registrations from JIRA
2. Log time registrations in BambooHR

## Requirements to configure
- A personal JIRA API key
- Your BambooHR employee number
- A BambooHR API key with access to your time registrations + time off requests
- A Bamboo iCal URL to fetch company holidays

## Build + Configuration
It is assumed you already have configured Java 21 / maven on your system for compiling. The build will create a native docker image.

Steps:
1. Configure - create a `application.properties` in `src/main/resources` directory. See `application-SAMPLE.properties`, rename, and fill in the blanks (JIRA username etc.).
2. Compile to native docker image `mvn -Pnative spring-boot:build-image`
3. Run - example: `docker run timesync-automator:1.0.0 dry-run today`

## Usage
```
docker run timesync-automator:1.0.0 dry-run <startDate> <endDate>
docker run timesync-automator:1.0.0 log-work <startDate> <endDate>
docker run timesync-automator:1.0.0 report <startDate> <endDate>
```

Examples:
```
docker run timesync-automator:1.0.0 log-work 2025-02-01 2025-02-28
docker run timesync-automator:1.0.0 report 2025-02-01 2025-02-28
```
You can still also run the jar file directly:
```
java -jar timesync-automator-1.0.0.jar dry-run 2025-02-01 2025-02-28 
```

