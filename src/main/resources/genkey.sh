#!/usr/bin/env bash

# use to generate self signed certificate
keytool -genkey -keyalg RSA -alias google-speech-demo-key -keystore google-speech-demo.jks -storepass abcd1234 -validity 360 -keysize 2048
