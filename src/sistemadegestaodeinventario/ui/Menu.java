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
    private static final String ADMIN_PADRAO_CREDENCIAL = "admin@system";
    private static final String ADMIN_PADRAO_SENHA = "Admin1234";
    private static final String LIMPAR_TERMINAL_ANSI = "\033[H\033[2J";
    private static final int LINHAS_LIMPEZA_FALLBACK = 40;
    private final String os = System.getProperty("os.name", "").toLowerCase();

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
        System.out.println("AUTENTICACAO DE USUARIO");
        System.out.println("1. Login Vendedor");
        System.out.println("2. Login Gestor de Stock");
        System.out.println("3. Login Admin");
        System.out.println("0. Sair");
        while (ativo && usuarioAutenticado == null) {
            int op = lerInteiro("Escolha uma opcao: ");
            switch (op) {
                case 1 -> fazerLoginVendedor();
                case 2 -> fazerLoginGestorStock();
                case 3 -> fazerLoginAdmin();
                case 0 -> ativo = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
        while (ativo && usuarioAutenticado != null) {
            if (usuarioAutenticado.isAdmin()) {
                ativo = executarSessaoAdmin();
            } else if (usuarioAutenticado.isGestorStock()) {
                ativo = executarSessaoGestorStock();
            } else {
                ativo = executarSessaoVendedor();
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
        if (!existeAdministrador()) {
            usuarioManager.adicionarUsuario(new Usuario(ADMIN_PADRAO_CREDENCIAL, ADMIN_PADRAO_SENHA, Usuario.Perfil.ADMIN));
            System.out.println("Conta admin inicial criada: " + ADMIN_PADRAO_CREDENCIAL + " / " + ADMIN_PADRAO_SENHA);
        }
        gestorFicheiros.salvarUsuarios(usuarioManager.listarUsuarios());
    }

    private void salvarDados() {
        gestorFicheiros.salvarUsuarios(usuarioManager.listarUsuarios());
        gestorFicheiros.salvarLojas(inventarioManager.getLojasOrdenadas());
    }

    private boolean executarSessaoAdmin() {
        boolean ativo = true;
        boolean exibirMenu = true;
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isAdmin()) {
            clearTerminal();
            if (exibirMenu)
            {
                exibirMenuPrincipalAdmin();
                exibirMenu = true;
            }
            int opcao = lerInteiro("Escolha uma opcao: ");
            switch (opcao) {
                case 1 -> menuLojas();
                case 2 -> menuProdutos();
                case 3 -> menuVendas();
                case 4 -> menuRelatorios();
                case 5 -> menuConfiguracao();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    System.out.println("Sessao terminada com sucesso.");
                }
                default -> {
                    System.out.println("Opcao invalida.");
                    exibirMenu = false;
                }
            }
        }
        return ativo;
    }

    private boolean executarSessaoGestorStock() {
        boolean ativo = true;
        boolean exibirMenu = true;
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isGestorStock()) {
            clearTerminal();
            if (exibirMenu)
            {
                exibirMenuPrincipalGestorStock();
                exibirMenu = true;
            }
            int opcao = lerInteiro("Escolha uma opcao: ");
            switch (opcao) {
                case 1 -> menuLojas();
                case 2 -> menuProdutos();
                case 3 -> menuVendas();
                case 4 -> menuRelatorios();
                case 5 -> menuConfiguracao();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    System.out.println("Sessao terminada com sucesso.");
                }
                default -> {
                    System.out.println("Opcao invalida.");
                    exibirMenu = false;
                }
            }
        }
        return ativo;
    }

    private boolean executarSessaoVendedor() {
        boolean ativo = true;
        boolean exibirMenu = true;
        if (!selecionarLojaDoVendedor()) {
            usuarioAutenticado = null;
            return false;
        }
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isVendedor()) {
            exibirMenuPrincipalVendedor();
            int opcao = lerInteiro("Escolha uma opcao: ");
            switch (opcao) {
                case 1 -> consultarStockProduto();
                case 2 -> iniciarNovaVenda();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    System.out.println("Sessao terminada com sucesso.");
                }
                default -> {
                    System.out.println("Opcao invalida.");
                    exibirMenu = false;
                }
            }
        }
        return ativo;
    }

    private void fazerLoginVendedor() {
        fazerLogin(Usuario.Perfil.VENDEDOR);
    }

    private void fazerLoginGestorStock() {
        fazerLogin(Usuario.Perfil.GESTOR_STOCK);
    }

    private void clearTerminal() {
        if (suportaLimpezaAnsi()) {
            System.out.print(LIMPAR_TERMINAL_ANSI);
        } else {
            for (int i = 0; i < LINHAS_LIMPEZA_FALLBACK; i++) {
                System.out.println();
            }
        }
        System.out.flush();
    }

    private boolean suportaLimpezaAnsi() {
        return !os.contains("win") || System.console() != null;
    }

    private void mostrarMensagemEmTelaLimpa(String mensagem) {
        clearTerminal();
        System.out.println(mensagem);
    }

    private void fazerLoginAdmin() {
        fazerLogin(Usuario.Perfil.ADMIN);
    }

    private void fazerLogin(Usuario.Perfil perfilEsperado) {
        while (usuarioAutenticado == null) {
            String credencial = lerTexto("Email/Telefone (0 para voltar): ");
            if ("0".equals(credencial)) {
                clearTerminal();
                return;
            }

            Usuario usuario = usuarioManager.buscarPorCredencial(credencial);
            if (usuario == null) {
                mostrarMensagemEmTelaLimpa("Login incorreto.");
                continue;
            }

            while (usuarioAutenticado == null) {
                String senha = lerTexto("Senha (0 para voltar): ");
                if ("0".equals(senha)) {
                    clearTerminal();
                    return;
                }

                if (!usuario.senhaCorreta(senha)) {
                    mostrarMensagemEmTelaLimpa("Senha incorreta.");
                    continue;
                }
                clearTerminal();

                if (usuario.getPerfil() != perfilEsperado) {
                    System.out.println("Esta conta é " + nomePerfil(usuario.getPerfil()) + ". Use a opcao correta de login.");
                    return;
                }

                usuarioAutenticado = usuario;
                if (usuario.isVendedor() && !selecionarLojaDoVendedor()) {
                    usuarioAutenticado = null;
                    return;
                }
                System.out.println("Login de " + nomePerfil(usuario.getPerfil()) + " efetuado com sucesso.");
            }
        }
    }

    private void criarUsuario() {
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
        Usuario.Perfil perfil = lerPerfilUsuario("Perfil (ADMIN/GESTOR_STOCK/VENDEDOR): ");
        String idLoja = "";
        if (perfil == Usuario.Perfil.VENDEDOR) {
            if (inventarioManager.getLojasOrdenadas().isEmpty()) {
                System.out.println("Cadastre uma loja antes de criar um vendedor.");
                return;
            }
            idLoja = lerLojaExistente("ID da loja do vendedor: ");
        }
        Usuario usuario = new Usuario(credencial, senha, perfil, idLoja);
        usuarioManager.adicionarUsuario(usuario);
        salvarDados();
        System.out.println("Usuario criado com sucesso.");
    }

    private void exibirMenuPrincipalAdmin() {
        System.out.println("==========================");
        System.out.println("| MENU PRINCIPAL - ADMIN |");
        System.out.println("==========================");
        System.out.println("1. Gestao de Lojas");
        System.out.println("2. Gestao de Produtos");
        System.out.println("3. Registar Vendas");
        System.out.println("4. Relatorios e Consultas");
        System.out.println("5. Configuracao do Sistema");
        System.out.println("0. Terminar Sessao");
    }

    private void exibirMenuPrincipalGestorStock() {
        System.out.println("=================================");
        System.out.println("| MENU PRINCIPAL - GESTOR STOCK |");
        System.out.println("=================================");
        System.out.println("1. Gestao de Lojas");
        System.out.println("2. Gestao de Produtos");
        System.out.println("3. Registar Vendas");
        System.out.println("4. Relatorios e Consultas");
        System.out.println("5. Configuracao do Sistema");
        System.out.println("0. Terminar Sessao");
    }

    private void exibirMenuPrincipalVendedor() {
        System.out.println("==============================");
        System.out.println("| MENU PRINCIPAL - VENDEDOR |");
        System.out.println("==============================");
        System.out.println("1. Consultar Stock");
        System.out.println("2. Registar Venda");
        System.out.println("0. Terminar Sessao");
    }

    private void menuLojas() {
        clearTerminal();
        boolean voltar = false;
        System.out.println("===================");
        System.out.println("| GESTAO DE LOJAS |");
        System.out.println("===================");
        System.out.println("1. Adicionar Loja");
        System.out.println("2. Listar Todas as Lojas");
        System.out.println("3. Selecionar Loja");
        System.out.println("4. Ver Detalhes da Loja Ativa");
        System.out.println("5. Deletar Loja");
        System.out.println("6. Importar Loja via CSV");
        System.out.println("0. Voltar ao Menu Principal");
        while (!voltar) {
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> adicionarLoja();
                case 2 -> listarLojas();
                case 3 -> selecionarLoja();
                case 4 -> verDetalhesLojaAtiva();
                case 5 -> deletarLoja();
                case 6 -> importarLojaCSV();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuProdutos() {
        clearTerminal();
        boolean voltar = false;
        System.out.println("=====================");
        System.out.println("| GESTAO DE PRODUTOS |");
        System.out.println("=====================");
        System.out.println("1. Adicionar Produto a Loja");
        System.out.println("2. Listar Produtos da Loja");
        System.out.println("3. Consultar Stock de Produto");
        System.out.println("4. Aumentar Stock de Produto");
        System.out.println("5. Produtos com Stock Baixo");
        System.out.println("6. Deletar Produto");
        System.out.println("0. Voltar ao Menu Principal");
        while (!voltar) {
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
        clearTerminal();
        boolean voltar = false;
        System.out.println("===================");
        System.out.println("| REGISTAR VENDAS |");
        System.out.println("===================");
        System.out.println("1. Iniciar Nova Venda");
        System.out.println("2. Ver Historico de Vendas");
        System.out.println("3. Valor Total de Vendas");
        System.out.println("0. Voltar ao Menu Principal");
        while (!voltar) {
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
        clearTerminal();
        boolean voltar = false;
        System.out.println("==============");
        System.out.println("| RELATORIOS |");
        System.out.println("==============");

        System.out.println("1. Relatorio da Loja Atual");
        System.out.println("2. Inventario Completo");
        System.out.println("3. Relatorio do Sistema");
        System.out.println("0. Voltar ao Menu Principal");
        while (!voltar) {
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
        clearTerminal();
        boolean voltar = false;
        System.out.println("================");
        System.out.println("| CONFIGURACAO |");
        System.out.println("================");
        System.out.println("1. Informacoes do Sistema");
        System.out.println("2. Criar Backup de Dados");
        System.out.println("3. Dados de Teste");
        while (!voltar) {
            if (usuarioAutenticado.isAdmin()) {
                System.out.println("4. Gestao de Utilizadores");
            }
            System.out.println("0. Voltar ao Menu Principal");
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> mostrarInformacoesSistema();
                case 2 -> { gestorFicheiros.criarBackup(); System.out.println("Backup criado com sucesso."); }
                case 3 -> inserirDadosTeste();
                case 4 -> {
                    if (usuarioAutenticado.isAdmin()) menuUsuarios();
                    else System.out.println("Opcao invalida.");
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void menuUsuarios() {
        clearTerminal();
        boolean voltar = false;
        System.out.println("==========================");
        System.out.println("| GESTAO DE UTILIZADORES |");
        System.out.println("==========================");
        System.out.println("1. Criar Novo Utilizador");
        System.out.println("2. Listar Utilizadores");
        System.out.println("3. Eliminar Utilizador");
        System.out.println("0. Voltar");
        while (!voltar) {
            switch (lerInteiro("Escolha uma opcao: ")) {
                case 1 -> criarUsuario();
                case 2 -> listarUsuarios();
                case 3 -> eliminarUsuario();
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    private void listarUsuarios() {
        
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            System.out.println(usuario);
        }
    }

    private void eliminarUsuario() {
        String credencial = lerTexto("Email/Telefone do utilizador a eliminar: ");
        Usuario usuario = usuarioManager.buscarPorCredencial(credencial);
        if (usuario == null) {
            System.out.println("Utilizador nao encontrado.");
            return;
        }
        if (usuarioAutenticado != null && usuarioAutenticado.coincideCredencial(credencial)) {
            System.out.println("Nao e possivel eliminar o utilizador autenticado.");
            return;
        }
        if (usuario.isAdmin() && contarAdministradores() <= 1) {
            System.out.println("Nao e possivel eliminar o ultimo administrador.");
            return;
        }
        if (usuarioManager.removerUsuario(credencial)) {
            salvarDados();
            System.out.println("Utilizador eliminado com sucesso.");
        } else {
            System.out.println("Nao foi possivel eliminar o utilizador.");
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
        clearTerminal();
        Loja loja = lojaManager.getLojaAtual();
        if (usuarioAutenticado.isVendedor()) {
            if (!selecionarLojaDoVendedor()) {
                return;
            }
            loja = lojaManager.getLojaAtual();
        } else if (loja == null) {
            String idLoja = lerTexto("ID da loja: ");
            if (!lojaManager.selecionarLoja(idLoja)) {
                System.out.println("Loja nao encontrada.");
                return;
            }
            loja = lojaManager.getLojaAtual();
        }
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
        lojaManager.deletarProduto(idProduto);
        salvarDados();
        System.out.println("Produto deletado com sucesso.");
    }

    private void deletarLoja() {
        
        String idLoja = lerTexto("ID da loja a deletar: ");
        if (existeVendedorAssociadoLoja(idLoja)) {
            System.out.println("Nao e possivel deletar a loja. Existe vendedor associado a esta loja.");
            return;
        }
        if (lojaManager.deletarLoja(idLoja)) {
            gestorFicheiros.deletarDadosLoja(idLoja);
            salvarDados();
            System.out.println("Loja deletada com sucesso.");
        } else {
            System.out.println("Loja nao encontrada.");
        }
    }

    private void importarLojaCSV() {
        
        String caminho = lerTexto("Caminho do ficheiro CSV: ");
        try {
            Loja loja = gestorFicheiros.importarLojaCSV(caminho);
            if (inventarioManager.existeLoja(loja.getIdLoja())) {
                System.out.println("Ja existe uma loja com este ID: " + loja.getIdLoja());
                return;
            }
            inventarioManager.adicionarLoja(loja);
            salvarDados();
            System.out.println("Loja importada com sucesso: " + loja.getNome());
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao importar CSV: " + e.getMessage());
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

    private Usuario.Perfil lerPerfilUsuario(String prompt) {
        while (true) {
            String valor = lerTexto(prompt).toUpperCase();
            if ("ADMIN".equals(valor)) {
                return Usuario.Perfil.ADMIN;
            }
            if ("GESTOR".equals(valor) || "GESTOR_STOCK".equals(valor) || "GESTOR DE STOCK".equals(valor)) {
                return Usuario.Perfil.GESTOR_STOCK;
            }
            if ("VENDEDOR".equals(valor) || "USUARIO".equals(valor) || "UTILIZADOR".equals(valor)) {
                return Usuario.Perfil.VENDEDOR;
            }
            System.out.println("Perfil invalido. Use ADMIN, GESTOR_STOCK ou VENDEDOR.");
        }
    }

    private String lerLojaExistente(String prompt) {
        while (true) {
            String idLoja = lerTexto(prompt);
            if (inventarioManager.existeLoja(idLoja)) {
                return idLoja;
            }
            System.out.println("Loja nao encontrada. Cadastre ou informe uma loja existente.");
        }
    }

    private boolean selecionarLojaDoVendedor() {
        if (usuarioAutenticado == null || !usuarioAutenticado.isVendedor()) {
            return true;
        }
        String idLoja = usuarioAutenticado.getIdLoja();
        if (idLoja == null || idLoja.isBlank()) {
            System.out.println("Este vendedor nao esta associado a nenhuma loja.");
            return false;
        }
        if (!lojaManager.selecionarLoja(idLoja)) {
            System.out.println("A loja associada ao vendedor nao foi encontrada: " + idLoja);
            return false;
        }
        return true;
    }

    private String nomePerfil(Usuario.Perfil perfil) {
        if (perfil == Usuario.Perfil.ADMIN) {
            return "administrador";
        }
        if (perfil == Usuario.Perfil.GESTOR_STOCK) {
            return "gestor de stock";
        }
        return "vendedor";
    }

    private boolean existeVendedorAssociadoLoja(String idLoja) {
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            if (usuario.isVendedor() && usuario.getIdLoja().equals(idLoja)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeAdministrador() {
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            if (usuario.isAdmin()) {
                return true;
            }
        }
        return false;
    }

    private int contarAdministradores() {
        int total = 0;
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            if (usuario.isAdmin()) {
                total++;
            }
        }
        return total;
    }
}
