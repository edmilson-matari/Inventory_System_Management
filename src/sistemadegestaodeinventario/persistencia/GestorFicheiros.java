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
					usuarios.add(new Usuario(partes[0], partes[1]));
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
			linhas.add(usuario.getEmailOuTelefone() + "|" + usuario.getSenha());
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
					for (Venda v : vendas) {
						loja.registarVenda(v);
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