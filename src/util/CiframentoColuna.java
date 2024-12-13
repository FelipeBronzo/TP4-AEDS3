package src.util;
import java.util.Arrays;

class CiframentoColuna {

    private String chave;

    CiframentoColuna(String chave){
        this.chave = chave.toLowerCase();
    }

    private char[][] construindoMatriz(String texto) {
        String textoSemEspaco = texto.replaceAll(" ", "");
        int linhas = (textoSemEspaco.length() + chave.length() - 1) / chave.length(); // Calcula o número de linhas
        char[][] matriz = new char[linhas][chave.length()];
        int x = 0;

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < chave.length(); j++) {
                // Preenche a matriz, usando espaços em branco se faltar caracteres
                if (x < textoSemEspaco.length()) {
                    matriz[i][j] = textoSemEspaco.charAt(x++);
                } else {
                    matriz[i][j] = ' '; // Preenche com espaços vazios
                }
            }
        }
        return matriz;
    }

    String criptografia(char[][] matriz) {
    // Determinar a ordem das colunas com base na chave
    Integer[] ordem = new Integer[chave.length()];
    for (int i = 0; i < chave.length(); i++) {
        ordem[i] = i;
    }

    // Ordenar os índices com base nos caracteres da chave
    Arrays.sort(ordem, (a, b) -> Character.compare(chave.charAt(a), chave.charAt(b)));

    // Concatenar as colunas na ordem definida pela chave
    StringBuilder resultado = new StringBuilder();
    for (int coluna : ordem) {
        for (int i = 0; i < matriz.length; i++) {
            if(matriz[i][coluna] != ' '){
                resultado.append(matriz[i][coluna]);
            }
            
        }
    }

    return resultado.toString();
}

    public static void main(String[] args) {

        CiframentoColuna obj = new CiframentoColuna("Rena");

        char[][] matriz = obj.construindoMatriz("ABCDEFGHIJKLMNOPQRS");

        for (int i = 0; i < matriz.length; i++) { // Percorre as linhas
            for (int j = 0; j < matriz[i].length; j++) { // Percorre as colunas
                System.out.print(matriz[i][j] + " "); // Imprime cada elemento com um espaço
            }
            System.out.println(); // Nova linha após cada linha da matriz
        }

        String resultado = obj.criptografia(matriz);
        System.out.println("\nTexto final criptografado: " + resultado);
        
    }

    
}
