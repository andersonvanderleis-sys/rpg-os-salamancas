import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static Random random = new Random();
    private static JogadorDAO dao = new JogadorDAO();
    private static int idJogadorBanco = -1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Busca gangues, territórios e inimigos direto do MySQL
            List<Gangue> gangues = dao.listarGangues();
            List<Territorio> territorios = dao.listarTerritorios(gangues);
            List<Inimigo> inimigos = dao.listarInimigos();

            System.out.println("=== GUERRA DE TERRITÓRIOS: ALBUQUERQUE ===");
            System.out.print("Digite o nome do seu personagem: ");
            String nomeJogador = scanner.nextLine();

            System.out.println("\nEscolha sua Gangue:");
            for (int i = 0; i < gangues.size(); i++) {
                Gangue g = gangues.get(i);
                System.out.println((i + 1) + " - " + g.getNome() + " (Bônus Ataque: +" + g.getBonusAtaque() + " | Dinheiro Inicial: $" + g.getDinheiroInicial() + ")");
            }
            int escolhaGangue = scanner.nextInt() - 1;
            Gangue gangueEscolhida = gangues.get(escolhaGangue);

            Jogador jogador = new Jogador(1, nomeJogador, 100, 100, gangueEscolhida.getDinheiroInicial(), 0, gangueEscolhida);

            // Salva o jogador no MySQL e pega o ID gerado
            idJogadorBanco = dao.salvarNovoJogador(jogador);
            System.out.println("\nBem-vindo ao cartel, " + jogador.getNome() + " (" + jogador.getGangue().getNome() + ")! [Salvo no Banco ID #" + idJogadorBanco + "]");

            boolean jogando = true;
            while (jogando && jogador.estaVivo()) {
                System.out.println("\n-----------------------------------------");
                System.out.println("STATUS: Vida: " + jogador.getVidaAtual() + "/" + jogador.getVidaMaxima()
                        + " | Dinheiro: $" + jogador.getDinheiro()
                        + " | Respeito: " + jogador.getNivelRespeito()
                        + " | Itens na mochila: " + jogador.getInventario().size());
                System.out.println("1 - Invadir Território");
                System.out.println("2 - Abrir Mochila (Usar Item)");
                System.out.println("3 - Recuperar Vida no Médico clandestino ($200)");
                System.out.println("0 - Sair do Jogo");
                System.out.print("Escolha uma ação: ");
                int opcao = scanner.nextInt();

                if (opcao == 1) {
                    System.out.println("\nEscolha o território para invadir:");
                    for (int i = 0; i < territorios.size(); i++) {
                        Territorio t = territorios.get(i);
                        System.out.println((i + 1) + " - " + t.getNome()
                                + " [Dono atual: " + t.getGangueDominante().getNome() + "]"
                                + " (Recompensa: $" + t.getRecompensaDinheiro() + ")");
                    }
                    int escolhaTerritorio = scanner.nextInt() - 1;
                    Territorio alvo = territorios.get(escolhaTerritorio);

                    if (alvo.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue()) {
                        System.out.println("Este território já pertence à sua gangue!");
                        continue;
                    }

                    Inimigo chefe = null;
                    for (Inimigo ini : inimigos) {
                        if (ini.getIdTerritorio() == alvo.getIdTerritorio() && ini.estaVivo()) {
                            chefe = ini;
                            break;
                        }
                    }

                    if (chefe != null) {
                        batalhar(scanner, jogador, chefe, alvo);
                    } else {
                        System.out.println("Território sem defesa! Você assumiu o controle.");
                        alvo.setGangueDominante(jogador.getGangue());
                        dao.atualizarDonoTerritorio(alvo.getIdTerritorio(), jogador.getGangue().getIdGangue());
                    }

                } else if (opcao == 2) {
                    abrirMochila(scanner, jogador);
                    dao.atualizarJogador(idJogadorBanco, jogador);
                } else if (opcao == 3) {
                    if (jogador.getDinheiro() >= 200) {
                        jogador.setDinheiro(jogador.getDinheiro() - 200);
                        jogador.setVidaAtual(jogador.getVidaMaxima());
                        dao.atualizarJogador(idJogadorBanco, jogador);
                        System.out.println("Vida restaurada ao máximo e salva no banco!");
                    } else {
                        System.out.println("Dinheiro insuficiente!");
                    }
                } else if (opcao == 0) {
                    dao.atualizarJogador(idJogadorBanco, jogador);
                    jogando = false;
                }
            }

            if (!jogador.estaVivo()) {
                dao.atualizarJogador(idJogadorBanco, jogador);
                System.out.println("\nVOCÊ MORREU! Fim de jogo em Albuquerque.");
            } else {
                System.out.println("\nJogo encerrado. Progresso salvo no MySQL!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao conectar ou salvar no banco de dados MySQL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void batalhar(Scanner scanner, Jogador jogador, Inimigo inimigo, Territorio territorio) throws SQLException {
        System.out.println("\n>>> INVASÃO INICIADA EM: " + territorio.getNome() + " <<<");
        System.out.println("Chefe local: " + inimigo.getNome() + " (Vida: " + inimigo.getVidaAtual() + ")");

        while (jogador.estaVivo() && inimigo.estaVivo()) {
            System.out.println("\n[Sua Vida: " + jogador.getVidaAtual() + " | Vida de " + inimigo.getNome() + ": " + inimigo.getVidaAtual() + "]");
            System.out.println("1 - Atacar | 2 - Usar Item da Mochila | 3 - Fugir");
            int acao = scanner.nextInt();

            if (acao == 1) {
                int danoJogador = jogador.atacar();
                inimigo.setVidaAtual(inimigo.getVidaAtual() - danoJogador);
                System.out.println("Você atacou " + inimigo.getNome() + " e causou " + danoJogador + " de dano!");

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!");
                }
            } else if (acao == 2) {
                abrirMochila(scanner, jogador);
            } else {
                System.out.println("Você recuou da invasão!");
                dao.atualizarJogador(idJogadorBanco, jogador);
                return;
            }
        }

        if (jogador.estaVivo() && !inimigo.estaVivo()) {
            System.out.println("\nVITÓRIA! Você derrotou " + inimigo.getNome() + " e dominou " + territorio.getNome() + "!");
            territorio.setGangueDominante(jogador.getGangue());
            jogador.setDinheiro(jogador.getDinheiro() + territorio.getRecompensaDinheiro());
            jogador.setNivelRespeito(jogador.getNivelRespeito() + 10);

            dao.atualizarDonoTerritorio(territorio.getIdTerritorio(), jogador.getGangue().getIdGangue());
            dao.atualizarJogador(idJogadorBanco, jogador);

            sortearLootTerritorio(jogador, territorio);
        }
    }

    private static void sortearLootTerritorio(Jogador jogador, Territorio territorio) throws SQLException {
        int chance = random.nextInt(100) + 1;

        if (territorio.getIdTerritorio() == 2) {
            if (chance <= 50) {
                jogador.adicionarItem(new Item(1, "Balde do Los Pollos Hermanos", "CURA", 40, 0));
                dao.adicionarItemInventario(idJogadorBanco, 1);
            } else {
                System.out.println("Nenhum item encontrado na cozinha do Los Pollos desta vez.");
            }
        } else if (territorio.getIdTerritorio() == 3) {
            if (chance <= 10) {
                jogador.adicionarItem(new Item(3, "Cristal Azul (Metanfetamina)", "BUFF_DANO", 2, 2));
                dao.adicionarItemInventario(idJogadorBanco, 3);
            } else if (chance <= 30) {
                jogador.adicionarItem(new Item(2, "Chá de Camomila da Lydia", "CURA", 25, 0));
                dao.adicionarItemInventario(idJogadorBanco, 2);
            } else {
                System.out.println("Nenhum item encontrado no laboratório subterrâneo desta vez.");
            }
        } else if (territorio.getIdTerritorio() == 1) {
            if (chance <= 40) {
                jogador.adicionarItem(new Item(4, "Cantil do Deserto", "CURA", 30, 0));
                dao.adicionarItemInventario(idJogadorBanco, 4);
            }
        }
    }

    private static void abrirMochila(Scanner scanner, Jogador jogador) {
        List<Item> mochila = jogador.getInventario();
        if (mochila.isEmpty()) {
            System.out.println("Sua mochila está vazia!");
            return;
        }
        System.out.println("\n--- MOCHILA ---");
        for (int i = 0; i < mochila.size(); i++) {
            System.out.println((i + 1) + " - " + mochila.get(i).getNome());
        }
        System.out.println("0 - Voltar");
        System.out.print("Escolha o item para usar: ");
        int escolha = scanner.nextInt();
        if (escolha > 0 && escolha <= mochila.size()) {
            jogador.usarItem(escolha - 1);
        }
    }
}