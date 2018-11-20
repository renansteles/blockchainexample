package blockchain;

import java.util.ArrayList;
import java.util.Date;

public class Bloco {
	
	public String hash;
	public String anteriorHash; 
	public String merkleRoot;
	public ArrayList<Transacoes> transacoes = new ArrayList<Transacoes>();
	public long timeStamp;
	public int nonce;
	
	public Bloco(String previousHash ) {
		this.anteriorHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calcularHash();
	}
	
	//Calculate new hash based on blocks contents
	public String calcularHash() {
		String calcularHash = StringUtil.applySha256( 
				anteriorHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calcularHash;
	}
	
	public void mineBlock(int dificuldade) {
		merkleRoot = StringUtil.getMerkleRoot(transacoes);
		String target = StringUtil.getDificuldadeString(dificuldade); //Cria string com  "0" 
		while(!hash.substring( 0, dificuldade).equals(target)) {
			nonce ++;
			hash = calcularHash();
		}
		System.out.println("Bloco minerado!!! : " + hash);
	}
	
	//Adiciona  a transacao a este bloco
	public boolean addTransacao(Transacoes transacao) {
		//processo de  transacao e verificacao se é válido, a menos que o bloco seja um bloco (INICIAL), então ignore.
		if(transacao == null) return false;		
		if((!"0".equals(anteriorHash))) {//NÃO É BLOCO INICIAL
			if((transacao.processoDeTransacao() != true)) {
				System.out.println("Transação falhou!");
				return false;
			}
		}

		transacoes.add(transacao);
		System.out.println("Transação adicionada com sucesso ao bloco");
		return true;
	}
	
}
