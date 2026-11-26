public class Item {
    private int idItem;
    private String nome;
    private String tipo; // "CURA" ou "BUFF_DANO"
    private int valorEfeito;
    private int duracaoAtaques;

    public Item(int idItem, String nome, String tipo, int valorEfeito, int duracaoAtaques) {
        this.idItem = idItem;
        this.nome = nome;
        this.tipo = tipo;
        this.valorEfeito = valorEfeito;
        this.duracaoAtaques = duracaoAtaques;
    }

    public int getIdItem() { return idItem; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getValorEfeito() { return valorEfeito; }
    public int getDuracaoAtaques() { return duracaoAtaques; }
}