package blockchain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Carteira {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransacaoSaida> UTXOs = new HashMap<String,TransacaoSaida>();
	
	public Carteira() {
		generateKeyPair();
	}
		
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); //256 
	        KeyPair keyPair = keyGen.generateKeyPair();
	        // Set the public and private keys from the keyPair
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getSaldo() {
		float total = 0;	
        for (Map.Entry<String, TransacaoSaida> item: Main.UTXOs.entrySet()){
        	TransacaoSaida UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //Se a chave pertence a mim
            	UTXOs.put(UTXO.id,UTXO); //add a lista de transações não  gastas
            	total += UTXO.valor ; 
            }
        }  
		return total;
	}
	
	public Transacoes enviarValor(PublicKey _destinatario,float valor ) {
		if(getSaldo() < valor) {
			System.out.println("#Saldo insuficiente :/");
			return null;
		}
		ArrayList<TransacaoEntrada> inputs = new ArrayList<TransacaoEntrada>();
		
		float total = 0;
		for (Map.Entry<String, TransacaoSaida> item: UTXOs.entrySet()){
			TransacaoSaida UTXO = item.getValue();
			total += UTXO.valor;
			inputs.add(new TransacaoEntrada(UTXO.id));
			if(total > valor) break;
		}
		
		Transacoes novaTransacao = new Transacoes(publicKey, _destinatario , valor, inputs);
		novaTransacao.gerarAssinatura(privateKey);
		
		for(TransacaoEntrada input: inputs){
			UTXOs.remove(input.idDaTransacaoDeSaida);
		}
		
		return novaTransacao;
	}
	
}


