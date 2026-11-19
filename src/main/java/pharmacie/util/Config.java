package pharmacie.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Lit le fichier config.properties placé à la racine du projet. */
public final class Config {
    private static final String FICHIER = "config.properties";
    private static final Properties PROPS = new Properties();

    static {
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(FICHIER), StandardCharsets.UTF_8)) {
            PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Fichier " + FICHIER + " introuvable dans le dossier de lancement.", e);
        }
    }

    private Config() {}

    public static String get(String cle) {
        return PROPS.getProperty(cle, "").trim();
    }

    public static BigDecimal getDecimal(String cle) {
        String valeur = get(cle).replace(',', '.');
        return valeur.isEmpty() ? BigDecimal.ZERO : new BigDecimal(valeur);
    }
}
