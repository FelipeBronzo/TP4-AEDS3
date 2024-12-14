package src.main;

import java.io.IOException;
import java.util.Scanner;

import src.model.Serie;
import src.util.CiframentoColuna;
import src.util.RSA;
import src.filemanager.SerieFileManager;

import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        SerieFileManager fileManager = new SerieFileManager();
        RSA rsa = new RSA(); // Instância de RSA para criptografia
        CiframentoColuna ciframento = null; // Instância será criada quando necessário

        try {
            fileManager.carregarArquivo();
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo: " + e.getMessage());
        }

        int operacao;
        do {
            System.out.println("\nEscolha a operação: ");
            System.out.println("1 - Carregar");
            System.out.println("2 - Ler");
            System.out.println("3 - Atualizar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Criar");
            System.out.println("6 - Criptografar Arquivo (RSA)");
            System.out.println("7 - Criptografar Arquivo (Ciframento de Colunas)");
            System.out.println("8 - Sair");
            System.out.print("Operação: ");
            operacao = entrada.nextInt();
            entrada.nextLine();

            try {
                switch (operacao) {
                    case 1 -> {
                        fileManager.carregarArquivo();
                        System.out.println("Arquivo de dados carregado com sucesso.");
                    }
                    case 2 -> {
                        System.out.print("ID da série para ler: ");
                        int id = entrada.nextInt();
                        entrada.nextLine(); // Limpar o buffer
                        Serie serie = fileManager.lerSerie(id, rsa, ciframento);
                        System.out.println(serie != null ? serie : "Série não encontrada.");
                    }
                    case 3 -> {
                        System.out.print("ID da série para atualizar: ");
                        int id = entrada.nextInt();
                        entrada.nextLine();

                        Serie novaSerie = obterDadosSerie(id, entrada);
                        fileManager.atualizarSerie(id, novaSerie, rsa, ciframento);
                        System.out.println("Série atualizada com sucesso.");
                    }
                    case 4 -> {
                        System.out.print("ID da série para excluir: ");
                        int id = entrada.nextInt();
                        fileManager.excluirSerie(id);
                        System.out.println("Série excluída com sucesso.");
                    }
                    case 5 -> {
                        System.out.print("ID da série para criar: ");
                        int id = entrada.nextInt();
                        entrada.nextLine();

                        Serie novaSerie = obterDadosSerie(id, entrada);
                        fileManager.adicionarSerie(novaSerie, rsa, ciframento);
                        System.out.println("Série adicionada com sucesso.");
                    }
                    case 6 -> {
                        System.out.println("Iniciando criptografia com RSA...");
                        fileManager.criptografarArquivo("RSA", rsa, null);
                        System.out.println("Arquivo criptografado com RSA.");
                    }
                    case 7 -> {
                        System.out.println("Informe a chave para o Ciframento de Colunas: ");
                        String chave = entrada.nextLine();
                        ciframento = new CiframentoColuna(chave);
                        System.out.println("Iniciando criptografia com Ciframento de Colunas...");
                        fileManager.criptografarArquivo("COL", null, ciframento);
                        System.out.println("Arquivo criptografado com Ciframento de Colunas.");
                    }
                    case 8 -> System.out.println("Saindo...");
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (IOException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (operacao != 8);

        entrada.close();
    }


    private static Serie obterDadosSerie(int id, Scanner entrada) {
        System.out.print("Nome da série: ");
        String name = entrada.nextLine();

        System.out.print("Linguagem: ");
        String language = entrada.nextLine();

        Date firstAirDate = null;
        while (firstAirDate == null) {
            System.out.print("Data de estreia (dd/MM/yyyy): ");
            String dateStr = entrada.nextLine();
            try {
                firstAirDate = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                System.out.println("Formato de data inválido. Tente novamente.");
            }
        }

        ArrayList<String> companies = new ArrayList<>();
        System.out.println("Insira os nomes das companhias (digite 'fim' para parar):");
        while (true) {
            System.out.print("Companhia: ");
            String company = entrada.nextLine();
            if (company.equalsIgnoreCase("fim")) break;
            companies.add(company);
        }

        return new Serie(id, name, language, firstAirDate, companies);
    }
}