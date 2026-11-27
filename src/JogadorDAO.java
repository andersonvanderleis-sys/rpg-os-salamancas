import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JogadorDAO {

    private boolean modoOffline = false;

    // 1. Buscar todas as gangues (tenta MySQL; se falhar, ativa modo offline)
    public List<Gangue> listarGangues() throws SQLException {
        List<Gangue> lista = new ArrayList<>();
        String sql = "SELECT * FROM gangue";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Gangue(
                        rs.getInt("id_gangue"),
                        rs.getString("nome"),
                        rs.getInt("bonus_ataque"),
                        rs.getDouble("dinheiro_inicial")
                ));
            }
            modoOffline = false;
        } catch (SQLException e) {
            modoOffline = true;
            System.out.println("⚠️ [AVISO] MySQL desligado neste PC. Iniciando em MODO OFFLINE para testes!");
            lista.add(new Gangue(1, "Cartel Salamanca", 10, 500.0));
            lista.add(new Gangue(2, "Los Pollos Hermanos", 5, 1000.0));
            lista.add(new Gangue(3, "Gangue de Albuquerque", 7, 700.0));
        }
        return lista;
    }

    // 2. Buscar todos os territórios (já ligando com a gangue dominante)
    public List<Territorio> listarTerritorios(List<Gangue> gangues) throws SQLException {
        List<Territorio> lista = new ArrayList<>();
        if (modoOffline) {
            Gangue g1 = gangues.get(0);
            Gangue g2 = gangues.get(1);
            Gangue g3 = gangues.get(2);
            lista.add(new Territorio(1, "Deserto de Tohajiilee", 1, 300.0, g1));
            lista.add(new Territorio(2, "Restaurante Los Pollos Hermanos", 2, 600.0, g2));
            lista.add(new Territorio(3, "Superlaboratório Subterrâneo", 3, 1200.0, g3));
            return lista;
        }

        String sql = "SELECT * FROM territorio";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int idGangueDom = rs.getInt("gangue_dominante_id");
                Gangue dominante = gangues.stream()
                        .filter(g -> g.getIdGangue() == idGangueDom)
                        .findFirst()
                        .orElse(null);

                lista.add(new Territorio(
                        rs.getInt("id_territorio"),
                        rs.getString("nome"),
                        rs.getInt("nivel_dificuldade"),
                        rs.getDouble("recompensa_dinheiro"),
                        dominante
                ));
            }
        }
        return lista;
    }

    // 3. Buscar todos os inimigos do banco
    public List<Inimigo> listarInimigos() throws SQLException {
        List<Inimigo> lista = new ArrayList<>();
        if (modoOffline) {
            lista.add(new Inimigo(1, "Tuco Salamanca", 70, 12, 1));
            lista.add(new Inimigo(2, "Mike Ehrmantraut", 100, 18, 2));
            lista.add(new Inimigo(3, "Gus Fring", 130, 22, 3));
            return lista;
        }

        String sql = "SELECT * FROM inimigo";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Inimigo(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("vida_atual"),
                        rs.getInt("dano"),
                        rs.getInt("id_territorio")
                ));
            }
        }
        return lista;
    }

    // 4. Salvar um novo jogador no banco e retornar o ID gerado
    public int salvarNovoJogador(Jogador j) throws SQLException {
        if (modoOffline) {
            return 999; // ID fictício para jogar offline
        }
        String sql = "INSERT INTO jogador (nome, vida_atual, vida_maxima, dinheiro, nivel_respeito, turnos_dobro_dano, gangue_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, j.getNome());
            stmt.setInt(2, j.getVidaAtual());
            stmt.setInt(3, j.getVidaMaxima());
            stmt.setDouble(4, j.getDinheiro());
            stmt.setInt(5, j.getNivelRespeito());
            stmt.setInt(6, 0);
            stmt.setInt(7, j.getGangue().getIdGangue());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // 5. Atualizar progresso do jogador (vida, dinheiro, respeito)
    public void atualizarJogador(int idJogador, Jogador j) throws SQLException {
        if (modoOffline) return;
        String sql = "UPDATE jogador SET vida_atual = ?, dinheiro = ?, nivel_respeito = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, j.getVidaAtual());
            stmt.setDouble(2, j.getDinheiro());
            stmt.setInt(3, j.getNivelRespeito());
            stmt.setInt(4, idJogador);
            stmt.executeUpdate();
        }
    }

    // 6. Atualizar o dono de um território quando for conquistado
    public void atualizarDonoTerritorio(int idTerritorio, int novaGangueId) throws SQLException {
        if (modoOffline) return;
        String sql = "UPDATE territorio SET gangue_dominante_id = ? WHERE id_territorio = ?";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novaGangueId);
            stmt.setInt(2, idTerritorio);
            stmt.executeUpdate();
        }
    }

    // 7. Salvar item coletado na mochila do jogador no banco
    public void adicionarItemInventario(int idJogador, int idItem) throws SQLException {
        if (modoOffline) return;
        String sql = "INSERT INTO inventario_jogador (jogador_id, item_id) VALUES (?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJogador);
            stmt.setInt(2, idItem);
            stmt.executeUpdate();
        }
    }
}