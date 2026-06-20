package sistemadegestaodeinventario.modelo;

public class Usuario {

    public enum Perfil {
        ADMIN,
        NORMAL
    }

    private String emailOuTelefone;
    private String senha;
    private Perfil perfil;

    public Usuario(String emailOuTelefone, String senha) {
        this(emailOuTelefone, senha, Perfil.NORMAL);
    }

    public Usuario(String emailOuTelefone, String senha, Perfil perfil) {
        setEmailOuTelefone(emailOuTelefone);
        setSenha(senha);
        setPerfil(perfil);
    }

    public String getEmailOuTelefone() {
        return emailOuTelefone;
    }

    public void setEmailOuTelefone(String emailOuTelefone) {
        if (emailOuTelefone == null || emailOuTelefone.trim().isEmpty()) {
            throw new IllegalArgumentException("A credencial não pode estar vazia.");
        }
        this.emailOuTelefone = emailOuTelefone.trim();
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("A senha não pode estar vazia.");
        }
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil == null ? Perfil.NORMAL : perfil;
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public boolean autenticar(String credencial, String senha) {
        return this.emailOuTelefone.equals(credencial) && this.senha.equals(senha);
    }

    public boolean coincideCredencial(String credencial) {
        return this.emailOuTelefone.equals(credencial == null ? null : credencial.trim());
    }

    @Override
    public String toString() {
        return emailOuTelefone + " [" + perfil + "]";
    }
}
