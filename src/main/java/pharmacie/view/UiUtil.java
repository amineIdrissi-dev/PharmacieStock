package pharmacie.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.IntFunction;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Fonctions communes aux fenêtres : messages, tableaux, formulaires, photos, formats. */
public final class UiUtil {
    public static final Color ORANGE = new Color(255, 224, 178);   // stock bas
    public static final Color ROUGE = new Color(255, 205, 210);    // quantité 0 ou expiration proche
    public static final Color VERT = new Color(200, 230, 201);     // en stock
    private static final long TAILLE_MAX_PHOTO = 2L * 1024 * 1024; // 2 Mo

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UiUtil() {}

    // ---- Messages ----

    public static void erreur(Component parent, Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (e instanceof SQLException && message != null) {
            if (message.contains("suitable driver")) {
                message = "Pilote JDBC SQL Server introuvable.\nPlacez le fichier mssql-jdbc-*.jar dans le dossier lib.";
            } else if (message.contains("REFERENCE")) {
                message = "Suppression impossible : cet élément est utilisé ailleurs (ventes, médicaments...).";
            } else if (message.contains("UNIQUE") || message.contains("duplicate")) {
                message = "Cette valeur existe déjà (login, code-barres ou libellé en double).";
            }
        }
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmer(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // ---- Tableaux ----

    /** Modèle de tableau non modifiable ; le type de chaque colonne est déduit de la première ligne. */
    public static DefaultTableModel modele(String... colonnes) {
        return new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int colonne) {
                for (int i = 0; i < getRowCount(); i++) {
                    Object v = getValueAt(i, colonne);
                    if (v != null) {
                        return v.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    public static JTable table(DefaultTableModel modele) {
        return tableColoree(modele, i -> null);
    }

    /** Tableau dont chaque ligne peut recevoir une couleur de fond (null = couleur normale). */
    public static JTable tableColoree(DefaultTableModel modele, IntFunction<Color> couleurLigne) {
        JTable t = new JTable(modele) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int ligne, int colonne) {
                Component c = super.prepareRenderer(renderer, ligne, colonne);
                if (!isRowSelected(ligne)) {
                    Color couleur = couleurLigne.apply(ligne);
                    c.setBackground(couleur == null ? getBackground() : couleur);
                }
                return c;
            }
        };
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(26);
        t.getTableHeader().setReorderingAllowed(false);
        t.setFillsViewportHeight(true);
        return t;
    }

    // ---- Formulaires ----

    public static JPanel formulaire(String[] libelles, JComponent[] champs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < libelles.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            p.add(new JLabel(libelles[i]), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            p.add(champs[i], g);
        }
        return p;
    }

    public static JButton bouton(String texte, ActionListener action) {
        JButton b = new JButton(texte);
        b.addActionListener(action);
        return b;
    }

    // ---- Photos ----

    public static ImageIcon icone(byte[] donnees, int taille) {
        if (donnees == null || donnees.length == 0) {
            return null;
        }
        Image image = new ImageIcon(donnees).getImage().getScaledInstance(taille, taille, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /** Ouvre un sélecteur de fichier JPG/PNG ; retourne null si l'utilisateur annule. */
    public static byte[] choisirPhoto(Component parent) throws IOException {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images JPG ou PNG", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File f = fc.getSelectedFile();
        if (f.length() > TAILLE_MAX_PHOTO) {
            throw new IOException("La photo dépasse 2 Mo.");
        }
        return Files.readAllBytes(f.toPath());
    }

    // ---- Formats ----

    public static String argent(BigDecimal montant) {
        return montant == null ? "" : montant.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(JOUR);
    }

    public static String date(LocalDateTime d) {
        return d == null ? "" : d.format(JOUR_HEURE);
    }

    /** Lit une date jj/mm/aaaa ; texte vide = null. */
    public static LocalDate lireDate(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texte.trim(), JOUR);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date invalide : utilisez le format jj/mm/aaaa.");
        }
    }

    /** Lit un montant (virgule ou point) ; texte vide = null. */
    public static BigDecimal lireMontant(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        try {
            BigDecimal v = new BigDecimal(texte.trim().replace(',', '.'));
            if (v.signum() < 0) {
                throw new IllegalArgumentException("Un montant ne peut pas être négatif.");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Montant invalide : " + texte);
        }
    }

    public static String texte(String valeur) {
        return valeur == null ? "" : valeur;
    }
}
