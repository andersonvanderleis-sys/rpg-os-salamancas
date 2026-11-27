import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static Random random = new Random();
    private static JogadorDAO dao = new JogadorDAO();
    private static int idJogadorBanco = -1;

    // Cores ANSI para estilizar o console
    private static final String RESET = "\u001B[0m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String VERDE_FORTE = "\u001B[92m";
    private static final String AMARELO = "\u001B[33m";
    private static final String ROXO = "\u001B[35m";
    private static final String CIANO = "\u001B[36m";
    private static final String NEGRITO = "\u001B[1m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Busca gangues, territórios e inimigos direto do MySQL
            List<Gangue> gangues = dao.listarGangues();
            List<Territorio> territorios = dao.listarTerritorios(gangues);
            List<Inimigo> inimigos = dao.listarInimigos();

            imprimirBanner();

            System.out.print(NEGRITO + "Digite o nome do seu personagem: " + RESET);
            String nomeJogador = scanner.nextLine();
            if (nomeJogador.trim().isEmpty()) {
                nomeJogador = "Salamanca";
            }

            System.out.println("\n" + AMARELO + "╔════════════════════════════════ ESCOLHA SUA GANGUE ════════════════════╗" + RESET);
            for (int i = 0; i < gangues.size(); i++) {
                Gangue g = gangues.get(i);
                System.out.println("  " + CIANO + (i + 1) + " - " + NEGRITO + g.getNome() + RESET
                        + " (Bônus Ataque: " + VERMELHO + "+" + g.getBonusAtaque() + RESET
                        + " | Dinheiro Inicial: " + VERDE + "$" + g.getDinheiroInicial() + RESET + ")");
            }
            System.out.println(AMARELO + "╚════════════════════════════════════════════════════════════════════════╝" + RESET);
            int escolhaGangue = lerOpcao(scanner, "Opção: ", 1, gangues.size()) - 1;
            Gangue gangueEscolhida = gangues.get(escolhaGangue);

            Jogador jogador = new Jogador(1, nomeJogador, 100, 100, gangueEscolhida.getDinheiroInicial(), 0, gangueEscolhida);

            // Salva o jogador no MySQL e pega o ID gerado
            idJogadorBanco = dao.salvarNovoJogador(jogador);
            System.out.println("\n" + VERDE + "✔ Bem-vindo ao cartel, " + NEGRITO + jogador.getNome() + RESET + VERDE
                    + " (" + jogador.getGangue().getNome() + ")! [Salvo no Banco ID #" + idJogadorBanco + "]" + RESET);

            boolean jogando = true;
            boolean zerouJogo = false;

            while (jogando && jogador.estaVivo()) {
                if (todosTerritoriosDominados(territorios, jogador.getGangue())) {
                    zerouJogo = true;
                    jogando = false;
                    break;
                }

                System.out.println("\n" + ROXO + "╔══════════════════════ STATUS DO CARTEL ══════════════════════╗" + RESET);
                System.out.println("  Vida:     " + gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima())
                        + " (" + jogador.getVidaAtual() + "/" + jogador.getVidaMaxima() + ")");
                System.out.println("  Dinheiro: " + VERDE + "$" + jogador.getDinheiro() + RESET
                        + " | Respeito: " + CIANO + jogador.getNivelRespeito() + RESET
                        + " | Itens na mochila: " + AMARELO + jogador.getInventario().size() + RESET);
                System.out.println(ROXO + "╠════════════════════════════ AÇÕES ═══════════════════════════╣" + RESET);
                System.out.println("  " + CIANO + "1" + RESET + " - 🔫 Invadir Território");
                System.out.println("  " + CIANO + "2" + RESET + " - 🎒 Abrir Mochila (Usar Item)");
                System.out.println("  " + CIANO + "3" + RESET + " - 💉 Recuperar Vida no Médico clandestino ($200)");
                System.out.println("  " + VERMELHO + "0" + RESET + " - 💾 Sair do Jogo");
                System.out.println(ROXO + "╚══════════════════════════════════════════════════════════════╝" + RESET);
                int opcao = lerOpcao(scanner, NEGRITO + "Escolha uma ação: " + RESET, 0, 3);

                if (opcao == 1) {
                    System.out.println("\n" + AMARELO + "╔═════════════════════════════ ESCOLHA O TERRITÓRIO PARA INVADIR ═══════════════════════════╗      " + RESET);
                    for (int i = 0; i < territorios.size(); i++) {
                        Territorio t = territorios.get(i);
                        System.out.println("  " + CIANO + (i + 1) + " - " + NEGRITO + t.getNome() + RESET
                                + " [Dono atual: " + AMARELO + t.getGangueDominante().getNome() + RESET + "]"
                                + " (Recompensa: " + VERDE + "$" + t.getRecompensaDinheiro() + RESET + ")");
                    }
                    System.out.println("  " + VERMELHO + "0" + RESET + " - Voltar");
                    System.out.println(AMARELO + "╚═══════════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
                    int escolhaTerritorio = lerOpcao(scanner, "Alvo: ", 0, territorios.size());
                    if (escolhaTerritorio == 0) continue;

                    Territorio alvo = territorios.get(escolhaTerritorio - 1);

                    if (alvo.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue()) {
                        System.out.println(VERDE + "✔ Este território já pertence à sua gangue!" + RESET);
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
                        System.out.println(VERDE + "🚩 Território sem defesa! Você assumiu o controle." + RESET);
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
                        System.out.println(VERDE + "✔ Vida restaurada ao máximo e salva no banco!" + RESET);
                    } else {
                        System.out.println(VERMELHO + "✖ Dinheiro insuficiente!" + RESET);
                    }
                } else if (opcao == 0) {
                    dao.atualizarJogador(idJogadorBanco, jogador);
                    jogando = false;
                }
            }

            if (zerouJogo) {
                dao.atualizarJogador(idJogadorBanco, jogador);
                imprimirTelaVitoriaFinal(jogador);
            } else if (!jogador.estaVivo()) {
                dao.atualizarJogador(idJogadorBanco, jogador);
                System.out.println("\n" + VERMELHO + NEGRITO + "💀 VOCÊ MORREU! Fim de jogo em Albuquerque." + RESET);
            } else {
                System.out.println("\n" + VERDE + "💾 Jogo encerrado. Progresso salvo no MySQL!" + RESET);
            }

        } catch (SQLException e) {
            System.out.println(VERMELHO + "Erro ao conectar ou salvar no banco de dados MySQL: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static boolean todosTerritoriosDominados(List<Territorio> territorios, Gangue gangueJogador) {
        for (Territorio t : territorios) {
            if (t.getGangueDominante() == null || t.getGangueDominante().getIdGangue() != gangueJogador.getIdGangue()) {
                return false;
            }
        }
        return true;
    }

    private static void imprimirTelaVitoriaFinal(Jogador jogador) {
        System.out.println("\n" + VERDE_FORTE + NEGRITO);
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 IMPÉRIO CONQUISTADO • FIM DE JOGO (VITÓRIA)              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Você derrotou todas as gangues rivais e agora é o DONO DO MORRO!        ║");
        System.out.println("║  Não sobrou ninguém para desafiar o trono do cartel.                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Chefe Supremo: " + String.format("%-57s", jogador.getNome()) + "║");
        System.out.println("║  Facção Vencedora: " + String.format("%-54s", jogador.getGangue().getNome()) + "║");
        System.out.println("║  Fortuna Acumulada: $" + String.format("%-52.2f", jogador.getDinheiro()) + "║");
        System.out.println("║  Respeito nas Ruas: " + String.format("%-53d", jogador.getNivelRespeito()) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    // Método que impede o jogo de quebrar se o usuário digitar número fora da lista ou letra
    private static int lerOpcao(Scanner scanner, String mensagem, int min, int max) {
        while (true) {
            System.out.print(mensagem);
            if (scanner.hasNextInt()) {
                int valor = scanner.nextInt();
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } else {
                scanner.next();
            }
            System.out.println(VERMELHO + "✖ Opção inválida! Digite um número entre " + min + " e " + max + "." + RESET);
        }
    }

    private static void batalhar(Scanner scanner, Jogador jogador, Inimigo inimigo, Territorio territorio) throws SQLException {
        int vidaMaxInimigo = Math.max(100, inimigo.getVidaAtual());

        System.out.println("\n" + VERMELHO + NEGRITO + "╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  >>> INVASÃO INICIADA EM: " + territorio.getNome().toUpperCase() + " <<<");
        System.out.println("  Chefe local: " + inimigo.getNome() + " (Vida: " + inimigo.getVidaAtual() + ")");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);

        while (jogador.estaVivo() && inimigo.estaVivo()) {
            System.out.println("\n----------------------------------------------------------------");
            System.out.println("  Você:               " + gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima())
                    + " (" + jogador.getVidaAtual() + "/" + jogador.getVidaMaxima() + ")");
            System.out.println("  " + String.format("%-18s", inimigo.getNome() + ":")
                    + gerarBarraVida(inimigo.getVidaAtual(), vidaMaxInimigo) + " (" + Math.max(0, inimigo.getVidaAtual()) + ")");
            System.out.println("----------------------------------------------------------------");
            System.out.println("  " + CIANO + "1" + RESET + " - 🔫 Atacar  |  " + CIANO + "2" + RESET + " - 🎒 Usar Item da Mochila  |  " + AMARELO + "3" + RESET + " - 🏃 Fugir");
            int acao = lerOpcao(scanner, "Ação: ", 1, 3);

            if (acao == 1) {
                int danoJogador = jogador.atacar();
                inimigo.setVidaAtual(inimigo.getVidaAtual() - danoJogador);
                System.out.println(VERDE + "💥 Você atacou " + inimigo.getNome() + " e causou " + danoJogador + " de dano!" + RESET);

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(VERMELHO + "🩸 " + inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!" + RESET);
                }
            } else if (acao == 2) {
                abrirMochila(scanner, jogador);
            } else {
                System.out.println(AMARELO + "🏃 Você recuou da invasão!" + RESET);
                dao.atualizarJogador(idJogadorBanco, jogador);
                return;
            }
        }

        if (jogador.estaVivo() && !inimigo.estaVivo()) {
            System.out.println("\n" + VERDE + NEGRITO + "🏆 VITÓRIA! Você derrotou " + inimigo.getNome() + " e dominou " + territorio.getNome() + "!" + RESET);
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
                System.out.println(AMARELO + "Nenhum item encontrado na cozinha do Los Pollos desta vez." + RESET);
            }
        } else if (territorio.getIdTerritorio() == 3) {
            if (chance <= 10) {
                jogador.adicionarItem(new Item(3, "Cristal Azul (Metanfetamina)", "BUFF_DANO", 2, 2));
                dao.adicionarItemInventario(idJogadorBanco, 3);
            } else if (chance <= 30) {
                jogador.adicionarItem(new Item(2, "Chá de Camomila da Lydia", "CURA", 25, 0));
                dao.adicionarItemInventario(idJogadorBanco, 2);
            } else {
                System.out.println(AMARELO + "Nenhum item encontrado no laboratório subterrâneo desta vez." + RESET);
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
            System.out.println(AMARELO + "🎒 Sua mochila está vazia!" + RESET);
            return;
        }
        System.out.println("\n" + CIANO + "╔══════════════════════ MOCHILA ══════════════════════╗" + RESET);
        for (int i = 0; i < mochila.size(); i++) {
            System.out.println("  " + CIANO + (i + 1) + " - " + NEGRITO + mochila.get(i).getNome() + RESET);
        }
        System.out.println("  " + VERMELHO + "0" + RESET + " - Voltar");
        System.out.println(CIANO + "╚═════════════════════════════════════════════════════╝" + RESET);
        int escolha = lerOpcao(scanner, "Escolha o item para usar: ", 0, mochila.size());
        if (escolha > 0) {
            jogador.usarItem(escolha - 1);
        }
    }

    private static void imprimirBanner() {
        System.out.println(VERDE_FORTE + NEGRITO);
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║   ██████╗ ███████╗    ███████╗ █████╗ ██╗      █████╗ ███╗   ███╗ █████╗ ███╗   ██╗ ██████╗ █████╗ ███████╗  ║");
        System.out.println("║  ██╔═══██╗██╔════╝    ██╔════╝██╔══██╗██║     ██╔══██╗████╗ ████║██╔══██╗████╗  ██║██╔════╝██╔══██╗██╔════╝  ║");
        System.out.println("║  ██║   ██║███████╗    ███████╗███████║██║     ███████║██╔████╔██║███████║██╔██╗ ██║██║     ███████║███████╗  ║");
        System.out.println("║  ██║   ██║╚════██║    ╚════██║██╔══██║██║     ██╔══██║██║╚██╔╝██║██╔══██║██║╚██╗██║██║     ██╔══██║╚════██║  ║");
        System.out.println("║  ╚██████╔╝███████║    ███████║██║  ██║███████╗██║  ██║██║ ╚═╝ ██║██║  ██║██║ ╚████║╚██████╗██║  ██║███████║  ║");
        System.out.println("║   ╚═════╝ ╚══════╝    ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝╚══════╝  ║");
        System.out.println("║                                      GUERRA DE TERRITÓRIOS: ALBUQUERQUE                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    private static String gerarBarraVida(int atual, int maxima) {
        int blocos = 10;
        int preenchidos = (int) Math.round(((double) Math.max(0, atual) / maxima) * blocos);
        String cor = atual > maxima * 0.5 ? VERDE : (atual > maxima * 0.25 ? AMARELO : VERMELHO);
        StringBuilder sb = new StringBuilder(cor + "[");
        for (int i = 0; i < blocos; i++) {
            sb.append(i < preenchidos ? "█" : "░");
        }
        sb.append("]" + RESET);
        return sb.toString();
    }
}