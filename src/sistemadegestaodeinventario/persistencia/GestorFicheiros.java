package sistemadegestaodeinventario.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sistemadegestaodeinventario.modelo.ItemVenda;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;
import sistemadegestaodeinventario.modelo.Usuario;
import sistemadegestaodeinventario.modelo.Venda;

public class GestorFicheiros {

	private final Path dataDir = Paths.get("data");

	public GestorFicheiros() {
		try {
			Files.createDirectories(dataDir);
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível criar a pasta de dados.", e);
		}
	}

	public List<Usuario> carregarUsuarios() {
		Path ficheiro = dataDir.resolve("usuarios.txt");
		List<Usuario> usuarios = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return usuarios;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 2) {
					Usuario.Perfil perfil = partes.length >= 3 ? lerPerfil(partes[2]) : Usuario.Perfil.VENDEDOR;
					String idLoja = partes.length >= 4 ? partes[3] : "";
					usuarios.add(new Usuario(partes[0], partes[1], perfil, idLoja));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar utilizadores.", e);
		}
		return usuarios;
	}

	public void salvarUsuarios(List<Usuario> usuarios) {
		Path ficheiro = dataDir.resolve("usuarios.txt");
		List<String> linhas = new ArrayList<>();
		for (Usuario usuario : usuarios) {
			linhas.add(String.join("|",
				usuario.getEmailOuTelefone(),
				usuario.getSenha(),
				usuario.getPerfil().name(),
				usuario.getIdLoja()));
		}
		escreverLinhas(ficheiro, linhas);
	}

	public List<Loja> carregarLojas() {
		Path ficheiro = dataDir.resolve("lojas.txt");
		List<Loja> lojas = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return lojas;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 4) {
					Loja loja = new Loja(partes[0], partes[1], partes[2], partes[3]);
					carregarProdutos(loja);
					List<Venda> vendas = carregarVendas(loja.getIdLoja());
					for (Venda venda : vendas) {
						loja.registarVenda(venda);
					}
					lojas.add(loja);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar lojas.", e);
		}
		return lojas;
	}

	public void salvarLojas(List<Loja> lojas) {
		Path ficheiro = dataDir.resolve("lojas.txt");
		List<String> linhas = new ArrayList<>();
		for (Loja loja : lojas) {
			linhas.add(String.join("|",
				loja.getIdLoja(), loja.getNome(), loja.getEndereco(), loja.getTelefone()));
			salvarProdutos(loja);
			salvarVendas(loja.getIdLoja(), loja.listarVendas());
		}
		escreverLinhas(ficheiro, linhas);
	}

	public void deletarDadosLoja(String idLoja) {
		if (idLoja == null || idLoja.trim().isEmpty()) {
			return;
		}
		try {
			Files.deleteIfExists(dataDir.resolve(nomeFicheiroProdutos(idLoja)));
			Files.deleteIfExists(dataDir.resolve(nomeFicheiroVendas(idLoja)));
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao deletar ficheiros da loja: " + idLoja, e);
		}
	}

	public void salvarProdutos(Loja loja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroProdutos(loja.getIdLoja()));
		List<String> linhas = new ArrayList<>();
		for (Produto produto : loja.listarProdutos()) {
			linhas.add(String.join("|",
				produto.getIdProduto(),
				produto.getNome(),
				produto.getDescricao(),
				String.valueOf(produto.getPreco()),
				String.valueOf(produto.getQuantidadeEmStock()),
				String.valueOf(produto.getQuantidadeMinima())));
		}
		escreverLinhas(ficheiro, linhas);
	}

	public void carregarProdutos(Loja loja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroProdutos(loja.getIdLoja()));
		if (!Files.exists(ficheiro)) {
			return;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 6) {
					loja.adicionarProduto(new Produto(
						partes[0],
						partes[1],
						partes[2],
						Double.parseDouble(partes[3]),
						Integer.parseInt(partes[4]),
						Integer.parseInt(partes[5])));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar produtos.", e);
		}
	}

	public void salvarVendas(String idLoja, List<Venda> vendas) {
		Path ficheiro = dataDir.resolve(nomeFicheiroVendas(idLoja));
		List<String> linhas = new ArrayList<>();
		linhas.add("ID_VENDA,DATA_VENDA,ID_LOJA,ID_PRODUTO,NOME_PRODUTO,QUANTIDADE,PRECO_UNITARIO,SUBTOTAL");
		for (Venda venda : vendas) {
			for (ItemVenda item : venda.getItensVenda()) {
				linhas.add(String.join(",",
					venda.getIdVenda(),
					venda.getDataVenda().toString(),
					venda.getIdLoja(),
					item.idProduto(),
					item.nomeProduto(),
					String.valueOf(item.quantidade()),
					String.valueOf(item.precoUnitario()),
					String.valueOf(item.subtotal())));
			}
		}
		escreverLinhas(ficheiro, linhas);
	}

	public List<Venda> carregarVendas(String idLoja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroVendas(idLoja));
		List<Venda> vendas = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return vendas;
		}
		try {
			Map<String, Venda> porId = new HashMap<>();
			List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
			for (int i = 1; i < linhas.size(); i++) {
				String linha = linhas.get(i);
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split(",", -1);
				if (partes.length >= 8) {
					Venda venda = porId.get(partes[0]);
					if (venda == null) {
						venda = new Venda(partes[0], LocalDateTime.parse(partes[1]), partes[2]);
						porId.put(partes[0], venda);
					}
					venda.adicionarItem(new ItemVenda(
						partes[3],
						partes[4],
						Integer.parseInt(partes[5]),
						Double.parseDouble(partes[6])));
				}
			}
			vendas.addAll(porId.values());
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar vendas.", e);
		}
		return vendas;
	}

	public Loja importarLojaCSV(String caminhoFicheiro) {
		Path ficheiro = Paths.get(caminhoFicheiro);
		if (!Files.exists(ficheiro)) {
			throw new IllegalArgumentException("Ficheiro nao encontrado: " + caminhoFicheiro);
		}
		try {
			List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
			int index = 0;
			while (index < linhas.size() && linhas.get(index).isBlank()) {
				index++;
			}
			if (index >= linhas.size()) {
				throw new IllegalArgumentException("Ficheiro CSV vazio ou sem conteudo util.");
			}
			String cabecalho = linhas.get(index).trim();
			String[] partesLoja = cabecalho.split(",", -1);
			if (partesLoja.length < 4) {
				throw new IllegalArgumentException("Cabecalho da loja invalido. Formato esperado: id,nome,morada,telefone");
			}
			Loja loja = new Loja(partesLoja[0].trim(), partesLoja[1].trim(), partesLoja[2].trim(), partesLoja[3].trim());
			for (int i = index + 1; i < linhas.size(); i++) {
				String linha = linhas.get(i).trim();
				if (linha.isBlank()) {
					continue;
				}
				String[] p = linha.split(",", -1);
				if (p.length < 6) {
					throw new IllegalArgumentException("Linha de produto invalida na linha " + (i + 1) + ". Formato esperado: id,nome,descricao,preco,quantidade,quantidadeMinima");
				}
				Produto produto = new Produto(
					p[0].trim(),
					p[1].trim(),
					p[2].trim(),
					Double.parseDouble(p[3].trim()),
					Integer.parseInt(p[4].trim()),
					Integer.parseInt(p[5].trim()));
				loja.adicionarProduto(produto);
			}
			return loja;
		} catch (IOException e) {
			throw new IllegalArgumentException("Erro ao ler o ficheiro: " + e.getMessage(), e);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Erro ao converter numeros no ficheiro CSV: " + e.getMessage(), e);
		}
	}

	public boolean verificarIntegridade() {
		return Files.exists(dataDir) && Files.isDirectory(dataDir);
	}

	public void criarBackup() {
		Path backupDir = dataDir.resolve("backup");
		try {
			Files.createDirectories(backupDir);
			copiarSeExistir(dataDir.resolve("lojas.txt"), backupDir.resolve("lojas.txt"));
			copiarSeExistir(dataDir.resolve("usuarios.txt"), backupDir.resolve("usuarios.txt"));
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao criar backup.", e);
		}
	}

	private Usuario.Perfil lerPerfil(String valor) {
		if (valor == null) {
			return Usuario.Perfil.VENDEDOR;
		}
		String perfil = valor.trim().toUpperCase();
		if ("NORMAL".equals(perfil) || "USUARIO".equals(perfil) || "UTILIZADOR".equals(perfil)) {
			return Usuario.Perfil.VENDEDOR;
		}
		if ("GESTOR".equals(perfil) || "GESTOR_DE_STOCK".equals(perfil)) {
			return Usuario.Perfil.GESTOR_STOCK;
		}
		try {
			return Usuario.Perfil.valueOf(perfil);
		} catch (IllegalArgumentException e) {
			return Usuario.Perfil.VENDEDOR;
		}
	}

	private void copiarSeExistir(Path origem, Path destino) throws IOException {
		if (Files.exists(origem)) {
			Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void escreverLinhas(Path ficheiro, List<String> linhas) {
		try {
			Files.createDirectories(ficheiro.getParent());
			Files.write(ficheiro, linhas, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao escrever ficheiro: " + ficheiro, e);
		}
	}

	private String nomeFicheiroProdutos(String idLoja) {
		return "loja_" + idLoja + "_produtos.txt";
	}

	private String nomeFicheiroVendas(String idLoja) {
		return "loja_" + idLoja + "_vendas.csv";
	}
}