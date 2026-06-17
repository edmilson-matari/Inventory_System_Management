package sistemadegestaodeinventario.ui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import sistemadegestaodeinventario.modelo.ItemVenda;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;
import sistemadegestaodeinventario.modelo.Usuario;
import sistemadegestaodeinventario.modelo.Venda;
import sistemadegestaodeinventario.negocio.InventarioManager;
import sistemadegestaodeinventario.negocio.LojaManager;
import sistemadegestaodeinventario.negocio.UsuarioManager;
import sistemadegestaodeinventario.persistencia.GestorFicheiros;

public class Menu {
    private final Scanner scanner;
    private final GestorFicheiros gestorFicheiros;
    private final UsuarioManager usuarioManager;
    private final InventarioManager inventarioManager;
    private final LojaManager lojaManager;
    private Usuario usuarioAutenticado = null;

    public Menu() {
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        this.gestorFicheiros = new GestorFicheiros();
        this.usuarioManager = new UsuarioManager();
        this.inventarioManager = new InventarioManager();
        this.lojaManager = new LojaManager(inventarioManager);
        carregarDadosIniciais();
    }

    public void iniciar() {
        boolean ativo = true;
        while (ativo && usuarioAutenticado == null) {
            System.out.println("AUTENTICACAO DE USUARIO");
            System.out.println("1. Login");
            System.out.println("2. Cadastrar novo usuario");
            System.out.println("0. Sair");
            int op = lerInteiro("Escolha uma opcao: ");
            switch (op) {
                case 1 -> fazerLogin();
                case 2 -> cadastrarUsuario();
                case 0 -> ativo = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
        while (ativo && usuarioAutenticado != null) {
            exibirMenuPrincipal();
            int opcao = lerInteiro("Escolha uma opcao: ");
            switch (opcao) {
                case 1 -> menuLojas();
                case 2 -> menuProdutos();
                case 3 -> menuVendas();
                case 4 -> menuRelatorios();
                case 5 -> menuConfiguracao();
                case 0 -> {
                    salvarDados();
                    ativo = false;
                    System.out.println("Sistema encerrado com sucesso.");
                }
                default -> System.out.println("Opcao invalida.");
            }
        }
        scanner.close();
    }

    private void carregarDadosIniciais() {
        for (Usuario usuario : gestorFicheiros.carregarUsuarios()) {
            try {
                usuarioManager.adicionarUsuario(usuario);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (Loja loja : gestorFicheiros.carregarLojas()) {
            inventarioManager.adicionarLoja(loja);
        }
    }

    private void salvarDados() {
        gestorFicheiros.salvarUsuarios(usuarioManager.listarUsuarios());
        gestorFicheiros.salvarLojas(inventarioManager.getLojasOrdenadas());
    }

    private void fazerLogin() {
        String credencial = lerTexto("Email/Telefone: ");
        String senha = lerTexto("Senha: ");
        Usuario usuario = usuarioManager.autenticar(credencial, senha);
        if (usuario != null) {
            usuarioAutenticado = usuario;
            System.out.println("Login efetuado com sucesso.");
        } else {
            System.out.println("Credenciais invalidas.");
        }
    }

    private void cadastrarUsuario() {
        String credencial;
        while (true) {
            credencial = lerTexto("Digite email ou numero de telemovel: ");
            boolean isTelefone = credencial.matches("\\d{9}");
            boolean isEmail = credencial.contains("@");
            if (!isTelefone && !isEmail) {
                System.out.println("Digite um numero de telemovel valido (9 digitos) ou um email valido.");
                continue;
            }
            if (usuarioManager.buscarPorCredencial(credencial) != null) {
                System.out.println("Ja existe um utilizador com esta credencial.");
                continue;
            }
            break;
        }
        String senha;
        while (true) {
            senha = lerTexto("Digite uma senha: ");
            if (senha.length() < 8) {
                System.out.println("A senha deve ter pelo menos 8 caracteres.");
                continue;
            }
            break;
        }
        Usuario usuario = new Usuario(credencial, senha);
        usuarioManager.adicionarUsuario(usuario);
        usuarioAutenticado = usuario;
        salvarDados();
        System.out.println("Usuario cadastrado e autenticado com sucesso.");
    }

    private void exibirMenuPrincipal() {
        System.out.println("MENU PRINCIPAL");
        System.out.println("1. Gestao de Lojas");
        System.out.println("2. Gestao de Produtos");
        System.out.println("3. Registar Vendas");
        System.out.println("4. Relatorios e Consultas");
        System.out.println("5. Configuracao do Sistema");
        System.out.println("0. Sair");
    }

    private void menuLojas() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("GESTAO DE LOJAS");
            System.out.println("1. Adicionar Loja");
            System.out.println("2. Listar Todas as Lojas");
            System.out.println("3. Selecionar Loja");
            System.out.println("4. Ver Detalhes da Loja Ativa");
            System.out.println("5. Deletar Loja");
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> adicionarLoja();
                case 2 -> listarLojas();
                case 3 -> selecionarLoja();
                case 4 -> verDetalhesLojaAtiva();
                case 5 -> deletarLoja();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuProdutos() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("GESTAO DE PRODUTOS");
            System.out.println("1. Adicionar Produto a Loja");
            System.out.println("2. Listar Produtos da Loja");
            System.out.println("3. Consultar Stock de Produto");
            System.out.println("4. Aumentar Stock de Produto");
            System.out.println("5. Produtos com Stock Baixo");
            System.out.println("6. Deletar Produto");
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> adicionarProduto();
                case 2 -> listarProdutosDaLoja();
                case 3 -> consultarStockProduto();
                case 4 -> aumentarStockProduto();
                case 5 -> listarProdutosStockBaixo();
                case 6 -> deletarProduto();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuVendas() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("REGISTAR VENDAS");
            System.out.println("1. Iniciar Nova Venda");
            System.out.println("2. Ver Historico de Vendas");
            System.out.println("3. Valor Total de Vendas");
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> iniciarNovaVenda();
                case 2 -> verHistoricoVendas();
                case 3 -> verValorTotalVendas();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuRelatorios() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("RELATORIOS");
            System.out.println("1. Relatorio da Loja Atual");
            System.out.println("2. Inventario Completo");
            System.out.println("3. Relatorio do Sistema");
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> System.out.println(lojaManager.obterRelatorioLoja());
                case 2 -> relatorioInventarioCompleto();
                case 3 -> System.out.println(inventarioManager.obterRelatorioSistema());
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuConfiguracao() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("CONFIGURACAO");
            System.out.println("1. Informacoes do Sistema");
            System.out.println("2. Criar Backup de Dados");
            System.out.println("3. Dados de Teste");
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> mostrarInformacoesSistema();
                case 2 -> { gestorFicheiros.criarBackup(); System.out.println("Backup criado com sucesso."); }
                case 3 -> inserirDadosTeste();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void adicionarLoja() {
        String id = lerTexto("ID da loja: ");
        if (inventarioManager.existeLoja(id)) { System.out.println("Ja existe uma loja com este ID."); return; }
        String nome = lerTexto("Nome da loja: ");
        String morada = lerTexto("Morada: ");
        String telefone = lerTexto("Numero de telefone: ");
        inventarioManager.adicionarLoja(new Loja(id, nome, morada, telefone));
        salvarDados();
        System.out.println("Loja adicionada com sucesso.");
    }

    private void listarLojas() {
        List<Loja> lojas = inventarioManager.getLojasOrdenadas();
        if (lojas.isEmpty()) { System.out.println("Nenhuma loja registada."); return; }
        for (Loja loja : lojas) System.out.println(loja);
    }

    private void selecionarLoja() {
        String id = lerTexto("ID da loja a selecionar: ");
        if (lojaManager.selecionarLoja(id)) System.out.println("Loja selecionada com sucesso: " + lojaManager.getLojaAtual().getNome());
        else System.out.println("Loja nao encontrada.");
    }

    private void verDetalhesLojaAtiva() {
        if (lojaManager.getLojaAtual() == null) { System.out.println("Nenhuma loja selecionada."); return; }
        System.out.println(lojaManager.getLojaAtual());
        System.out.println("Produtos: " + lojaManager.getLojaAtual().listarProdutos().size());
        System.out.println("Vendas: " + lojaManager.getLojaAtual().listarVendas().size());
        System.out.println("Total vendido: " + String.format("%.2f", lojaManager.getLojaAtual().obterTotalVendas()));
    }

    private void adicionarProduto() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        Loja atual = lojaManager.getLojaAtual();
        Produto produto = new Produto(lerTexto("Codigo do produto: "), lerTexto("Nome: "), lerTexto("Descricao: "), lerDouble("Preco: "), lerInteiro("Quantidade em stock: "), lerInteiro("Quantidade minima permitida: "));
        atual.adicionarProduto(produto);
        salvarDados();
        System.out.println("Produto adicionado com sucesso.");
    }

    private void listarProdutosDaLoja() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        if (loja.listarProdutos().isEmpty()) { System.out.println("Sem produtos registados."); return; }
        for (Produto produto : loja.listarProdutos()) System.out.println(produto);
    }

    private void consultarStockProduto() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        Produto produto = loja.consultarProduto(lerTexto("ID do produto: "));
        if (produto == null) { System.out.println("Produto nao encontrado."); return; }
        System.out.println("Stock atual: " + produto.getQuantidadeEmStock());
    }

    private void aumentarStockProduto() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        Produto produto = loja.consultarProduto(lerTexto("ID do produto: "));
        if (produto == null) { System.out.println("Produto nao encontrado."); return; }
        produto.aumentarStock(lerInteiro("Quantidade a adicionar: "));
        salvarDados();
        System.out.println("Stock atualizado com sucesso.");
    }

    private void listarProdutosStockBaixo() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        boolean encontrado = false;
        for (Produto produto : loja.listarProdutos()) {
            if (produto.estaAbaixoMinimo()) { System.out.println(produto); encontrado = true; }
        }
        if (!encontrado) System.out.println("Nenhum produto com stock baixo.");
    }

    private void iniciarNovaVenda() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        if (loja.listarProdutos().isEmpty()) { System.out.println("A loja nao possui produtos."); return; }
        List<ItemVenda> itens = new ArrayList<>();
        while (true) {
            String idProduto = lerTexto("ID do produto (0 para terminar): ");
            if ("0".equals(idProduto)) break;
            Produto produto = loja.consultarProduto(idProduto);
            if (produto == null) { System.out.println("Produto nao encontrado."); continue; }
            int quantidade = lerInteiro("Quantidade: ");
            itens.add(new ItemVenda(produto.getIdProduto(), produto.getNome(), quantidade, produto.getPreco()));
        }
        if (itens.isEmpty()) { System.out.println("Venda cancelada."); return; }
        try {
            Venda venda = inventarioManager.registarVenda(loja.getIdLoja(), itens);
            salvarDados();
            System.out.println("Venda registada com sucesso: " + venda.getIdVenda());
            System.out.println("Total: " + String.format("%.2f", venda.getTotalVenda()));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void verHistoricoVendas() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        if (loja.listarVendas().isEmpty()) { System.out.println("Sem vendas registadas."); return; }
        for (Venda venda : loja.listarVendas()) {
            System.out.println(venda);
            for (ItemVenda item : venda.getItensVenda()) System.out.println("   - " + item);
        }
    }

    private void verValorTotalVendas() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        System.out.println("Valor total vendido: " + String.format("%.2f", loja.obterTotalVendas()));
    }

    private void relatorioInventarioCompleto() {
        List<Loja> lojas = inventarioManager.getLojasOrdenadas();
        if (lojas.isEmpty()) { System.out.println("Nenhuma loja registada."); return; }
        for (Loja loja : lojas) {
            System.out.println("=== " + loja.getNome() + " ===");
            for (Produto produto : loja.listarProdutos()) System.out.println(produto);
        }
    }

    private void mostrarInformacoesSistema() {
        System.out.println("Nome do sistema: Sistema de Gestao de Inventario");
        System.out.println("Versao: v1.0");
        System.out.println("Disciplina: Programacao II");
        System.out.println("Ano lectivo: 2025/2026");
        System.out.println("Integridade de dados: " + (gestorFicheiros.verificarIntegridade() ? "OK" : "ERRO"));
    }

    private void inserirDadosTeste() {
        if (!inventarioManager.existeLoja("L001")) {
            Loja loja = new Loja("L001", "Loja Centro", "Rua Principal, 10", "211234567");
            loja.adicionarProduto(new Produto("P001", "Arroz 5kg", "Arroz branco", 5.99, 25, 5));
            loja.adicionarProduto(new Produto("P002", "Oleo 1L", "Oleo alimentar", 3.50, 18, 4));
            inventarioManager.adicionarLoja(loja);
        }
        if (!inventarioManager.existeLoja("L002")) {
            Loja loja = new Loja("L002", "Loja Norte", "Av. da Republica, 50", "218765432");
            loja.adicionarProduto(new Produto("P003", "Acucar 1kg", "Acucar branco", 1.29, 40, 8));
            inventarioManager.adicionarLoja(loja);
        }
        salvarDados();
        System.out.println("Dados de teste inseridos com sucesso.");
    }

    private int lerInteiro(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduza um numero valido.");
            }
        }
    }

    private double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduza um valor numerico valido.");
            }
        }
    }

    private void deletarProduto() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { System.out.println("Selecione uma loja primeiro."); return; }
        String idProduto = lerTexto("ID do produto a deletar: ");
        Produto produto = loja.consultarProduto(idProduto);
        if (produto == null) { System.out.println("Produto nao encontrado."); return; }
        loja.removerProduto(idProduto);
        salvarDados();
        System.out.println("Produto deletado com sucesso.");
    }

    private void deletarLoja() {
        String idLoja = lerTexto("ID da loja a deletar: ");
        if (inventarioManager.deletarLoja(idLoja)) {
            salvarDados();
            System.out.println("Loja deletada com sucesso.");
        } else {
            System.out.println("Loja nao encontrada.");
        }
    }

    private String lerTexto(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("O campo nao pode estar em branco.");
                continue;
            }
            return texto;
        }
    }
}
