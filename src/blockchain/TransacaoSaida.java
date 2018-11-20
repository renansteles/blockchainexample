package blockchain;

import java.security.PublicKey;

/*
 * As transações  de  saída mostrarão o valor final enviado para cada parte da transação. 
 * Estes, quando referenciados como entradas em novas transações, 
 * agem como prova de que você tem moedas para enviar.
 */
public class TransacaoSaida {
	public String id;
	public PublicKey destinatario; //Quem vai  receber as moedas
	public float valor; //Total de moedas que possui
	public String idTransacao;
	
	public TransacaoSaida(PublicKey destinatario, float valor, String parenteTransacaoId) {
		this.destinatario = destinatario;
		this.valor = valor;
		this.idTransacao = parenteTransacaoId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(destinatario)+Float.toString(valor)+parenteTransacaoId);
	}
	
	//Checa se a moeda é minha
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == destinatario);
	}
	
}
