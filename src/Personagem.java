public abstract class Personagem {
    private int id;
    private String nome;
    private int vidaAtual;
    int vidaMaxima;

    public Personagem(int id, String nome, int vidaAtual, int vidaMaxima) {
        this.id = id;
        this.nome = nome;
        this.vidaMaxima = vidaMaxima;
        setVidaAtual(vidaAtual);
    }

    public void setVidaAtual(int vidaAtual) {
        if (vidaAtual < 0) {
            this.vidaAtual = 0;
        } else if (vidaAtual > this.vidaMaxima) {
            this.vidaAtual = this.vidaMaxima;
        } else {
            this.vidaAtual = vidaAtual;
        }
    }

    public boolean estaVivo() {
        return this.vidaAtual > 0;
    }

    public abstract int atacar();

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getVidaAtual() { return vidaAtual; }
    public int getVidaMaxima() { return vidaMaxima; }
}