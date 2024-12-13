package src.util;
import java.util.Random;
import java.util.Scanner;
import java.math.BigInteger;

class RSA{

    /* Fizemos o RSA de maneira que 'p' e 'q' sejam escolhidos aleatoriamente dentre algumas opções
     * O que acabou gerando algumas funções a mais para calcular o 'd' e o 'e'
     */

    // 97 - 122

    private int p, q, n, z, d, e;
    private int[] opcoes_p = {3, 5, 7};
    private int[] opcoes_q = {11, 13, 17};

    RSA(){
        Random random = new Random();
        this.p = opcoes_p[random.nextInt(opcoes_p.length)];
        this.q = opcoes_q[random.nextInt(opcoes_q.length)];
        this.n = p*q;
        this.z = (p-1) * (q-1);
        // Encontrar d que seja primo relativo a z
        this.d = encontrarPrimoRelativo(z);

        // Encontrar e que satisfaça (e * d) % z == 1
        this.e = encontrarInversoModular(d, z);
    }

        // Função para calcular o MDC usando o algoritmo de Euclides
    private int mdc(int a, int b) {
        while (b != 0) {
            int resto = a % b;
            a = b;
            b = resto;
        }
        return a;
    }
    
    // Função para encontrar o primeiro número primo relativo a z
    private int encontrarPrimoRelativo(int z) {
        for (int i = 2; i < z; i++) { // Começa de 2 porque 1 é trivial
            if (mdc(i, z) == 1) {
                return i;
            }
        }
        throw new RuntimeException("Não foi possível encontrar um primo relativo a " + z);
    }

    // Função para encontrar o inverso modular usando o Algoritmo Estendido de Euclides
    private int encontrarInversoModular(int d, int z) {
        int t = 0, novoT = 1;
        int r = z, novoR = d;

        while (novoR != 0) {
            int quociente = r / novoR;

            // Atualizar r e t
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

    private int[] encontrandoP(String texto){
        int[] textoP = new int[texto.length()];
        String textoMinusculo = texto.toLowerCase();

        for(int i = 0; i < texto.length(); i++){
            textoP[i] = (int) textoMinusculo.charAt(i) - 96;
        }

        return textoP;
    }

    public int[] criptografia(String texto){
        int[] textoP = encontrandoP(texto);
        int[] textoCriptografado = new int[texto.length()];
        BigInteger[] textoCalculo = new BigInteger[texto.length()];
        BigInteger modBase = BigInteger.valueOf(this.n);
        BigInteger base;

        for(int i = 0; i < texto.length(); i++){
            base = BigInteger.valueOf(textoP[i]);
            textoCalculo[i] = base.pow(this.e);
            textoCalculo[i] = textoCalculo[i].mod(modBase);
        }

        for (int i = 0; i < textoCalculo.length; i++) {
            textoCriptografado[i] = textoCalculo[i].intValue(); // Converte BigInteger para int
        }

        return textoCriptografado;
    }

    public static void main(String[] args) {

        RSA obj = new RSA();
        Scanner sc = new Scanner(System.in);
        
        String texto = sc.nextLine();
        int[] textoCriptografado = obj.criptografia(texto);

        System.out.println("Escolhas das chaves: \np = " + obj.p + "\nq = " +  obj.q + "\nn = " + obj.n + "\nz = " + obj.z +"\nd = " + obj.d + "\ne = " + obj.e);

        System.out.print("[");
        for (int i = 0; i < textoCriptografado.length; i++) {
            System.out.print(textoCriptografado[i]);
            if (i < textoCriptografado.length - 1) { // Adicionar vírgula exceto no último elemento
                System.out.print(", ");
            }
        }
        System.out.println("]");
        sc.close();
    }
}