package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pharmacie.dao.UtilisateurDAO;
import pharmacie.model.Role;
import pharmacie.model.Utilisateur;
import pharmacie.util.HashUtil;

/** Formulaire d'ajout ou de modification d'un compte utilisateur (avec photo). */
public class UtilisateurDialog extends JDialog {
    private final Utilisateur utilisateur;
    private final boolean nouveau;
    private boolean enregistre;
    private byte[] photo;

    private final JTextField nom = new JTextField(20);
    private final JTextField prenom = new JTextField(20);
    private final JTextField login = new JTextField(20);
    private final JPasswordField motDePasse = new JPasswordField(20);
    private final JComboBox<Role> role = new JComboBox<>(Role.values());
    private final JCheckBox actif = new JCheckBox("Compte actif", true);
    private final JLabel apercuPhoto = new JLabel("Aucune photo", SwingConstants.CENTER);

    public UtilisateurDialog(Window parent, Utilisateur u) {
        super(parent, u == null ? "Nouvel utilisateur" : "Modifier l'utilisateur", ModalityType.APPLICATION_MODAL);
        this.nouveau = u == null;
        this.utilisateur = u == null ? new Utilisateur() : u;
        apercuPhoto.setPreferredSize(new Dimension(110, 110));

        if (!nouveau) {
            nom.setText(u.getNom());
            prenom.setText(u.getPrenom());
            login.setText(u.getLogin());
            role.setSelectedItem(u.getRole());
            actif.setSelected(u.isActif());
            afficherPhoto(u.getPhoto());
        }

        JPanel form = UiUtil.formulaire(
                new String[] {"Nom *", "Prénom *", "Login *",
                              nouveau ? "Mot de passe *" : "Nouveau mot de passe (vide = inchangé)", "Rôle", ""},
                new JComponent[] {nom, prenom, login, motDePasse, role, actif});

        JPanel panneauPhoto = new JPanel(new BorderLayout(4, 4));
        panneauPhoto.add(apercuPhoto, BorderLayout.CENTER);
        JPanel boutonsPhoto = new JPanel(new FlowLayout());
        boutonsPhoto.add(UiUtil.bouton("Choisir...", e -> choisirPhoto()));
        boutonsPhoto.add(UiUtil.bouton("Retirer", e -> afficherPhoto(null)));
        panneauPhoto.add(boutonsPhoto, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(UiUtil.bouton("Enregistrer", e -> enregistrer()));
        actions.add(UiUtil.bouton("Annuler", e -> dispose()));

        add(form, BorderLayout.CENTER);
        add(panneauPhoto, BorderLayout.EAST);
        add(actions, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean afficher() {
        setVisible(true);
        return enregistre;
    }

    private void choisirPhoto() {
        try {
            byte[] donnees = UiUtil.choisirPhoto(this);
            if (donnees != null) {
                afficherPhoto(donnees);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void afficherPhoto(byte[] donnees) {
        photo = donnees;
        apercuPhoto.setIcon(UiUtil.icone(donnees, 100));
        apercuPhoto.setText(donnees == null ? "Aucune photo" : "");
    }

    private void enregistrer() {
        String mdp = new String(motDePasse.getPassword());
        if (nom.getText().isBlank() || prenom.getText().isBlank() || login.getText().isBlank()) {
            UiUtil.info(this, "Le nom, le prénom et le login sont obligatoires.");
            return;
        }
        if (nouveau && mdp.length() < 6) {
            UiUtil.info(this, "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!nouveau && !mdp.isEmpty() && mdp.length() < 6) {
            UiUtil.info(this, "Le nouveau mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        utilisateur.setNom(nom.getText().trim());
        utilisateur.setPrenom(prenom.getText().trim());
        utilisateur.setLogin(login.getText().trim());
        utilisateur.setRole((Role) role.getSelectedItem());
        utilisateur.setActif(actif.isSelected());
        utilisateur.setPhoto(photo);
        utilisateur.setMotDePasseHash(mdp.isEmpty() ? null : HashUtil.sha256(mdp));
        try {
            UtilisateurDAO dao = new UtilisateurDAO();
            if (nouveau) {
                dao.ajouter(utilisateur);
            } else {
                dao.modifier(utilisateur);
            }
            enregistre = true;
            dispose();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
