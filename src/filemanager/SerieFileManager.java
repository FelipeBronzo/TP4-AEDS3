package src.filemanager;
import java.io.*;

import src.model.Serie;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "dados/tvs.csv";

    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);

        // Verificar se o arquivo de dados já existe
        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {

                // Ignorar o cabeçalho do CSV
                bufferedReader.readLine();

                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);  // Preenche o objeto Serie com os dados do CSV

                    byte[] ba = serie.toByteArray();      // Converte a série para bytes

                    arq.writeBoolean(true);  // Marca o registro como ativo
                    arq.writeInt(ba.length);
                    arq.write(ba);           // Escreve os dados da série
                }

                System.out.println("Arquivo de dados carregado com sucesso.");
            } catch (FileNotFoundException e) {
                System.out.println("Arquivo CSV não encontrado: " + e.getMessage());
            }
        } else {
            System.out.println("Arquivo de dados já existe.");
        }
    }

    public Serie lerSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
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
                        return serie; // Retorna a série se o ID corresponder
                    }
                }
            }
        }
        return null; // ID não encontrado
    }

    public void adicionarSerie(Serie serie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            arq.seek(arq.length()); // Vai para o final do arquivo
            byte[] ba = serie.toByteArray();
            arq.writeBoolean(true); // Marca como ativo
            arq.writeInt(ba.length);
            arq.write(ba);
        }
    }

    public void atualizarSerie(int id, Serie novaSerie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
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
                        // Marcar o registro antigo como excluído
                        arq.seek(posicao);
                        arq.writeBoolean(false); // Marca como inativo

                        // Escrever o novo registro no final do arquivo
                        arq.seek(arq.length());
                        byte[] novoBa = novaSerie.toByteArray();
                        arq.writeBoolean(true);
                        arq.writeInt(novoBa.length);
                        arq.write(novoBa);
                        return; // Atualização concluída
                    }
                }
            }
            System.out.println("ID não encontrado para atualização.");
        }
    }

    public void excluirSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
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
                        return; // Exclusão concluída
                    }
                }
            }
            System.out.println("ID não encontrado para exclusão.");
        }
    }
}
