package src.filemanager;

import java.io.*;
import src.model.Serie;
import src.util.RSA;
import src.util.CiframentoColuna;

public class SerieFileManager {
    // Caminho dos arquivos de dados
    private final String dbPath = "dados/series.db"; // Arquivo original
    private final String csvPath = "dados/tvs.csv"; // CSV inicial
    private final String rsaEncryptedDbPath = "dados/series_rsa.db"; // Arquivo criptografado com RSA
    private final String colEncryptedDbPath = "dados/series_col.db"; // Arquivo criptografado com Ciframento de Colunas
    private String currentDbPath; // Caminho do arquivo atualmente em uso

    // Enum para representar o tipo de criptografia ativa
    private enum EncryptionType {
        NONE, // Sem criptografia
        RSA,  // Criptografia RSA
        COL   // Ciframento de Colunas
    }

    private EncryptionType currentEncryption; // Tipo de criptografia atual

    // Construtor
    public SerieFileManager() {
        this.currentDbPath = dbPath; // Inicialmente usa o arquivo original
        this.currentEncryption = EncryptionType.NONE; // Sem criptografia ativa
    }

    // Atualiza o arquivo ativo baseado na existência de arquivos criptografados
    private void atualizarArquivoAtivo() {
        if (new File(rsaEncryptedDbPath).exists()) {
            currentDbPath = rsaEncryptedDbPath;
            currentEncryption = EncryptionType.RSA;
        } else if (new File(colEncryptedDbPath).exists()) {
            currentDbPath = colEncryptedDbPath;
            currentEncryption = EncryptionType.COL;
        } else {
            currentDbPath = dbPath;
            currentEncryption = EncryptionType.NONE;
        }
    }

    // Carrega o arquivo inicial a partir do CSV, se necessário
    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {
                bufferedReader.readLine(); // Ignora o cabeçalho do CSV
                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha); // Preenche o objeto `Serie` com os dados da linha
                    byte[] ba = serie.toByteArray();
                    arq.writeBoolean(true); // Marca como ativo
                    arq.writeInt(ba.length);
                    arq.write(ba); // Escreve o registro no arquivo
                }
                System.out.println("Arquivo de dados carregado com sucesso.");
            }
        } else {
            System.out.println("Arquivo de dados já existe.");
        }
    }

    // Lê uma série pelo ID
    public Serie lerSerie(int id, RSA rsa, CiframentoColuna ciframento) throws IOException {
        atualizarArquivoAtivo(); // Certifica-se de usar o arquivo correto
        try (RandomAccessFile arq = new RandomAccessFile(currentDbPath, "r")) {
            arq.seek(0);
            while (arq.getFilePointer() < arq.length()) {
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) { // Verifica se o registro é válido
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);

                    // Descriptografar o nome, se necessário
                    if (currentEncryption == EncryptionType.RSA) {
                        String[] nomeCripto = serie.getName().split(" ");
                        int[] nomeCriptoInt = new int[nomeCripto.length];
                        for (int i = 0; i < nomeCripto.length; i++) {
                            nomeCriptoInt[i] = Integer.parseInt(nomeCripto[i]);
                        }
                        serie.setName(rsa.descriptografar(nomeCriptoInt));
                    } else if (currentEncryption == EncryptionType.COL) {
                        serie.setName(ciframento.descriptografia(serie.getName()));
                    }

                    if (serie.getId() == id) {
                        return serie; // Retorna a série correspondente
                    }
                }
            }
        }
        return null; // Série não encontrada
    }

    // Adiciona uma nova série
    public void adicionarSerie(Serie serie, RSA rsa, CiframentoColuna ciframento) throws IOException {
        atualizarArquivoAtivo(); // Certifica-se de usar o arquivo correto
        try (RandomAccessFile arq = new RandomAccessFile(currentDbPath, "rw")) {
            arq.seek(arq.length()); // Vai para o final do arquivo

            // Criptografar o nome, se necessário
            if (currentEncryption == EncryptionType.RSA) {
                int[] nomeCriptografado = rsa.criptografia(serie.getName());
                StringBuilder nomeCriptoString = new StringBuilder();
                for (int num : nomeCriptografado) {
                    nomeCriptoString.append(num).append(" ");
                }
                serie.setName(nomeCriptoString.toString().trim());
            } else if (currentEncryption == EncryptionType.COL) {
                String textoCriptografado = ciframento.criptografia(ciframento.construindoMatriz(serie.getName()));
                serie.setName(textoCriptografado);
            }

            byte[] ba = serie.toByteArray();
            arq.writeBoolean(true); // Marca como ativo
            arq.writeInt(ba.length);
            arq.write(ba); // Escreve a nova série no arquivo
        }
    }

    // Atualiza uma série existente
    public void atualizarSerie(int id, Serie novaSerie, RSA rsa, CiframentoColuna ciframento) throws IOException {
        atualizarArquivoAtivo(); // Certifica-se de usar o arquivo correto
        try (RandomAccessFile arq = new RandomAccessFile(currentDbPath, "rw")) {
            arq.seek(0);
            while (arq.getFilePointer() < arq.length()) {
                long posicao = arq.getFilePointer();
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        arq.seek(posicao);
                        arq.writeBoolean(false); // Marca como inativo

                        // Criptografar o nome, se necessário
                        if (currentEncryption == EncryptionType.RSA) {
                            int[] nomeCriptografado = rsa.criptografia(novaSerie.getName());
                            StringBuilder nomeCriptoString = new StringBuilder();
                            for (int num : nomeCriptografado) {
                                nomeCriptoString.append(num).append(" ");
                            }
                            novaSerie.setName(nomeCriptoString.toString().trim());
                        } else if (currentEncryption == EncryptionType.COL) {
                            String textoCriptografado = ciframento.criptografia(ciframento.construindoMatriz(novaSerie.getName()));
                            novaSerie.setName(textoCriptografado);
                        }

                        arq.seek(arq.length());
                        byte[] novoBa = novaSerie.toByteArray();
                        arq.writeBoolean(true);
                        arq.writeInt(novoBa.length);
                        arq.write(novoBa);
                        return;
                    }
                }
            }
            System.out.println("ID não encontrado para atualização.");
        }
    }

    // Exclui uma série pelo ID
    public void excluirSerie(int id) throws IOException {
        atualizarArquivoAtivo(); // Certifica-se de usar o arquivo correto
        try (RandomAccessFile arq = new RandomAccessFile(currentDbPath, "rw")) {
            arq.seek(0);
            while (arq.getFilePointer() < arq.length()) {
                long posicao = arq.getFilePointer();
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        arq.seek(posicao);
                        arq.writeBoolean(false); // Marca o registro como inativo
                        System.out.println("Registro com ID " + id + " excluído com sucesso.");
                        return;
                    }
                }
            }
            System.out.println("ID não encontrado para exclusão.");
        }
    }

    // Criptografa o arquivo com o método escolhido
    public void criptografarArquivo(String metodo, RSA rsa, CiframentoColuna ciframento) throws IOException {
        File dbFile = new File(dbPath);
        File newDbFile = metodo.equals("RSA") ? new File(rsaEncryptedDbPath) : new File(colEncryptedDbPath);

        if (!dbFile.exists()) {
            System.out.println("Arquivo de dados não encontrado para criptografar.");
            return;
        }

        try (RandomAccessFile arq = new RandomAccessFile(dbFile, "r");
             RandomAccessFile newArq = new RandomAccessFile(newDbFile, "rw")) {

            arq.seek(0);
            System.out.println("Iniciando a criação do arquivo criptografado com " + metodo + "...");

            while (arq.getFilePointer() < arq.length()) {
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);

                    // Aplica a criptografia escolhida
                    if (metodo.equals("RSA")) {
                        int[] nomeCriptografado = rsa.criptografia(serie.getName());
                        StringBuilder nomeCriptoString = new StringBuilder();
                        for (int num : nomeCriptografado) {
                            nomeCriptoString.append(num).append(" ");
                        }
                        serie.setName(nomeCriptoString.toString().trim());
                    } else if (metodo.equals("COL")) {
                        String textoCriptografado = ciframento.criptografia(ciframento.construindoMatriz(serie.getName()));
                        serie.setName(textoCriptografado);
                    }

                    byte[] novoBa = serie.toByteArray();
                    newArq.writeBoolean(true);
                    newArq.writeInt(novoBa.length);
                    newArq.write(novoBa);
                }
            }
            System.out.println("Arquivo criptografado criado com sucesso: " + newDbFile.getAbsolutePath());
        }
    }
}
