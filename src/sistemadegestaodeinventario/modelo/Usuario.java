package sistemadegestaodeinventario.modelo;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Usuario {

    public enum Perfil {
        ADMIN,
        GESTOR_STOCK,
        VENDEDOR
    }

    private static final String HASH_PREFIXO = "PBKDF2";
    private static final String HASH_ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int HASH_ITERACOES = 65536;
    private static final int HASH_TAMANHO_BITS = 256;
    private static final int SAL_TAMANHO_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String emailOuTelefone;
    private String senha;
    private Perfil perfil;
    private String idLoja;

    public Usuario(String emailOuTelefone, String senha) {
        this(emailOuTelefone, senha, Perfil.VENDEDOR);
    }

    public Usuario(String emailOuTelefone, String senha, Perfil perfil) {
        this(emailOuTelefone, senha, perfil, null);
    }

    public Usuario(String emailOuTelefone, String senha, Perfil perfil, String idLoja) {
        setEmailOuTelefone(emailOuTelefone);
        setSenha(senha);
        setPerfil(perfil);
        setIdLoja(idLoja);
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
        this.senha = senhaCriptografada(senha) ? senha : criptografarSenha(senha);
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil == null ? Perfil.VENDEDOR : perfil;
    }

    public String getIdLoja() {
        return idLoja;
    }

    public void setIdLoja(String idLoja) {
        this.idLoja = idLoja == null || idLoja.trim().isEmpty() ? "" : idLoja.trim();
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public boolean isGestorStock() {
        return perfil == Perfil.GESTOR_STOCK;
    }

    public boolean isVendedor() {
        return perfil == Perfil.VENDEDOR;
    }

    public boolean autenticar(String credencial, String senha) {
        return coincideCredencial(credencial) && senhaCorreta(senha);
    }

    public boolean senhaCorreta(String senhaInformada) {
        if (senhaInformada == null) {
            return false;
        }
        if (!senhaCriptografada(this.senha)) {
            return this.senha.equals(senhaInformada);
        }
        String[] partes = this.senha.split("\\$", -1);
        if (partes.length != 4) {
            return false;
        }
        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashGuardado = Base64.getDecoder().decode(partes[3]);
            byte[] hashInformado = gerarHash(senhaInformada.toCharArray(), sal, iteracoes, hashGuardado.length * 8);
            return comparacaoConstante(hashGuardado, hashInformado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean coincideCredencial(String credencial) {
        return this.emailOuTelefone.equals(credencial == null ? null : credencial.trim());
    }

    private static boolean senhaCriptografada(String senha) {
        if (senha == null) {
            return false;
        }
        String[] partes = senha.split("\\$", -1);
        if (partes.length != 4 || !HASH_PREFIXO.equals(partes[0])) {
            return false;
        }
        try {
            Integer.parseInt(partes[1]);
            Base64.getDecoder().decode(partes[2]);
            Base64.getDecoder().decode(partes[3]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String criptografarSenha(String senha) {
        byte[] sal = new byte[SAL_TAMANHO_BYTES];
        RANDOM.nextBytes(sal);
        byte[] hash = gerarHash(senha.toCharArray(), sal, HASH_ITERACOES, HASH_TAMANHO_BITS);
        return HASH_PREFIXO + "$" + HASH_ITERACOES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private static byte[] gerarHash(char[] senha, byte[] sal, int iteracoes, int tamanhoBits) {
        try {
            KeySpec spec = new PBEKeySpec(senha, sal, iteracoes, tamanhoBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro ao criptografar a senha.", e);
        }
    }

    private static boolean comparacaoConstante(byte[] esperado, byte[] recebido) {
        if (esperado.length != recebido.length) {
            return false;
        }
        int diferenca = 0;
        for (int i = 0; i < esperado.length; i++) {
            diferenca |= esperado[i] ^ recebido[i];
        }
        return diferenca == 0;
    }

    @Override
    public String toString() {
        String loja = idLoja == null || idLoja.isEmpty() ? "" : " | loja=" + idLoja;
        return emailOuTelefone + " [" + perfil + "]" + loja;
    }
}
