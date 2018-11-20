package blockchain;
import java.security.*;
import java.util.ArrayList;

public class Transacoes {
	
	public String id; //Contains a hash of transaction*
	public PublicKey remetente; //Endereco remetente/public key.
	public PublicKey destinatario; //Endereco destinatario/public key.
	public float valor;
	public byte[] assinatura; //Impede que outra pessoa utilize os fundos de outra
	
	public ArrayList<TransacaoEntrada> entradaArrayList = new ArrayList<TransacaoEntrada>();
	public ArrayList<TransacaoSaida> saidaArrayList = new ArrayList<TransacaoSaida>();
	
	private static int totalTransacoes = 0;
	
	public Transacoes(PublicKey enviadoDe, PublicKey enviadoPara, float valorEnviado,  ArrayList<TransacaoEntrada> entradasArrayList) {
		this.remetente = enviadoDe;
		this.destinatario = enviadoPara;
		this.valor = valorEnviado;
		this.entradaArrayList = entradasArrayList;
	}
	
	public boolean processoDeTransacao() {
		
		if(verificarAssinatura() == false) {
			System.out.println("#######Assinatura de transação falhou ao verificar########");
			return false;
		}
				
		//Reúne as entradas de transação, certificando-se de que não são gastos
		for(TransacaoEntrada i : entradaArrayList) {
			i.UTXO = Main.UTXOs.get(i.idDaTransacaoDeSaida);
		}

		//Vefificando se transacao e  valida
		if(getValorEntrada() < Main.minimoTransacao) {
			System.out.println("Transacao de entrada  menor do que valor minimo: " + getValorEntrada());
			System.out.println("Envie um valor maior que " + Main.minimoTransacao);
			return false;
		}
		
		//Gerar saidas de transacao
		float restante = getValorEntrada() - valor;
		id = calcularHash();
		saidaArrayList.add(new TransacaoSaida( this.destinatario, valor,id)); //enviando valor ao destinatario
		saidaArrayList.add(new TransacaoSaida( this.remetente, restante,id));	
				
		//Adiciona saídas à lista de nao gastos
		for(TransacaoSaida o : saidaArrayList) {
			Main.UTXOs.put(o.id , o);
		}
		
		//Remova entradas de transação das listas UTXO conforme gasto
		for(TransacaoEntrada i : entradaArrayList) {
			if(i.UTXO == null) continue; //transacao não encontrada
			Main.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	public float getValorEntrada() {
		float total = 0;
		for(TransacaoEntrada i : entradaArrayList) {
			if(i.UTXO == null) continue; //transacao não encontrada
			total += i.UTXO.valor;
		}
		return total;
	}
	
	public void gerarAssinatura(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(remetente) + StringUtil.getStringFromKey(destinatario) + Float.toString(valor)	;
		assinatura = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	public boolean verificarAssinatura() {
		String data = StringUtil.getStringFromKey(remetente) + StringUtil.getStringFromKey(destinatario) + Float.toString(valor)	;
		return StringUtil.verificaECDSA(remetente, data, assinatura);
	}
	
	public float getValorSaida() {
		float total = 0;
		for(TransacaoSaida o : saidaArrayList) {
			total += o.valor;
		}
		return total;
	}
	
	private String calcularHash() {
		totalTransacoes++; //aumentar a sequência para evitar 2 transações idênticas com o mesmo hash
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(remetente) +
				StringUtil.getStringFromKey(destinatario) +
				Float.toString(valor) + totalTransacoes
				);
	}
}
