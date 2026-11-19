package pharmacie.model;

import java.time.LocalDateTime;

public class Acheteur extends Personne {
    private String telephone;
    private String email;
    private String adresse;
    private LocalDateTime dateInscription;

    public Acheteur() {}

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
}
