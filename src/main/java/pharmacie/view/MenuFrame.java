package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pharmacie.model.Utilisateur;
import pharmacie.service.AuthService;
import pharmacie.util.Config;
import pharmacie.util.ConnexionBD;

/** Fenêtre principale : en-tête (photo, nom, déconnexion), menu à gauche, rubrique au centre. */
public class MenuFrame extends JFrame {
    private final CardLayout cartes = new CardLayout();
    private final JPanel centre = new JPanel(cartes);
    private final Map<String, JComponent> rubriques = new LinkedHashMap<>();

    public MenuFrame(Utilisateur u) {
        super("Gestion de stock - " + Config.get("pharmacie.nom"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConnexionBD.fermer();
                System.exit(0);
            }
        });

        rubriques.put("Tableau de bord", new DashboardPanel());
        rubriques.put("Stocks disponibles", new StocksPanel());
        rubriques.put("Produits vendus", new VentesPanel());
        rubriques.put("Produits indisponibles", new IndisponiblesPanel());
        rubriques.put("Acheteurs", new AcheteursPanel());
        rubriques.put("Catégories et fournisseurs", new ReferentielsPanel());
        if (u.estAdmin()) {
            rubriques.put("Utilisateurs", new UtilisateursPanel());
        }

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 6));
        menu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (Map.Entry<String, JComponent> r : rubriques.entrySet()) {
            centre.add(r.getValue(), r.getKey());
            JButton b = new JButton(r.getKey());
            b.addActionListener(e -> afficher(r.getKey()));
            menu.add(b);
        }
        JPanel gauche = new JPanel(new BorderLayout());
        gauche.add(menu, BorderLayout.NORTH);

        add(enTete(u), BorderLayout.NORTH);
        add(gauche, BorderLayout.WEST);
        add(centre, BorderLayout.CENTER);

        setSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        afficher("Tableau de bord");
    }

    private JPanel enTete(Utilisateur u) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0, 121, 107));
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titre = new JLabel("Gestion de stock - " + Config.get("pharmacie.nom"));
        titre.setForeground(Color.WHITE);
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 16f));

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);
        ImageIcon photo = UiUtil.icone(u.getPhoto(), 40);
        JLabel nom = new JLabel(u.getNomComplet() + " (" + (u.estAdmin() ? "Administrateur" : "Employé") + ")", photo,
                JLabel.LEFT);
        nom.setForeground(Color.WHITE);
        JButton deconnexion = new JButton("Déconnexion");
        deconnexion.addActionListener(e -> {
            AuthService.deconnecter();
            dispose();
            new LoginFrame().setVisible(true);
        });
        droite.add(nom);
        droite.add(deconnexion);

        p.add(titre, BorderLayout.WEST);
        p.add(droite, BorderLayout.EAST);
        return p;
    }

    private void afficher(String nom) {
        JComponent panneau = rubriques.get(nom);
        if (panneau instanceof Rafraichissable r) {
            r.rafraichir();
        }
        cartes.show(centre, nom);
    }
}
