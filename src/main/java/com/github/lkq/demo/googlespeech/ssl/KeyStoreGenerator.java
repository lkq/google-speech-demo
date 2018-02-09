package com.github.lkq.demo.googlespeech.ssl;

import com.github.lkq.demo.googlespeech.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;

/**
 * generate the self signed keystore on the fly
 * Pending Implementation
 */
public class KeyStoreGenerator {

    private static Logger logger = LoggerFactory.getLogger(KeyStoreGenerator.class);

    public static final String KEY_STORE_PWD = "abcd1234";

    public String getKeyStore() {
        try {
            String jarPath = Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File keyStoreFile = new File(new File(jarPath).getParent(), "google-speech-demo.jks");
            KeyStore keyStore = KeyStore.getInstance("jks");
            boolean shouldGenerateKeyStore = true;
            if (keyStoreFile.exists()) {
                try {
                    keyStore.load(new FileInputStream(keyStoreFile), KEY_STORE_PWD.toCharArray());
                    // do not re-generate keystore if a valid keystore exists
                    shouldGenerateKeyStore = false;
                } catch (Throwable t) {
                    logger.error("keystore exists but not valid, deleting and re-generate", t);
                    if (!keyStoreFile.delete()) {
                        logger.error("failed to delete keystore file {}", keyStoreFile);
                    }
                }
            }
            if (shouldGenerateKeyStore) {
                keyStore.load(null, null);
                keyStore.setCertificateEntry("google-speech-demo-key", genCert());
                keyStore.store(new FileOutputStream(keyStoreFile), KEY_STORE_PWD.toCharArray());
                logger.info("generated keystore: {}", keyStoreFile);
            }
            return keyStoreFile.getAbsolutePath();
        } catch (Throwable t) {
            logger.error("failed to get keystore", t);
            return null;
        }
    }

    private Certificate genCert() throws NoSuchProviderException, NoSuchAlgorithmException {

        //TODO: generate the certificate
        // references:
        // https://stackoverflow.com/questions/925377/generate-certificates-public-and-private-keys-with-java?rq=1
        // http://www.bouncycastle.org/latest_releases.html
        throw new RuntimeException("Pending Implementation");
    }
}
