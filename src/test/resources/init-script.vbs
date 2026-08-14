Option Explicit

Dim http, url, body, responseMsg

Set http = CreateObject("MSXML2.ServerXMLHTTP")

url = "http://127.0.0.1:80/api/v1/magat/admin/init"
body = "[{""path"":""MAGAZZINO/ZONA_A/BIN_01/"",""capacity"":10,""products"":[]}]"

WScript.Echo "Invio richiesta POST in corso..."

http.open "POST", url, False
http.setRequestHeader "Content-Type", "application/json"
http.setRequestHeader "X-Admin-Key", "secret123"

http.send body

WScript.Echo ""
WScript.Echo "================ RESPONSE ================"
WScript.Echo "HTTP STATUS: " & http.status

' Evita il crash decodificando in modo sicuro
On Error Resume Next
responseMsg = http.responseText
If Err.Number <> 0 Then
    ' Se responseText fallisce, recuperiamo il messaggio nativo HTTP
    responseMsg = http.statusText
End If
On Error GoTo 0

WScript.Echo "RESPONSE BODY: " & responseMsg
WScript.Echo "========================================="