package blockchain;
import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
//import com.google.gson.GsonBuilder;
import java.util.Map;

public class Main {
	
	public static ArrayList<Bloco> blockchain = new ArrayList<Bloco>();
	public static HashMap<String,TransacaoSaida> UTXOs = new HashMap<String,TransacaoSaida>();
	
	public static int dificuldade = 1;
	public static float minimoTransacao = 0.1f;
	public static Carteira carteiraA;
	public static Carteira carteiraB;
	public static Transacoes iniTransacao;

	public static void main(String[] args) {	
		//adicionando o bloco ao ArrayList
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
		
		//Criando carteiras
		carteiraA = new Carteira();
		carteiraB = new Carteira();		
		Carteira coinbase = new Carteira();
		
		//na  transacao inicial A comeca com 100
		iniTransacao = new Transacoes(coinbase.publicKey, carteiraA.publicKey, 50f, null);
		iniTransacao.gerarAssinatura(coinbase.privateKey);	
		iniTransacao.id = "0"; //manually set the transaction id
		iniTransacao.saidaArrayList.add(new TransacaoSaida(iniTransacao.destinatario, iniTransacao.valor, iniTransacao.id)); 
		UTXOs.put(iniTransacao.saidaArrayList.get(0).id, iniTransacao.saidaArrayList.get(0));
		
		System.out.println("Criando e Minerando o bloco inicial... ");
		Bloco inicial = new Bloco("0");
		inicial.addTransacao(iniTransacao);
		addBlock(inicial);
		
		//testando
		Bloco block1 = new Bloco(inicial.hash);
		System.out.println("\nCarteira A possui " + carteiraA.getSaldo());
		System.out.println("\nCarteira A está enviando 40 para Carteira B...");
		block1.addTransacao(carteiraA.enviarValor(carteiraB.publicKey, 40f));
		
		addBlock(block1);
		
		System.out.println("\nCarteira A possui " + carteiraA.getSaldo());
		System.out.println("Carteira B possui " + carteiraB.getSaldo());
		
		Bloco block2 = new Bloco(block1.hash);
		System.out.println("\nCarteira A está enviando 1000 para Carteira B...");
		block2.addTransacao(carteiraA.enviarValor(carteiraB.publicKey, 1000f));
		
		addBlock(block2);
		
		System.out.println("\nCarteira A possui " + carteiraA.getSaldo());
		System.out.println("\nCarteira B possui " + carteiraB.getSaldo());
		
		Bloco block3 = new Bloco(block2.hash);
		
		System.out.println("\nCarteira B está enviando 20 para Carteira A...");
		block3.addTransacao(carteiraB.enviarValor( carteiraA.publicKey, 20));
		
		System.out.println("\nCarteira A possui " + carteiraA.getSaldo());
		System.out.println("\nCarteira B possui " + carteiraB.getSaldo());
		
		isChainValid();
		
	}
	
	//Verifica se o BlockChain  é  valido
	public static Boolean isChainValid() {
		Bloco blocoAtual; 
		Bloco blocoAnterior;
		String hashTarget = new String(new char[dificuldade]).replace('\0', '0');
		HashMap<String,TransacaoSaida> tempUTXOs = new HashMap<String,TransacaoSaida>(); //transações não gastas em um determinado estado de bloqueio.
		tempUTXOs.put(iniTransacao.saidaArrayList.get(0).id, iniTransacao.saidaArrayList.get(0));
		
		//percorre o BlockChain verificando as Hashes
		for(int i=1; i < blockchain.size(); i++) {
			
			blocoAtual = blockchain.get(i);
			blocoAnterior = blockchain.get(i-1);
			
			//compara o hash registrado com o calculado
			if(!blocoAtual.hash.equals(blocoAtual.calcularHash()) ){
				System.out.println("#Hash atual não é igual");
				return false;
			}
			
			//compara o hash anterior e o hash anterior registrado
			if(!blocoAnterior.hash.equals(blocoAtual.anteriorHash) ) {
				System.out.println("#Hashe anterior não é igual");
				return false;
			}
			
			//vefirica se o hash está resolvido
			if(!blocoAtual.hash.substring( 0, dificuldade).equals(hashTarget)) {
				System.out.println("#Bloco não minerado");
				return false;
			}
			
			//percorrendo as transacoes
			TransacaoSaida tempOutput;
			for(int t=0; t <blocoAtual.transacoes.size(); t++) {
				Transacoes transacaoAtual = blocoAtual.transacoes.get(t);
				
				if(!transacaoAtual.verificarAssinatura()) {
					System.out.println("#Assinatura na transacao (" + t + ") não é valida");
					return false; 
				}
				if(transacaoAtual.getValorEntrada() != transacaoAtual.getValorSaida()) {
					System.out.println("#Entrada não é igual as transacoes  de saída(" + t + ")");
					return false; 
				}
				
				for(TransacaoEntrada input: transacaoAtual.entradaArrayList) {	
					tempOutput = tempUTXOs.get(input.idDaTransacaoDeSaida);
					
					if(tempOutput == null) {
						System.out.println("#Não possui transacao de saida  referenciada (" + t + ")");
						return false;
					}
					
					if(input.UTXO.valor != tempOutput.valor) {
						System.out.println("#Não possui transacao de  entrada referenciada(" + t + ")");
						return false;
					}
					
					tempUTXOs.remove(input.idDaTransacaoDeSaida);
				}
				
				for(TransacaoSaida output: transacaoAtual.saidaArrayList) {
					tempUTXOs.put(output.id, output);
				}
				
				if( transacaoAtual.saidaArrayList.get(0).destinatario != transacaoAtual.destinatario) {
					System.out.println("#Transacao(" + t + ") de saida, o destinatario está incorreto");
					return false;
				}
				if( transacaoAtual.saidaArrayList.get(1).destinatario != transacaoAtual.remetente) {
					System.out.println("#Transacao(" + t + ") saida 'change' não é o remetente.");
					return false;
				}
				
			}
			
		}
		System.out.println("O Blockchain é valido");
		return true;
	}
	
	public static void addBlock(Bloco newBlock) {
		newBlock.mineBlock(dificuldade);
		blockchain.add(newBlock);
	}
}
