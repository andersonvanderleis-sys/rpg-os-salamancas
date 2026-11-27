import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final Random random = new Random();
    private static final JogadorDAO dao = new JogadorDAO();
    private static int idJogadorBanco = -1;

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
            List<Gangue> gangues = dao.listarGangues();
            List<Territorio> territorios = dao.listarTerritorios(gangues);
            List<Inimigo> inimigos = dao.listarInimigos();

            imprimirBanner();

            System.out.print(NEGRITO + "Digite o nome do seu personagem: " + RESET);
            String nomeJogador = scanner.nextLine();
            if (nomeJogador.trim().isEmpty()) {
                nomeJogador = "Salamanca";
            }

            System.out.println("\n" + AMARELO + "╔════════════════════════════════ ESCOLHA SUA GANGUE ═════════════════════════╗" + RESET);
            for (int i = 0; i < gangues.size(); i++) {
                Gangue g = gangues.get(i);
                System.out.println("  " + CIANO + (i + 1) + " - " + NEGRITO + g.getNome() + RESET
                        + " (Bônus Ataque: " + VERMELHO + "+" + g.getBonusAtaque() + RESET
                        + " | Dinheiro Inicial: " + VERDE + "$" + g.getDinheiroInicial() + RESET + ")");
            }
            System.out.println(AMARELO + "╚═════════════════════════════════════════════════════════════════════════════╝" + RESET);
            int escolhaGangue = lerOpcao(scanner, "Opção: ", 1, gangues.size()) - 1;
            Gangue gangueEscolhida = gangues.get(escolhaGangue);

            Jogador jogador = new Jogador(1, nomeJogador, 100, 100, gangueEscolhida.getDinheiroInicial(), 0, gangueEscolhida);
            idJogadorBanco = dao.salvarNovoJogador(jogador);

            for (int i = 0; i < territorios.size(); i++) {
                Territorio t = territorios.get(i);
                Gangue rival = gangues.get((escolhaGangue + 1 + i) % gangues.size());
                t.setGangueDominante(rival);
                dao.atualizarDonoTerritorio(t.getIdTerritorio(), rival.getIdGangue());
            }

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

                int dominados = contarTerritoriosDominados(territorios, jogador.getGangue());

                System.out.println("\n" + ROXO + "╔══════════════════════ STATUS DO CARTEL ══════════════════════╗" + RESET);
                System.out.println("  Chefe:      " + NEGRITO + jogador.getNome() + RESET + " [" + AMARELO + obterPatente(jogador.getNivelRespeito()) + RESET + "]");
                System.out.println("  Domínio:    " + VERDE_FORTE + dominados + "/" + territorios.size() + " territórios" + RESET);
                System.out.println("  Vida:       " + gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima())
                        + " (" + jogador.getVidaAtual() + "/" + jogador.getVidaMaxima() + ")");
                System.out.println("  Dinheiro:   " + VERDE + "$" + jogador.getDinheiro() + RESET
                        + " | Respeito: " + CIANO + jogador.getNivelRespeito() + RESET
                        + " | Mochila: " + AMARELO + jogador.getInventario().size() + RESET);
                System.out.println(ROXO + "╠════════════════════════════ AÇÕES ═══════════════════════════╣" + RESET);
                System.out.println("  " + CIANO + "1" + RESET + " - 🔫 Invadir Território");
                System.out.println("  " + CIANO + "2" + RESET + " - 🎒 Abrir Mochila (Usar Item)");
                System.out.println("  " + CIANO + "3" + RESET + " - 💉 Recuperar Vida no Médico clandestino ($200)");
                System.out.println("  " + CIANO + "4" + RESET + " - 💼 Mercado Negro (Saul Goodman)");
                System.out.println("  " + VERMELHO + "0" + RESET + " - 💾 Sair do Jogo");
                System.out.println(ROXO + "╚══════════════════════════════════════════════════════════════╝" + RESET);
                int opcao = lerOpcao(scanner, NEGRITO + "Escolha uma ação: " + RESET, 0, 4);

                if (opcao == 1) {
                    System.out.println("\n" + AMARELO + "╔═════════════════════════════ ESCOLHA O TERRITÓRIO PARA INVADIR ═══════════════════════════╗" + RESET);
                    for (int i = 0; i < territorios.size(); i++) {
                        Territorio t = territorios.get(i);
                        boolean meu = t.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue();
                        String statusDono = meu
                                ? VERDE_FORTE + "✔ SEU TERRITÓRIO" + RESET
                                : AMARELO + t.getGangueDominante().getNome() + RESET;
                        System.out.println("  " + CIANO + (i + 1) + " - " + NEGRITO + t.getNome() + RESET
                                + " [" + statusDono + "]"
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
                        int vidaCapanga = 45 + (alvo.getIdTerritorio() * 10);
                        int danoCapanga = 8 + (alvo.getIdTerritorio() * 2);
                        Inimigo capanga = new Inimigo(900 + alvo.getIdTerritorio(), "Capanga de " + alvo.getGangueDominante().getNome(), vidaCapanga, danoCapanga, alvo.getIdTerritorio());

                        System.out.println("\n" + AMARELO + NEGRITO + "⚠️  ONDA 1/2: Um capanga bloqueia a entrada de " + alvo.getNome() + "!" + RESET);
                        boolean venceuCapanga = lutarContraInimigo(scanner, jogador, capanga, alvo, false);

                        if (venceuCapanga && jogador.estaVivo()) {
                            System.out.println("\n" + VERMELHO + NEGRITO + "🔥 ONDA 2/2: O chefe do território apareceu para defender o ponto!" + RESET);
                            boolean venceuChefe = lutarContraInimigo(scanner, jogador, chefe, alvo, true);

                            if (venceuChefe && jogador.estaVivo()) {
                                verificarContraAtaqueRival(scanner, jogador, territorios, gangues);
                            }
                        }
                    } else {
                        System.out.println(VERDE + "🚩 Território sem chefe vivo! Você assumiu o controle." + RESET);
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
                } else if (opcao == 4) {
                    abrirMercadoNegro(scanner, jogador);
                    dao.atualizarJogador(idJogadorBanco, jogador);
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

    private static int calcularRespeitoGanho(Territorio territorio) {
        int bonusGangueRival = (territorio.getGangueDominante() != null)
                ? territorio.getGangueDominante().getBonusAtaque()
                : 10;
        int id = territorio.getIdTerritorio();

        if (bonusGangueRival >= 14 || id >= 6) {
            return 11 + random.nextInt(5);
        } else if (bonusGangueRival >= 11 || id >= 3) {
            return 8 + random.nextInt(3);
        } else {
            return 3 + random.nextInt(5);
        }
    }

    private static int contarTerritoriosDominados(List<Territorio> territorios, Gangue gangueJogador) {
        int count = 0;
        for (Territorio t : territorios) {
            if (t.getGangueDominante() != null && t.getGangueDominante().getIdGangue() == gangueJogador.getIdGangue()) {
                count++;
            }
        }
        return count;
    }

    private static String obterPatente(int respeito) {
        if (respeito >= 50) return "👑 Dono de Albuquerque";
        if (respeito >= 30) return "💀 Chefão do Cartel";
        if (respeito >= 20) return "⭐ Tenente";
        if (respeito >= 10) return "🔫 Soldado";
        return "📦 Aviãozinho";
    }

    private static void abrirMercadoNegro(Scanner scanner, Jogador jogador) throws SQLException {
        System.out.println("\n" + AMARELO + "╔══════════════════════ MERCADO NEGRO (SAUL) ══════════════════════╗" + RESET);
        System.out.println("  Seu saldo: " + VERDE + "$" + jogador.getDinheiro() + RESET);
        System.out.println("  " + CIANO + "1" + RESET + " - 🦺 Colete Balístico (+25 Vida Máxima)          - " + VERDE + "$300" + RESET);
        System.out.println("  " + CIANO + "2" + RESET + " - 🍗 Balde Los Pollos (Cura +40 na mochila)      - " + VERDE + "$150" + RESET);
        System.out.println("  " + CIANO + "3" + RESET + " - 💎 Cristal Azul (Buff (2X) Dano  na mochila)    - " + VERDE + "$400" + RESET);
        System.out.println("  " + VERMELHO + "0" + RESET + " - Voltar");
        System.out.println(AMARELO + "╚══════════════════════════════════════════════════════════════════╝" + RESET);

        int escolha = lerOpcao(scanner, "Comprar item: ", 0, 3);
        if (escolha == 0) return;

        if (escolha == 1) {
            if (jogador.getDinheiro() >= 300) {
                jogador.setDinheiro(jogador.getDinheiro() - 300);
                jogador.setVidaMaxima(jogador.getVidaMaxima() + 25);
                jogador.setVidaAtual(jogador.getVidaAtual() + 25);
                System.out.println(VERDE + "✔ Colete equipado! Vida máxima subiu para " + jogador.getVidaMaxima() + "." + RESET);
            } else {
                System.out.println(VERMELHO + "✖ Dinheiro insuficiente!" + RESET);
            }
        } else if (escolha == 2) {
            if (jogador.getDinheiro() >= 150) {
                jogador.setDinheiro(jogador.getDinheiro() - 150);
                jogador.adicionarItem(new Item(1, "Balde do Los Pollos Hermanos", "CURA", 40, 0));
                dao.adicionarItemInventario(idJogadorBanco, 1);
                System.out.println(VERDE + "✔ Balde do Los Pollos adicionado à mochila!" + RESET);
            } else {
                System.out.println(VERMELHO + "✖ Dinheiro insuficiente!" + RESET);
            }
        } else if (escolha == 3) {
            if (jogador.getDinheiro() >= 400) {
                jogador.setDinheiro(jogador.getDinheiro() - 400);
                jogador.adicionarItem(new Item(3, "Cristal Azul (Metanfetamina)", "BUFF_DANO", 2, 2));
                dao.adicionarItemInventario(idJogadorBanco, 3);
                System.out.println(VERDE + "✔ Cristal Azul adicionado à mochila!" + RESET);
            } else {
                System.out.println(VERMELHO + "✖ Dinheiro insuficiente!" + RESET);
            }
        }
    }

    private static void verificarContraAtaqueRival(Scanner scanner, Jogador jogador, List<Territorio> territorios, List<Gangue> gangues) throws SQLException {
        List<Territorio> meusTerritorios = new ArrayList<>();
        for (Territorio t : territorios) {
            if (t.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue()) {
                meusTerritorios.add(t);
            }
        }

        if (meusTerritorios.size() >= 2 && meusTerritorios.size() < territorios.size() && random.nextInt(100) < 35) {
            Territorio atacado = meusTerritorios.get(random.nextInt(meusTerritorios.size()));
            Gangue invasora = gangues.get(random.nextInt(gangues.size()));
            while (invasora.getIdGangue() == jogador.getGangue().getIdGangue()) {
                invasora = gangues.get(random.nextInt(gangues.size()));
            }

            System.out.println("\n" + VERMELHO + NEGRITO + "╔══════════════════════ ALERTA DE CONTRA-ATAQUE ══════════════════════╗");
            System.out.println("  🚨 A gangue " + invasora.getNome() + " está atacando seu território: " + atacado.getNome() + "!");
            System.out.println("╚═════════════════════════════════════════════════════════════════════╝" + RESET);
            System.out.println("  " + CIANO + "1" + RESET + " - 🛡️ Defender o território agora");
            System.out.println("  " + AMARELO + "2" + RESET + " - 🏳️ Abandonar o território para eles");
            int escolha = lerOpcao(scanner, "Decisão: ", 1, 2);

            if (escolha == 1) {
                Inimigo liderAtaque = new Inimigo(999, "Tenente de " + invasora.getNome(), 85, 15, atacado.getIdTerritorio());
                boolean defendeu = lutarContraInimigo(scanner, jogador, liderAtaque, atacado, false);
                if (defendeu && jogador.estaVivo()) {
                    int respeitoDefesa = invasora.getBonusAtaque() >= 14 ? 8 : 4;
                    System.out.println(VERDE_FORTE + "✔ Você repeliu o contra-ataque de " + invasora.getNome() + " e manteve " + atacado.getNome() + " (+" + respeitoDefesa + " Respeito)!" + RESET);
                    jogador.setNivelRespeito(jogador.getNivelRespeito() + respeitoDefesa);
                    dao.atualizarJogador(idJogadorBanco, jogador);
                } else if (jogador.estaVivo()) {
                    atacado.setGangueDominante(invasora);
                    dao.atualizarDonoTerritorio(atacado.getIdTerritorio(), invasora.getIdGangue());
                    System.out.println(VERMELHO + "✖ Você recuou e perdeu o controle de " + atacado.getNome() + "!" + RESET);
                }
            } else {
                atacado.setGangueDominante(invasora);
                dao.atualizarDonoTerritorio(atacado.getIdTerritorio(), invasora.getIdGangue());
                System.out.println(AMARELO + "🏳️ Você abandonou " + atacado.getNome() + " para " + invasora.getNome() + "." + RESET);
            }
        }
    }

    private static void verificarEventoAleatorio(Jogador jogador) {
        int sorteio = random.nextInt(100) + 1;

        if (sorteio <= 25) {
            double suborno = Math.min(jogador.getDinheiro(), 100);
            if (suborno > 0) {
                jogador.setDinheiro(jogador.getDinheiro() - suborno);
                System.out.println("\n" + VERMELHO + "🚨 BATIDA DA DEA! Você pagou $" + suborno + " de suborno para escapar da emboscada." + RESET);
            } else {
                int dano = 15;
                jogador.setVidaAtual(Math.max(1, jogador.getVidaAtual() - dano));
                System.out.println("\n" + VERMELHO + "🚨 BATIDA DA DEA! Sem dinheiro para suborno, você fugiu trocando tiros (-" + dano + " de vida)." + RESET);
            }
        } else if (sorteio <= 50) {
            double bonus = 150;
            jogador.setDinheiro(jogador.getDinheiro() + bonus);
            System.out.println("\n" + VERDE_FORTE + "💰 CARREGAMENTO EXTRA! Um informante deixou $" + bonus + " escondidos no território!" + RESET);
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

    private static boolean lutarContraInimigo(Scanner scanner, Jogador jogador, Inimigo inimigo, Territorio territorio, boolean conquistarAoVencer) throws SQLException {
        int vidaMaxInimigo = Math.max(50, inimigo.getVidaAtual());

        System.out.println("\n" + VERMELHO + NEGRITO + "╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  >>> CONFRONTO EM: " + territorio.getNome().toUpperCase() + " <<<");
        System.out.println("  Alvo: " + inimigo.getNome() + " (Vida: " + inimigo.getVidaAtual() + ")");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);

        while (jogador.estaVivo() && inimigo.estaVivo()) {
            System.out.println("\n----------------------------------------------------------------");
            System.out.println("  Você:               " + gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima())
                    + " (" + jogador.getVidaAtual() + "/" + jogador.getVidaMaxima() + ")");
            System.out.println("  " + String.format("%-18s", inimigo.getNome() + ":")
                    + gerarBarraVida(inimigo.getVidaAtual(), vidaMaxInimigo) + " (" + Math.max(0, inimigo.getVidaAtual()) + ")");
            System.out.println("----------------------------------------------------------------");
            System.out.println("  " + CIANO + "1" + RESET + " - 🔫 Atacar  |  " + ROXO + "2" + RESET + " - 🎯 Tiro Certeiro (Crítico)  |  " + CIANO + "3" + RESET + " - 🎒 Mochila  |  " + AMARELO + "4" + RESET + " - 🏃 Fugir");
            int acao = lerOpcao(scanner, "Ação: ", 1, 4);

            if (acao == 1) {
                int bonusPatente = jogador.getNivelRespeito() / 10 * 2;
                int danoJogador = jogador.atacar() + bonusPatente;
                inimigo.setVidaAtual(inimigo.getVidaAtual() - danoJogador);
                System.out.println(VERDE + "💥 Você atacou " + inimigo.getNome() + " e causou " + danoJogador + " de dano!" + RESET);

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(VERMELHO + "🩸 " + inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!" + RESET);
                }
            } else if (acao == 2) {
                if (random.nextInt(100) < 55) {
                    int bonusPatente = jogador.getNivelRespeito() / 10 * 2;
                    int danoCritico = (jogador.atacar() + bonusPatente) * 2;
                    inimigo.setVidaAtual(inimigo.getVidaAtual() - danoCritico);
                    System.out.println(VERDE_FORTE + NEGRITO + "🎯 TIRO CERTEIRO! Dano crítico de " + danoCritico + " em " + inimigo.getNome() + "!" + RESET);
                } else {
                    System.out.println(AMARELO + "💨 Você errou o tiro certeiro e perdeu a abertura!" + RESET);
                }

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(VERMELHO + "🩸 " + inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!" + RESET);
                }
            } else if (acao == 3) {
                abrirMochila(scanner, jogador);
            } else {
                System.out.println(AMARELO + "🏃 Você recuou do confronto!" + RESET);
                dao.atualizarJogador(idJogadorBanco, jogador);
                return false;
            }
        }

        if (jogador.estaVivo() && !inimigo.estaVivo()) {
            if (conquistarAoVencer) {
                int respeitoGanho = calcularRespeitoGanho(territorio);
                String gangueDerrotada = (territorio.getGangueDominante() != null) ? territorio.getGangueDominante().getNome() : "Rival";

                System.out.println("\n" + VERDE + NEGRITO + "🏆 VITÓRIA! Você derrotou " + inimigo.getNome() + " (" + gangueDerrotada + ") e dominou " + territorio.getNome() + "!" + RESET);
                System.out.println(CIANO + "⭐ Respeito ganho: +" + respeitoGanho + " (Total: " + (jogador.getNivelRespeito() + respeitoGanho) + ")" + RESET);

                territorio.setGangueDominante(jogador.getGangue());
                jogador.setDinheiro(jogador.getDinheiro() + territorio.getRecompensaDinheiro());
                jogador.setNivelRespeito(jogador.getNivelRespeito() + respeitoGanho);

                sortearLootTerritorio(jogador, territorio);
                verificarEventoAleatorio(jogador);

                dao.atualizarDonoTerritorio(territorio.getIdTerritorio(), jogador.getGangue().getIdGangue());
                dao.atualizarJogador(idJogadorBanco, jogador);
            } else {
                System.out.println(VERDE + "✔ Você eliminou " + inimigo.getNome() + "!" + RESET);
            }
            return true;
        }
        return false;
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
        } else {
            if (chance <= 45) {
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