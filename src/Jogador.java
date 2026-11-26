import java.util.ArrayList;
import java.util.List;

public class Jogador extends Personagem {
    private double dinheiro;
    private int nivelRespeito;
    private Gangue gangue;
    private List<Item> inventario;
    private int turnosDobroDano;

    public Jogador(int id, String nome, int vidaAtual, int vidaMaxima, double dinheiro, int nivelRespeito, Gangue gangue) {
        super(id, nome, vidaAtual, vidaMaxima);
        this.dinheiro = dinheiro;
        this.nivelRespeito = nivelRespeito;
        this.gangue = gangue;
        this.inventario = new ArrayList<>();
        this.turnosDobroDano = 0;
    }

    @Override
    public int atacar() {
        int danoBase = 15 + (gangue != null ? gangue.getBonusAtaque() : 0);
        if (turnosDobroDano > 0) {
            turnosDobroDano--;
            System.out.println("🔥 [EFEITO CRISTAL AZUL] Dano dobrado! (Restam " + turnosDobroDano + " ataques com buff)");
            return danoBase * 2;
        }
        return danoBase;
    }

    public void adicionarItem(Item item) {
        inventario.add(item);
        System.out.println("🎒 ITEM COLETADO: " + item.getNome() + " foi adicionado à sua mochila!");
    }

    public void usarItem(int indice) {
        if (indice < 0 || indice >= inventario.size()) {
            System.out.println("Item inválido!");
            return;
        }
        Item item = inventario.remove(indice);
        if (item.getTipo().equals("CURA")) {
            setVidaAtual(getVidaAtual() + item.getValorEfeito());
            System.out.println("🍗 Você usou " + item.getNome() + " e recuperou " + item.getValorEfeito() + " de vida!");
        } else if (item.getTipo().equals("BUFF_DANO")) {
            this.turnosDobroDano = item.getDuracaoAtaques();
            System.out.println("💎 Você usou " + item.getNome() + "! Seus próximos " + item.getDuracaoAtaques() + " ataques causarão o DOBRO de dano!");
        }
    }

    public List<Item> getInventario() { return inventario; }
    public double getDinheiro() { return dinheiro; }
    public void setDinheiro(double dinheiro) { this.dinheiro = dinheiro; }
    public int getNivelRespeito() { return nivelRespeito; }
    public void setNivelRespeito(int nivelRespeito) { this.nivelRespeito = nivelRespeito; }
    public Gangue getGangue() { return gangue; }
}