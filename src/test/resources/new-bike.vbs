Dim http, json, url
url = "http://localhost/api/v1/magat/"
json = "{""serial"":333,""color"":""101010"",""price"":540.99}"

Set http = CreateObject("MSXML2.ServerXMLHTTP.6.0")
http.Open "POST", url, False
http.setRequestHeader "Content-Type", "application/json"
http.send json

WScript.Echo "Inviato! Risposta del server: " & http.responseText