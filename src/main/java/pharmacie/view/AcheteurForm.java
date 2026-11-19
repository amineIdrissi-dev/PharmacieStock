package pharmacie.view;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import pharmacie.dao.AcheteurDAO;
import pharmacie.model.Acheteur;

/** Formulaire d'ajout ou de modification d'un acheteur. */
public final class AcheteurForm {

    private AcheteurForm() {}

    /** a = null : nouvel acheteur. Retourne l'acheteur enregistré, ou null si annulé. */
    public static Acheteur editer(Component parent, Acheteur a) {
        Acheteur acheteur = a == null ? new Acheteur() : a;
        JTextField nom = new JTextField(UiUtil.texte(acheteur.getNom()), 22);
        JTextField prenom = new JTextField(UiUtil.texte(acheteur.getPrenom()), 22);
        JTextField telephone = new JTextField(UiUtil.texte(acheteur.getTelephone()), 22);
        JTextField email = new JTextField(UiUtil.texte(acheteur.getEmail()), 22);
        JTextField adresse = new JTextField(UiUtil.texte(acheteur.getAdresse()), 22);

        while (true) {
            int choix = JOptionPane.showConfirmDialog(parent,
                    UiUtil.formulaire(new String[] {"Nom *", "Prénom *", "Téléphone", "Email", "Adresse"},
                            new JComponent[] {nom, prenom, telephone, email, adresse}),
                    a == null ? "Nouvel acheteur" : "Modifier l'acheteur",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choix != JOptionPane.OK_OPTION) {
                return null;
            }
            if (nom.getText().isBlank() || prenom.getText().isBlank()) {
                UiUtil.info(parent, "Le nom et le prénom sont obligatoires.");
                continue;
            }
            acheteur.setNom(nom.getText().trim());
            acheteur.setPrenom(prenom.getText().trim());
            acheteur.setTelephone(telephone.getText().trim());
            acheteur.setEmail(email.getText().trim());
            acheteur.setAdresse(adresse.getText().trim());
            try {
                AcheteurDAO dao = new AcheteurDAO();
                if (a == null) {
                    dao.ajouter(acheteur);
                } else {
                    dao.modifier(acheteur);
                }
                return acheteur;
            } catch (Exception e) {
                UiUtil.erreur(parent, e);
                return null;
            }
        }
    }
}
