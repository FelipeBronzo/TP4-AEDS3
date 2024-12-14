package src.util;

import java.util.Random;
import java.math.BigInteger;

public class RSA {

    /*
     * Implementação do RSA onde 'p' e 'q' são números primos escolhidos aleatoriamente
     * de uma lista predefinida. Este algoritmo inclui métodos para gerar as chaves,
     * criptografar e descriptografar mensagens.
     */

    private int p, q, n, z, d, e; // Variáveis principais do algoritmo RSA
    private int[] opcoes_p = {3, 5, 7}; // Lista de valores possíveis para 'p'
    private int[] opcoes_q = {11, 13, 17}; // Lista de valores possíveis para 'q'

    // Construtor que inicializa o RSA com valores aleatórios para 'p' e 'q'
    public RSA() {
        Random random = new Random();
        this.p = opcoes_p[random.nextInt(opcoes_p.length)]; // Escolhe um 'p' aleatório
        this.q = opcoes_q[random.nextInt(opcoes_q.length)]; // Escolhe um 'q' aleatório
        this.n = p * q; // Calcula 'n' (parte da chave pública)
        this.z = (p - 1) * (q - 1); // Calcula o 'z' (totiente de n)
        this.d = encontrarPrimoRelativo(z); // Gera 'd', uma chave privada
        this.e = encontrarInversoModular(d, z); // Gera 'e', parte da chave pública
    }

    // Calcula o MDC (Máximo Divisor Comum) usando o algoritmo de Euclides
    private int mdc(int a, int b) {
        while (b != 0) {
            int resto = a % b;
            a = b;
            b = resto;
        }
        return a;
    }

    // Encontra o primeiro número primo relativo a 'z'
    private int encontrarPrimoRelativo(int z) {
        for (int i = 2; i < z; i++) { // Começa do 2 porque 1 é trivial
            if (mdc(i, z) == 1) { // Verifica se é primo relativo a 'z'
                return i;
            }
        }
        throw new RuntimeException("Não foi possível encontrar um primo relativo a " + z);
    }

    // Encontra o inverso modular de 'd' com relação a 'z' usando o Algoritmo Estendido de Euclides
    private int encontrarInversoModular(int d, int z) {
        int t = 0, novoT = 1;
        int r = z, novoR = d;

        while (novoR != 0) {
            int quociente = r / novoR;

            // Atualiza os valores de 'r' e 't'
            int temp = novoR;
            novoR = r - quociente * novoR;
            r = temp;

            temp = novoT;
            novoT = t - quociente * novoT;
            t = temp;
        }

        if (r > 1) {
            throw new RuntimeException("d não tem inverso modular com z.");
        }
        if (t < 0) {
            t += z;
        }
        return t;
    }

    // Converte um texto em um array de inteiros representando a posição das letras no alfabeto
    private int[] encontrandoP(String texto) {
        int[] textoP = new int[texto.length()];
        String textoMinusculo = texto.toLowerCase();

        for (int i = 0; i < texto.length(); i++) {
            textoP[i] = (int) textoMinusculo.charAt(i) - 96; // 'a' = 1, 'b' = 2, ...
        }

        return textoP;
    }

    // Criptografa uma mensagem usando a chave pública (n, e)
    public int[] criptografia(String texto) {
        int[] textoP = encontrandoP(texto); // Converte o texto para números
        int[] textoCriptografado = new int[texto.length()];
        BigInteger[] textoCalculo = new BigInteger[texto.length()];
        BigInteger modBase = BigInteger.valueOf(this.n);

        for (int i = 0; i < texto.length(); i++) {
            BigInteger base = BigInteger.valueOf(textoP[i]);
            textoCalculo[i] = base.pow(this.e).mod(modBase); // Cálculo da criptografia
            textoCriptografado[i] = textoCalculo[i].intValue(); // Converte o resultado para inteiro
        }

        return textoCriptografado; // Retorna o texto criptografado como um array de inteiros
    }

    // Descriptografa uma mensagem usando a chave privada (n, d)
    public String descriptografar(int[] textoCripto) {
        StringBuilder textoDescriptografado = new StringBuilder();
        BigInteger[] textoCalculo = new BigInteger[textoCripto.length];
        BigInteger modBase = BigInteger.valueOf(this.n);

        for (int i = 0; i < textoCripto.length; i++) {
            BigInteger base = BigInteger.valueOf(textoCripto[i]);
            textoCalculo[i] = base.pow(this.d).mod(modBase); // Cálculo da descriptografia
            textoDescriptografado.append((char) (textoCalculo[i].intValue() + 96)); // Converte para caractere
        }

        return textoDescriptografado.toString(); // Retorna o texto descriptografado como String
    }
}
