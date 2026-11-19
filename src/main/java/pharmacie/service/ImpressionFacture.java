package pharmacie.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import pharmacie.model.Facture;
import pharmacie.model.LigneVente;
import pharmacie.model.Vente;
import pharmacie.util.Config;

/** Imprime une facture avec l'API standard java.awt.print (aucune bibliothèque à ajouter). */
public class ImpressionFacture implements Printable {
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Facture facture;
    private final Vente vente;

    public ImpressionFacture(Facture facture) {
        this.facture = facture;
        this.vente = facture.getVente();
    }

    public void imprimer() throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Facture " + facture.getNumero());
        job.setPrintable(this);
        if (job.printDialog()) {          // choix de l'imprimante, ou « Microsoft Print to PDF »
            job.print();
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) {
        if (page > 0) {
            return NO_SUCH_PAGE;                           // facture sur une seule page
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        g2.setColor(Color.BLACK);
        int largeur = (int) pf.getImageableWidth();
        int y = 20;

        // En-tête de la pharmacie
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(Config.get("pharmacie.nom"), 0, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        y += 16;
        g2.drawString(Config.get("pharmacie.adresse") + " - Tél. " + Config.get("pharmacie.telephone"), 0, y);

        // Numéro, date, client
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        y += 30;
        g2.drawString("Facture n° " + facture.getNumero()
                + (facture.estAnnulee() ? "   (ANNULÉE)" : ""), 0, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        y += 15;
        g2.drawString("Date : " + facture.getDateFacture().format(FORMAT_DATE), 0, y);
        y += 15;
        g2.drawString("Client : " + vente.getNomClient(), 0, y);
        y += 15;
        g2.drawString("Vendeur : " + vente.getUtilisateur().getNomComplet(), 0, y);

        // Lignes de la vente
        int colQte = largeur - 230, colPu = largeur - 170, colTotal = largeur - 80;
        y += 25;
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("Médicament", 0, y);
        g2.drawString("Qté", colQte, y);
        g2.drawString("P.U. HT", colPu, y);
        g2.drawString("Sous-total", colTotal, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        y += 5;
        g2.drawLine(0, y, largeur, y);
        for (LigneVente l : vente.getLignes()) {
            y += 15;
            String nom = l.getMedicament().getNom()
                    + (l.getMedicament().getDosage() == null ? "" : " " + l.getMedicament().getDosage());
            g2.drawString(nom, 0, y);
            g2.drawString(String.valueOf(l.getQuantite()), colQte, y);
            g2.drawString(argent(l.getPrixUnitaire()), colPu, y);
            g2.drawString(argent(l.getSousTotal()), colTotal, y);
        }
        y += 8;
        g2.drawLine(0, y, largeur, y);

        // Totaux
        y += 18;
        g2.drawString("Total HT : " + argent(facture.getMontantHt()), colPu, y);
        y += 15;
        g2.drawString("TVA (" + facture.getTauxTva().stripTrailingZeros().toPlainString() + " %) : "
                + argent(facture.getMontantTva()), colPu, y);
        y += 15;
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("Total TTC : " + argent(facture.getMontantTtc()), colPu, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        y += 30;
        g2.drawString("Paiement : " + libellePaiement(facture.getModePaiement()), 0, y);
        y += 25;
        g2.drawString("Merci de votre visite.", 0, y);
        return PAGE_EXISTS;
    }

    public static String argent(BigDecimal montant) {
        return montant == null ? "0.00" : montant.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String libellePaiement(String code) {
        return switch (code == null ? "" : code) {
            case "ESPECES" -> "Espèces";
            case "CARTE" -> "Carte bancaire";
            case "CHEQUE" -> "Chèque";
            default -> code;
        };
    }
}
