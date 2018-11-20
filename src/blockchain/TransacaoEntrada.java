package blockchain;

public class TransacaoEntrada {
	public String idDaTransacaoDeSaida;
	public TransacaoSaida UTXO;
	
	public TransacaoEntrada(String idDaTransacaoDeSaida) {
		this.idDaTransacaoDeSaida = idDaTransacaoDeSaida;
	}
}
