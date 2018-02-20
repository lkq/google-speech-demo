## Google Speech API Demo

a demo of using Web API to capture audio from microphone and send it to google speech api for voice recognition


A Running Instance:

[https://liukangquan.com:1025](https://liukangquan.com:1025)

#### Run It Locally
provide your own speech [API Key](https://cloud.google.com/docs/authentication/api-keys) by environment variable or jvm argument as:

    GOOGLE_SPEECH_API_KEY=<your api key>


#### Running on Server
As Chrome not allow using microphone by unsecured remote site, we need to enable https when running on server.

besides the GOOGLE_SPEECH_API_KEY, you also need to provide your keystore file and keystore password by environment variables or jvm arguments as:

    javax.net.ssl.keyStore=<keystore file path>
    javax.net.ssl.keyStorePassword=<keystore password or none>

a sample self-signed keystore generation script is under resources/genkey.sh

