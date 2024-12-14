package src.util;
import java.util.Arrays;

public class CiframentoColuna {

    private String chave;

    public CiframentoColuna(String chave){
        this.chave = chave.toLowerCase();
    }

    public char[][] construindoMatriz(String texto) {
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

    public String criptografia(char[][] matriz) {
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

    public String descriptografia(String textoCriptografado) {
        int numLinhas = (textoCriptografado.length() + chave.length() - 1) / chave.length();
        int numColunas = chave.length();
        int numEspacosVazios = (numLinhas * numColunas) - textoCriptografado.length();
        char[][] matriz = new char[numLinhas][numColunas];

        // Determinar a ordem das colunas com base na chave
        Integer[] ordem = new Integer[numColunas];
        for (int i = 0; i < numColunas; i++) {
            ordem[i] = i;
        }

        // Ordenar os índices com base nos caracteres da chave
        Arrays.sort(ordem, (a, b) -> Character.compare(chave.charAt(a), chave.charAt(b)));

        // Preencher a matriz por colunas na ordem definida pela chave
        int x = 0;
        for (int coluna : ordem) {
            for (int linha = 0; linha < numLinhas; linha++) {
                // Não preencher as células que correspondem aos espaços em branco no final
                if (linha == numLinhas - 1 && coluna >= numColunas - numEspacosVazios) {
                    matriz[linha][coluna] = ' ';
                } else if (x < textoCriptografado.length()) {
                    matriz[linha][coluna] = textoCriptografado.charAt(x++);
                }
            }
        }

        // Reconstruir o texto original percorrendo a matriz linha por linha
        StringBuilder textoOriginal = new StringBuilder();
        for (int i = 0; i < numLinhas; i++) { // Percorrer as linhas
            for (int j = 0; j < numColunas; j++) { // Percorrer as colunas
                if (matriz[i][j] != ' ') {
                    textoOriginal.append(matriz[i][j]);
                }
            }
        }

        return textoOriginal.toString();
    }

}
