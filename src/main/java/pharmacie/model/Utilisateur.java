package pharmacie.model;

import java.time.LocalDateTime;

public class Utilisateur extends Personne {
    private String login;
    private String motDePasseHash;
    private byte[] photo;
    private Role role = Role.EMPLOYE;
    private boolean actif = true;
    private LocalDateTime dateCreation;

    public Utilisateur() {}

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean estAdmin() { return role == Role.ADMIN; }
}
