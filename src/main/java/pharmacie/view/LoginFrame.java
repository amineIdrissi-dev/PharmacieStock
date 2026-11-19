package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pharmacie.model.Utilisateur;
import pharmacie.service.AuthService;
import pharmacie.util.Config;

/** Fenêtre d'authentification : première fenêtre affichée. */
public class LoginFrame extends JFrame {
    private final JTextField champLogin = new JTextField(18);
    private final JPasswordField champMotDePasse = new JPasswordField(18);
    private final JLabel messageErreur = new JLabel(" ");
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("Connexion - Gestion de stock pharmacie");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        String nomPharmacie = Config.get("pharmacie.nom");
        JLabel titre = new JLabel(nomPharmacie.isEmpty() ? "Pharmacie" : nomPharmacie, SwingConstants.CENTER);
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 20f));
        JLabel sousTitre = new JLabel("Gestion de stock", SwingConstants.CENTER);

        JCheckBox afficher = new JCheckBox("Afficher le mot de passe");
        char masque = champMotDePasse.getEchoChar();
        afficher.addActionListener(e -> champMotDePasse.setEchoChar(afficher.isSelected() ? (char) 0 : masque));

        JButton boutonConnexion = new JButton("Se connecter");
        boutonConnexion.addActionListener(e -> connecter());
        getRootPane().setDefaultButton(boutonConnexion);

        messageErreur.setForeground(new Color(198, 40, 40));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        form.add(titre, g);
        g.gridy = 1;
        form.add(sousTitre, g);
        g.gridwidth = 1;
        g.gridy = 2; form.add(new JLabel("Nom d'utilisateur"), g);
        g.gridx = 1; form.add(champLogin, g);
        g.gridx = 0; g.gridy = 3; form.add(new JLabel("Mot de passe"), g);
        g.gridx = 1; form.add(champMotDePasse, g);
        g.gridy = 4; form.add(afficher, g);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        form.add(messageErreur, g);
        g.gridy = 6;
        form.add(boutonConnexion, g);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        add(form, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void connecter() {
        String login = champLogin.getText();
        String motDePasse = new String(champMotDePasse.getPassword());
        if (login.isBlank() || motDePasse.isEmpty()) {
            messageErreur.setText("Saisissez votre identifiant et votre mot de passe.");
            return;
        }
        try {
            Utilisateur u = authService.connecter(login, motDePasse);
            if (u == null) {
                messageErreur.setText("Identifiant ou mot de passe incorrect.");
                champMotDePasse.setText("");
                return;
            }
            new MenuFrame(u).setVisible(true);
            dispose();
        } catch (Exception ex) {
            UiUtil.erreur(this, ex);
        }
    }
}
