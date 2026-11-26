import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JogadorDAO {

    // 1. Buscar todas as gangues do banco
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
        }
        return lista;
    }

    // 2. Buscar todos os territórios (já ligando com a gangue dominante)
    public List<Territorio> listarTerritorios(List<Gangue> gangues) throws SQLException {
        List<Territorio> lista = new ArrayList<>();
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
        String sql = "INSERT INTO inventario_jogador (jogador_id, item_id) VALUES (?, ?)";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJogador);
            stmt.setInt(2, idItem);
            stmt.executeUpdate();
        }
    }
}