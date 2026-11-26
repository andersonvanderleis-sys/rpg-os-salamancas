import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    public static Connection conectar() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/rpg_salamancas",
                    "root",
                    "12345678"
            );
        } catch (Exception e) {
            System.out.println("Erro ao ligar: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        if (conectar() != null) {
            System.out.println("LIGADO COM SUCESSO AO MYSQL!");
        }
    }
}