package sistemadegestaodeinventario.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venda {

	private String idVenda;
	private LocalDateTime dataVenda;
	private final List<ItemVenda> itensVenda;
	private double totalVenda;
	private String idLoja;

	public Venda(String idVenda, LocalDateTime dataVenda, String idLoja) {
		setIdVenda(idVenda);
		setDataVenda(dataVenda);
		setIdLoja(idLoja);
		this.itensVenda = new ArrayList<>();
	}

	public String getIdVenda() {
		return idVenda;
	}

	public void setIdVenda(String idVenda) {
		if (idVenda == null || idVenda.trim().isEmpty()) {
			throw new IllegalArgumentException("O ID da venda não pode estar vazio.");
		}
		this.idVenda = idVenda.trim();
	}

	public LocalDateTime getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(LocalDateTime dataVenda) {
		if (dataVenda == null) {
			throw new IllegalArgumentException("A data da venda não pode ser nula.");
		}
		this.dataVenda = dataVenda;
	}

	public String getIdLoja() {
		return idLoja;
	}

	public void setIdLoja(String idLoja) {
		if (idLoja == null || idLoja.trim().isEmpty()) {
			throw new IllegalArgumentException("O ID da loja não pode estar vazio.");
		}
		this.idLoja = idLoja.trim();
	}

	public void adicionarItem(ItemVenda itemVenda) {
		if (itemVenda == null) {
			throw new IllegalArgumentException("O item da venda não pode ser nulo.");
		}
		itensVenda.add(itemVenda);
		calcularTotal();
	}

	public double calcularTotal() {
		totalVenda = itensVenda.stream().mapToDouble(ItemVenda::calcularSubtotal).sum();
		return totalVenda;
	}

	public List<ItemVenda> obterItens() {
		return Collections.unmodifiableList(itensVenda);
	}

	public List<ItemVenda> getItensVenda() {
		return obterItens();
	}

	public double getTotalVenda() {
		return totalVenda;
	}

	public void setTotalVenda(double totalVenda) {
		this.totalVenda = totalVenda;
	}

	public LocalDateTime obterDataVenda() {
		return dataVenda;
	}

	@Override
	public String toString() {
		return String.format("%s | %s | loja=%s | total=%.2f | itens=%d",
				idVenda, dataVenda, idLoja, totalVenda, itensVenda.size());
	}
}