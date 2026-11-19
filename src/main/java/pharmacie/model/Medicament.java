package pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicament {
    private int id;
    private String codeBarre;
    private String nom;
    private String forme;
    private String dosage;
    private byte[] photo;
    private BigDecimal prixUnitaire = BigDecimal.ZERO;
    private int quantite;
    private int seuilMin = 10;
    private LocalDate dateExpiration;
    private boolean ordonnanceRequise;
    private Categorie categorie;

    public Medicament() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodeBarre() { return codeBarre; }
    public void setCodeBarre(String codeBarre) { this.codeBarre = codeBarre; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public int getSeuilMin() { return seuilMin; }
    public void setSeuilMin(int seuilMin) { this.seuilMin = seuilMin; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public boolean isOrdonnanceRequise() { return ordonnanceRequise; }
    public void setOrdonnanceRequise(boolean ordonnanceRequise) { this.ordonnanceRequise = ordonnanceRequise; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public boolean estDisponible() { return quantite > 0; }

    public boolean estStockBas() { return quantite > 0 && quantite <= seuilMin; }

    /** Expire dans moins de 30 jours (ou est déjà expiré). */
    public boolean expireBientot() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now().plusDays(30));
    }

    @Override
    public String toString() {
        String detail = dosage == null || dosage.isBlank() ? "" : " " + dosage;
        return nom + detail + " (stock : " + quantite + ")";
    }
}
