package pharmacie.model;

import java.math.BigDecimal;

public class LigneVente {
    private int id;
    private int idVente;
    private Medicament medicament;
    private int quantite;
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    public LigneVente() {}

    public LigneVente(Medicament medicament, int quantite) {
        this.medicament = medicament;
        this.quantite = quantite;
        this.prixUnitaire = medicament.getPrixUnitaire();   // prix figé au moment de la vente
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdVente() { return idVente; }
    public void setIdVente(int idVente) { this.idVente = idVente; }
    public Medicament getMedicament() { return medicament; }
    public void setMedicament(Medicament medicament) { this.medicament = medicament; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public BigDecimal getSousTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
