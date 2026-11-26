public class Gangue {
    private int idGangue;
    private String nome;
    private int bonusAtaque;
    private double dinheiroInicial;

    public Gangue(int idGangue, String nome, int bonusAtaque, double dinheiroInicial) {
        this.idGangue = idGangue;
        this.nome = nome;
        this.bonusAtaque = bonusAtaque;
        this.dinheiroInicial = dinheiroInicial;
    }

    public int getIdGangue() { return idGangue; }
    public String getNome() { return nome; }
    public int getBonusAtaque() { return bonusAtaque; }
    public double getDinheiroInicial() { return dinheiroInicial; }
}