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
    private static final String VERMELHO_FORTE = "\u001B[91m";
    private static final String VERDE = "\u001B[32m";
    private static final String VERDE_FORTE = "\u001B[92m";
    private static final String AMARELO = "\u001B[33m";
    private static final String DOURADO = "\u001B[38;2;255;215;0m";
    private static final String ROXO = "\u001B[35m";
    private static final String CIANO = "\u001B[36m";
    private static final String CINZA = "\u001B[90m";
    private static final String NEGRITO = "\u001B[1m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            List<Gangue> gangues = dao.listarGangues();
            List<Territorio> territorios = dao.listarTerritorios(gangues);
            List<Inimigo> inimigos = dao.listarInimigos();

            imprimirBanner();

            System.out.print(DOURADO + NEGRITO + "  ➤ Digite o nome do seu personagem: " + RESET);
            String nomeJogador = scanner.nextLine();
            if (nomeJogador.trim().isEmpty()) {
                nomeJogador = "Salamanca";
            }

            System.out.println("\n" + DOURADO + "╔══════════════════════════════ ESCOLHA SUA GANGUE ══════════════════════════════╗" + RESET);
            for (int i = 0; i < gangues.size(); i++) {
                Gangue g = gangues.get(i);
                String linha = String.format("  %s[%d]%s %s%-27s%s | Ataque: %s+%2d%s | Caixa Inicial: %s$%6.2f%s",
                        CIANO, (i + 1), RESET,
                        NEGRITO, g.getNome(), RESET,
                        VERMELHO_FORTE, g.getBonusAtaque(), RESET,
                        VERDE_FORTE, g.getDinheiroInicial(), RESET);
                linhaCaixa(DOURADO, linha, 80);
            }
            System.out.println(DOURADO + "╚════════════════════════════════════════════════════════════════════════════════╝" + RESET);          int escolhaGangue = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Opção: " + RESET, 1, gangues.size()) - 1;
            Gangue gangueEscolhida = gangues.get(escolhaGangue);

            animarCarregamento("Registrando cartel no banco de dados de Albuquerque");

            Jogador jogador = new Jogador(1, nomeJogador, 100, 100, gangueEscolhida.getDinheiroInicial(), 0, gangueEscolhida);
            idJogadorBanco = dao.salvarNovoJogador(jogador);

            for (int i = 0; i < territorios.size(); i++) {
                Territorio t = territorios.get(i);
                Gangue rival = gangues.get((escolhaGangue + 1 + i) % gangues.size());
                t.setGangueDominante(rival);
                dao.atualizarDonoTerritorio(t.getIdTerritorio(), rival.getIdGangue());
            }

            digitarLento("\n" + VERDE_FORTE + NEGRITO + "✔ Bem-vindo ao cartel, " + jogador.getNome()
                    + " (" + jogador.getGangue().getNome() + ")! [ID #" + idJogadorBanco + "]" + RESET, 15);

            boolean jogando = true;
            boolean zerouJogo = false;

            while (jogando && jogador.estaVivo()) {
                if (todosTerritoriosDominados(territorios, jogador.getGangue())) {
                    zerouJogo = true;
                    jogando = false;
                    break;
                }

                int dominados = contarTerritoriosDominados(territorios, jogador.getGangue());
                String patente = obterPatente(jogador.getNivelRespeito());

                imprimirRadarTerritorios(territorios, jogador.getGangue());

                System.out.println("\n" + ROXO + "╔═══════════════════════════════════ STATUS DO CARTEL ═══════════════════════════════════╗" + RESET);
                linhaCaixa(ROXO, String.format("  Chefe:    %s%-24s%s  Patente: %s%s%s",
                        NEGRITO, jogador.getNome(), RESET, DOURADO, patente, RESET), 88);
                linhaCaixa(ROXO, String.format("  Domínio:  %s%d/%d territórios%s           Mochila: %s%d item(ns)%s",
                        VERDE_FORTE, dominados, territorios.size(), RESET, AMARELO, jogador.getInventario().size(), RESET), 88);

                linhaCaixa(ROXO, String.format("  Vida:     %s (%3d/%3d)",
                        gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima()), jogador.getVidaAtual(), jogador.getVidaMaxima()), 88);
                linhaCaixa(ROXO, String.format("  Dinheiro: %s$%-12.2f%s             Respeito: %s%d%s",
                        VERDE_FORTE, jogador.getDinheiro(), RESET, CIANO, jogador.getNivelRespeito(), RESET), 88);
                System.out.println(ROXO + "╠════════════════════════════════════════ AÇÕES ═════════════════════════════════════════╣" + RESET);
                linhaCaixa(ROXO, "  " + CIANO + "[1]" + RESET + " 🔫 Invadir Território", 88);
                linhaCaixa(ROXO, "  " + CIANO + "[2]" + RESET + " 🎒 Abrir Mochila (Usar Item)", 88);
                linhaCaixa(ROXO, "  " + CIANO + "[3]" + RESET + " 💉 Médico Clandestino (Recuperar Vida Total - " + VERDE + "$200" + RESET + ")", 88);
                linhaCaixa(ROXO, "  " + CIANO + "[4]" + RESET + " 💼 Mercado Negro (Saul Goodman)", 88);
                linhaCaixa(ROXO, "  " + VERMELHO_FORTE + "[0]" + RESET + " 💾 Salvar e Sair do Jogo", 88);
                System.out.println(ROXO + "╚════════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
                int opcao = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Escolha uma ação: " + RESET, 0, 4);

                if (opcao == 1) {
                    System.out.println("\n" + AMARELO + "╔═════════════════════════════ ESCOLHA O ALVO DA INVASÃO ═════════════════════════════╗" + RESET);
                    for (int i = 0; i < territorios.size(); i++) {
                        Territorio t = territorios.get(i);
                        boolean meu = t.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue();
                        String statusDono = meu
                                ? VERDE_FORTE + String.format("%-26s", "✔ SEU TERRITÓRIO") + RESET
                                : VERMELHO_FORTE + String.format("%-26s", t.getGangueDominante().getNome()) + RESET;
                        String linhaAlvo = String.format("  %s[%d]%s %s%-31s%s [%s] | Loot: %s$%6.2f%s",
                                CIANO, (i + 1), RESET,
                                NEGRITO, t.getNome(), RESET,
                                statusDono,
                                VERDE_FORTE, t.getRecompensaDinheiro(), RESET);
                        linhaCaixa(AMARELO, linhaAlvo, 85);
                    }
                    linhaCaixa(AMARELO, "  " + VERMELHO_FORTE + "[0]" + RESET + " Voltar", 88);
                    System.out.println(AMARELO + "╚═════════════════════════════════════════════════════════════════════════════════════╝" + RESET);

                    int escolhaTerritorio = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Alvo: " + RESET, 0, territorios.size());
                    if (escolhaTerritorio == 0) continue;

                    Territorio alvo = territorios.get(escolhaTerritorio - 1);

                    if (alvo.getGangueDominante().getIdGangue() == jogador.getGangue().getIdGangue()) {
                        System.out.println(VERDE + "  ✔ Este território já pertence à sua gangue!" + RESET);
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
                        animarCarregamento("Mobilizando soldados para " + alvo.getNome());

                        int vidaCapanga = 45 + (alvo.getIdTerritorio() * 10);
                        int danoCapanga = 8 + (alvo.getIdTerritorio() * 2);
                        Inimigo capanga = new Inimigo(900 + alvo.getIdTerritorio(), "Capanga de " + alvo.getGangueDominante().getNome(), vidaCapanga, danoCapanga, alvo.getIdTerritorio());

                        digitarLento("\n" + AMARELO + NEGRITO + "  ⚠️  ONDA 1/2: Um capanga bloqueia a entrada de " + alvo.getNome() + "!" + RESET, 12);
                        boolean venceuCapanga = lutarContraInimigo(scanner, jogador, capanga, alvo, false);

                        if (venceuCapanga && jogador.estaVivo()) {
                            digitarLento("\n" + VERMELHO_FORTE + NEGRITO + "  🔥 ONDA 2/2: O chefe do território apareceu para defender o ponto!" + RESET, 15);
                            boolean venceuChefe = lutarContraInimigo(scanner, jogador, chefe, alvo, true);

                            if (venceuChefe && jogador.estaVivo()) {
                                verificarContraAtaqueRival(scanner, jogador, territorios, gangues);
                            }
                        }
                    } else {
                        System.out.println(VERDE + "  🚩 Território sem chefe vivo! Você assumiu o controle." + RESET);
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
                        animarCarregamento("Realizando tratamento clandestino");
                        System.out.println(VERDE_FORTE + "  ✔ Vida restaurada ao máximo e salva no banco!" + RESET);
                    } else {
                        System.out.println(VERMELHO_FORTE + "  ✖ Dinheiro insuficiente! Você precisa de $200." + RESET);
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
                System.out.println("\n" + VERMELHO_FORTE + NEGRITO + "╔════════════════════════════════════════════════════════════════╗" + RESET);
                linhaCaixa(VERMELHO_FORTE + NEGRITO, "         💀 VOCÊ MORREU! Fim de jogo em Albuquerque.", 64);
                System.out.println(VERMELHO_FORTE + NEGRITO + "╚════════════════════════════════════════════════════════════════╝" + RESET);
            } else {
                System.out.println("\n" + VERDE_FORTE + "  💾 Jogo encerrado. Progresso salvo no MySQL!" + RESET);
            }

        } catch (SQLException e) {
            System.out.println(VERMELHO_FORTE + "Erro ao conectar ou salvar no banco de dados MySQL: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void imprimirRadarTerritorios(List<Territorio> territorios, Gangue minhaGangue) {
        System.out.println("\n" + CIANO + "╔══════════════════════════════ RADAR DE ALBUQUERQUE ══════════════════════════════╗" + RESET);
        for (Territorio t : territorios) {
            boolean dominado = t.getGangueDominante() != null
                    && t.getGangueDominante().getIdGangue() == minhaGangue.getIdGangue();
            String badge = dominado
                    ? VERDE_FORTE + "[✔ DOMINADO] " + RESET
                    : VERMELHO_FORTE + "[☠ RIVAL]   " + RESET;
            String dono = t.getGangueDominante() != null ? t.getGangueDominante().getNome() : "Neutro";
            String linha = String.format("  %s %s%-32s%s | %s", badge, NEGRITO, t.getNome(), RESET, dono);
            linhaCaixa(CIANO, linha, 82);
        }
        System.out.println(CIANO + "╚══════════════════════════════════════════════════════════════════════════════════╝" + RESET);
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
        System.out.println("\n" + DOURADO + "╔═══════════════════════════ MERCADO NEGRO (SAUL GOODMAN) ═══════════════════════════╗" + RESET);
        linhaCaixa(DOURADO, String.format("  Seu saldo atual: %s$%.2f%s", VERDE_FORTE, jogador.getDinheiro(), RESET), 84);
        System.out.println(DOURADO + "╠════════════════════════════════════════════════════════════════════════════════════╣" + RESET);
        linhaCaixa(DOURADO, "  " + CIANO + "[1]" + RESET + " 🦺 Colete Balístico (+25 Vida Máxima imediata)              - " + VERDE_FORTE + "$300.00" + RESET, 84);
        linhaCaixa(DOURADO, "  " + CIANO + "[2]" + RESET + " 🍗 Balde Los Pollos (Cura +40 na mochila)                    - " + VERDE_FORTE + "$150.00" + RESET, 84);
        linhaCaixa(DOURADO, "  " + CIANO + "[3]" + RESET + " 💎 Cristal Azul (Buff 2X Dano por 2 turnos na mochila)        - " + VERDE_FORTE + "$400.00" + RESET, 84);
        linhaCaixa(DOURADO, "  " + VERMELHO_FORTE + "[0]" + RESET + " Voltar", 84);
        System.out.println(DOURADO + "╚════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
        int escolha = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Comprar item: " + RESET, 0, 3);
        if (escolha == 0) return;

        if (escolha == 1) {
            if (jogador.getDinheiro() >= 300) {
                jogador.setDinheiro(jogador.getDinheiro() - 300);
                jogador.setVidaMaxima(jogador.getVidaMaxima() + 25);
                jogador.setVidaAtual(jogador.getVidaAtual() + 25);
                System.out.println(VERDE_FORTE + "  ✔ Colete equipado! Vida máxima subiu para " + jogador.getVidaMaxima() + "." + RESET);
            } else {
                System.out.println(VERMELHO_FORTE + "  ✖ Dinheiro insuficiente!" + RESET);
            }
        } else if (escolha == 2) {
            if (jogador.getDinheiro() >= 150) {
                jogador.setDinheiro(jogador.getDinheiro() - 150);
                jogador.adicionarItem(new Item(1, "Balde do Los Pollos Hermanos", "CURA", 40, 0));
                dao.adicionarItemInventario(idJogadorBanco, 1);
                System.out.println(VERDE_FORTE + "  ✔ Balde do Los Pollos adicionado à mochila!" + RESET);
            } else {
                System.out.println(VERMELHO_FORTE + "  ✖ Dinheiro insuficiente!" + RESET);
            }
        } else if (escolha == 3) {
            if (jogador.getDinheiro() >= 400) {
                jogador.setDinheiro(jogador.getDinheiro() - 400);
                jogador.adicionarItem(new Item(3, "Cristal Azul (Metanfetamina)", "BUFF_DANO", 2, 2));
                dao.adicionarItemInventario(idJogadorBanco, 3);
                System.out.println(VERDE_FORTE + "  ✔ Cristal Azul adicionado à mochila!" + RESET);
            } else {
                System.out.println(VERMELHO_FORTE + "  ✖ Dinheiro insuficiente!" + RESET);
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

            System.out.println("\n" + VERMELHO_FORTE + NEGRITO + "╔══════════════════════════════ ALERTA DE CONTRA-ATAQUE ══════════════════════════════╗" + RESET);
            linhaCaixa(VERMELHO_FORTE + NEGRITO, "  🚨 A gangue " + invasora.getNome() + " está atacando: " + atacado.getNome() + "!", 85);
            System.out.println(VERMELHO_FORTE + NEGRITO + "╚═════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
            System.out.println("  " + CIANO + "[1]" + RESET + " 🛡️ Defender o território agora");
            System.out.println("  " + AMARELO + "[2]" + RESET + " 🏳️ Abandonar o território para eles");
            int escolha = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Decisão: " + RESET, 1, 2);

            if (escolha == 1) {
                Inimigo liderAtaque = new Inimigo(999, "Tenente de " + invasora.getNome(), 85, 15, atacado.getIdTerritorio());
                boolean defendeu = lutarContraInimigo(scanner, jogador, liderAtaque, atacado, false);
                if (defendeu && jogador.estaVivo()) {
                    int respeitoDefesa = invasora.getBonusAtaque() >= 14 ? 8 : 4;
                    System.out.println(VERDE_FORTE + "  ✔ Você repeliu o contra-ataque de " + invasora.getNome() + " e manteve " + atacado.getNome() + " (+" + respeitoDefesa + " Respeito)!" + RESET);
                    jogador.setNivelRespeito(jogador.getNivelRespeito() + respeitoDefesa);
                    dao.atualizarJogador(idJogadorBanco, jogador);
                } else if (jogador.estaVivo()) {
                    atacado.setGangueDominante(invasora);
                    dao.atualizarDonoTerritorio(atacado.getIdTerritorio(), invasora.getIdGangue());
                    System.out.println(VERMELHO_FORTE + "  ✖ Você recuou e perdeu o controle de " + atacado.getNome() + "!" + RESET);
                }
            } else {
                atacado.setGangueDominante(invasora);
                dao.atualizarDonoTerritorio(atacado.getIdTerritorio(), invasora.getIdGangue());
                System.out.println(AMARELO + "  🏳️ Você abandonou " + atacado.getNome() + " para " + invasora.getNome() + "." + RESET);
            }
        }
    }

    private static void verificarEventoAleatorio(Jogador jogador) {
        int sorteio = random.nextInt(100) + 1;

        if (sorteio <= 25) {
            double suborno = Math.min(jogador.getDinheiro(), 100);
            if (suborno > 0) {
                jogador.setDinheiro(jogador.getDinheiro() - suborno);
                digitarLento("\n" + VERMELHO_FORTE + "  🚨 BATIDA DA DEA! Você pagou $" + suborno + " de suborno para escapar da emboscada." + RESET, 12);
            } else {
                int dano = 15;
                jogador.setVidaAtual(Math.max(1, jogador.getVidaAtual() - dano));
                digitarLento("\n" + VERMELHO_FORTE + "  🚨 BATIDA DA DEA! Sem dinheiro para suborno, você fugiu trocando tiros (-" + dano + " de vida)." + RESET, 12);
            }
        } else if (sorteio <= 50) {
            double bonus = 150;
            jogador.setDinheiro(jogador.getDinheiro() + bonus);
            digitarLento("\n" + VERDE_FORTE + "  💰 CARREGAMENTO EXTRA! Um informante deixou $" + bonus + " escondidos no território!" + RESET, 12);
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
        System.out.println("\n" + DOURADO + NEGRITO + "╔══════════════════════════════════════════════════════════════════════════════╗" + RESET);
        linhaCaixa(DOURADO + NEGRITO, "                 IMPÉRIO CONQUISTADO • FIM DE JOGO (VITÓRIA)", 78);
        System.out.println(DOURADO + NEGRITO + "╠══════════════════════════════════════════════════════════════════════════════╣" + RESET);
        linhaCaixa(DOURADO + NEGRITO, "  Você derrotou todas as gangues rivais e agora é o DONO DE ALBUQUERQUE!", 78);
        linhaCaixa(DOURADO + NEGRITO, "  Não sobrou ninguém para desafiar o trono do cartel.", 78);
        System.out.println(DOURADO + NEGRITO + "╠══════════════════════════════════════════════════════════════════════════════╣" + RESET);
        linhaCaixa(DOURADO + NEGRITO, "  Chefe Supremo:    " + jogador.getNome(), 78);
        linhaCaixa(DOURADO + NEGRITO, "  Facção Vencedora: " + jogador.getGangue().getNome(), 78);
        linhaCaixa(DOURADO + NEGRITO, String.format("  Fortuna Final:    $%.2f", jogador.getDinheiro()), 78);
        linhaCaixa(DOURADO + NEGRITO, "  Respeito Total:   " + jogador.getNivelRespeito(), 78);
        System.out.println(DOURADO + NEGRITO + "╚══════════════════════════════════════════════════════════════════════════════╝" + RESET);
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
            System.out.println(VERMELHO_FORTE + "  ✖ Opção inválida! Digite um número entre " + min + " e " + max + "." + RESET);
        }
    }

    private static boolean lutarContraInimigo(Scanner scanner, Jogador jogador, Inimigo inimigo, Territorio territorio, boolean conquistarAoVencer) throws SQLException {
        int vidaMaxInimigo = Math.max(50, inimigo.getVidaAtual());

        System.out.println("\n" + VERMELHO_FORTE + NEGRITO + "╔══════════════════════════════════════════════════════════════════════╗" + RESET);
        linhaCaixa(VERMELHO_FORTE + NEGRITO, "  ⚔️  CONFRONTO EM: " + territorio.getNome().toUpperCase(), 70);
        linhaCaixa(VERMELHO_FORTE + NEGRITO, "  💀 ALVO: " + inimigo.getNome() + "  |  ❤️  VIDA: " + inimigo.getVidaAtual(), 70);
        System.out.println(VERMELHO_FORTE + NEGRITO + "╚══════════════════════════════════════════════════════════════════════╝" + RESET);

        while (jogador.estaVivo() && inimigo.estaVivo()) {
            System.out.println("\n" + CINZA + "────────────────────────────────────────────────────────────────────────" + RESET);
            System.out.printf("  %-22s %s (%3d/%3d)%n",
                    NEGRITO + jogador.getNome() + RESET,
                    gerarBarraVida(jogador.getVidaAtual(), jogador.getVidaMaxima()),
                    jogador.getVidaAtual(), jogador.getVidaMaxima());
            System.out.printf("  %-22s %s (%3d/%3d)%n",
                    VERMELHO_FORTE + inimigo.getNome() + RESET,
                    gerarBarraVida(inimigo.getVidaAtual(), vidaMaxInimigo),
                    Math.max(0, inimigo.getVidaAtual()), vidaMaxInimigo);
            System.out.println(CINZA + "────────────────────────────────────────────────────────────────────────" + RESET);
            System.out.println("  " + CIANO + "[1]" + RESET + " 🔫 Atacar   |   "
                    + ROXO + "[2]" + RESET + " 🎯 Tiro Certeiro (55% Crítico)   |   "
                    + CIANO + "[3]" + RESET + " 🎒 Mochila   |   "
                    + AMARELO + "[4]" + RESET + " 🏃 Fugir");

            int acao = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Ação: " + RESET, 1, 4);

            if (acao == 1) {
                int bonusPatente = jogador.getNivelRespeito() / 10 * 2;
                int danoJogador = jogador.atacar() + bonusPatente;
                inimigo.setVidaAtual(inimigo.getVidaAtual() - danoJogador);
                System.out.println(VERDE_FORTE + "  💥 Você atacou " + inimigo.getNome() + " e causou " + danoJogador + " de dano!" + RESET);

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(VERMELHO_FORTE + "  🩸 " + inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!" + RESET);
                }
            } else if (acao == 2) {
                if (random.nextInt(100) < 55) {
                    int bonusPatente = jogador.getNivelRespeito() / 10 * 2;
                    int danoCritico = (jogador.atacar() + bonusPatente) * 2;
                    inimigo.setVidaAtual(inimigo.getVidaAtual() - danoCritico);
                    System.out.println(DOURADO + NEGRITO + "  🎯 TIRO CERTEIRO! Dano crítico de " + danoCritico + " em " + inimigo.getNome() + "!" + RESET);
                } else {
                    System.out.println(AMARELO + "  💨 Você errou o tiro certeiro e perdeu a abertura!" + RESET);
                }

                if (inimigo.estaVivo()) {
                    int danoInimigo = inimigo.atacar();
                    jogador.setVidaAtual(jogador.getVidaAtual() - danoInimigo);
                    System.out.println(VERMELHO_FORTE + "  🩸 " + inimigo.getNome() + " revidou e causou " + danoInimigo + " de dano!" + RESET);
                }
            } else if (acao == 3) {
                abrirMochila(scanner, jogador);
            } else {
                System.out.println(AMARELO + "  🏃 Você recuou do confronto!" + RESET);
                dao.atualizarJogador(idJogadorBanco, jogador);
                return false;
            }
        }

        if (jogador.estaVivo() && !inimigo.estaVivo()) {
            if (conquistarAoVencer) {
                int respeitoGanho = calcularRespeitoGanho(territorio);
                String gangueDerrotada = (territorio.getGangueDominante() != null) ? territorio.getGangueDominante().getNome() : "Rival";

                System.out.println("\n" + VERDE_FORTE + NEGRITO + "  🏆 VITÓRIA! Você derrotou " + inimigo.getNome() + " (" + gangueDerrotada + ") e dominou " + territorio.getNome() + "!" + RESET);
                System.out.println(CIANO + "  ⭐ Respeito ganho: +" + respeitoGanho + " (Total: " + (jogador.getNivelRespeito() + respeitoGanho) + ")" + RESET);

                territorio.setGangueDominante(jogador.getGangue());
                jogador.setDinheiro(jogador.getDinheiro() + territorio.getRecompensaDinheiro());
                jogador.setNivelRespeito(jogador.getNivelRespeito() + respeitoGanho);

                sortearLootTerritorio(jogador, territorio);
                verificarEventoAleatorio(jogador);

                dao.atualizarDonoTerritorio(territorio.getIdTerritorio(), jogador.getGangue().getIdGangue());
                dao.atualizarJogador(idJogadorBanco, jogador);
            } else {
                System.out.println(VERDE_FORTE + "  ✔ Você eliminou " + inimigo.getNome() + "!" + RESET);
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
                System.out.println(AMARELO + "  Nenhum item encontrado na cozinha do Los Pollos desta vez." + RESET);
            }
        } else if (territorio.getIdTerritorio() == 3) {
            if (chance <= 10) {
                jogador.adicionarItem(new Item(3, "Cristal Azul (Metanfetamina)", "BUFF_DANO", 2, 2));
                dao.adicionarItemInventario(idJogadorBanco, 3);
            } else if (chance <= 30) {
                jogador.adicionarItem(new Item(2, "Chá de Camomila da Lydia", "CURA", 25, 0));
                dao.adicionarItemInventario(idJogadorBanco, 2);
            } else {
                System.out.println(AMARELO + "  Nenhum item encontrado no laboratório subterrâneo desta vez." + RESET);
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
            System.out.println(AMARELO + "  [!] Sua mochila está vazia!" + RESET);
            return;
        }
        System.out.println("\n" + CIANO + "╔════════════════════════════ MOCHILA ════════════════════════════╗" + RESET);
        for (int i = 0; i < mochila.size(); i++) {
            linhaCaixa(CIANO, "  " + DOURADO + "[" + (i + 1) + "]" + RESET + " " + NEGRITO + mochila.get(i).getNome() + RESET, 65);
        }
        linhaCaixa(CIANO, "  " + VERMELHO_FORTE + "[0]" + RESET + " Voltar", 65);
        System.out.println(CIANO + "╚═════════════════════════════════════════════════════════════════╝" + RESET);
        int escolha = lerOpcao(scanner, DOURADO + NEGRITO + "  ➤ Escolha o item para usar: " + RESET, 0, mochila.size());
        if (escolha > 0) {
            jogador.usarItem(escolha - 1);
        }
    }
    private static void linhaCaixa(String corBorda, String conteudo, int larguraInterna) {
        String limpo = conteudo.replaceAll("\u001B\\[[;\\d]*m", "").replace("\uFE0F", "");
        double larguraVisual = 0.0;
        for (int i = 0; i < limpo.length(); ) {
            int cp = limpo.codePointAt(i);
            if (cp >= 0x1F300) {
                larguraVisual += 1.5;
            } else {
                larguraVisual += 1.0;
            }
            i += Character.charCount(cp);
        }
        int espacos = Math.max(0, (int) Math.floor(larguraInterna - larguraVisual));
        StringBuilder preenchimento = new StringBuilder();
        for (int i = 0; i < espacos; i++) {
            preenchimento.append(" ");
        }
        System.out.println(corBorda + "║" + RESET + conteudo + preenchimento.toString() + corBorda + "║" + RESET);
    }

    private static void digitarLento(String texto, int atrasoMs) {
        for (char c : texto.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(atrasoMs);
            } catch (InterruptedException ignored) {}
        }
        System.out.println();
    }

    private static void animarCarregamento(String acao) {
        System.out.print("\n" + DOURADO + "  ⏳ " + acao + " " + RESET + VERDE_FORTE);
        for (int i = 0; i < 16; i++) {
            System.out.print("█");
            try {
                Thread.sleep(30);
            } catch (InterruptedException ignored) {}
        }
        System.out.println(" ✔" + RESET);
    }

    private static void imprimirBanner() {
        String[] linhas = {
                "╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗",
                "║   ██████╗ ███████╗    ███████╗ █████╗ ██╗      █████╗ ███╗   ███╗ █████╗ ███╗   ██╗ ██████╗ █████╗ ███████╗  ║",
                "║  ██╔═══██╗██╔════╝    ██╔════╝██╔══██╗██║     ██╔══██╗████╗ ████║██╔══██╗████╗  ██║██╔════╝██╔══██╗██╔════╝  ║",
                "║  ██║   ██║███████╗    ███████╗███████║██║     ███████║██╔████╔██║███████║██╔██╗ ██║██║     ███████║███████╗  ║",
                "║  ██║   ██║╚════██║    ╚════██║██╔══██║██║     ██╔══██║██║╚██╔╝██║██╔══██║██║╚██╗██║██║     ██╔══██║╚════██║  ║",
                "║  ╚██████╔╝███████║    ███████║██║  ██║███████╗██║  ██║██║ ╚═╝ ██║██║  ██║██║ ╚████║╚██████╗██║  ██║███████║  ║",
                "║   ╚═════╝ ╚══════╝    ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝╚══════╝  ║",
                "║                                      GUERRA DE TERRITÓRIOS: ALBUQUERQUE                                      ║",
                "╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════╝"
        };
        String[] degrade = {
                "\u001B[38;2;40;75;175m",    // Borda superior (Azul)
                "\u001B[38;2;45;70;170m",    // Linha 1 letras (Azul)
                "\u001B[38;2;52;62;162m",    // Linha 2 letras (Azul levemente arroxeado)
                "\u001B[38;2;60;52;152m",    // Linha 3 letras (Transição suave)
                "\u001B[38;2;68;42;142m",    // Linha 4 letras (Roxo escuro suave)
                "\u001B[38;2;72;32;130m",    // Linha 5 letras (Roxo escuro)
                "\u001B[38;2;75;25;118m",    // Linha 6 letras (Roxo escuro)
                "\u001B[38;2;115;85;185m",   // Subtítulo (Tom médio para leitura)
                "\u001B[38;2;75;25;118m"     // Borda inferior (Roxo escuro)
        };

        System.out.println(NEGRITO);
        for (int i = 0; i < linhas.length; i++) {
            System.out.println(degrade[i] + linhas[i] + RESET);
        }
    }

    private static String gerarBarraVida(int atual, int maxima) {
        int blocos = 12;
        int preenchidos = (int) Math.round(((double) Math.max(0, atual) / maxima) * blocos);
        String cor = atual > maxima * 0.5 ? VERDE_FORTE : (atual > maxima * 0.25 ? AMARELO : VERMELHO_FORTE);
        StringBuilder sb = new StringBuilder(cor + "[");
        for (int i = 0; i < blocos; i++) {
            sb.append(i < preenchidos ? "█" : "░");
        }
        sb.append("]" + RESET);
        return sb.toString();
    }
}