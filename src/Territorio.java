public class Territorio {
    private int idTerritorio;
    private String nome;
    private int nivelDificuldade;
    private double recompensaDinheiro;
    private Gangue gangueDominante;

    public Territorio(int idTerritorio, String nome, int nivelDificuldade, double recompensaDinheiro, Gangue gangueDominante) {
        this.idTerritorio = idTerritorio;
        this.nome = nome;
        this.nivelDificuldade = nivelDificuldade;
        this.recompensaDinheiro = recompensaDinheiro;
        this.gangueDominante = gangueDominante;
    }

    public int getIdTerritorio() { return idTerritorio; }
    public String getNome() { return nome; }
    public int getNivelDificuldade() { return nivelDificuldade; }
    public double getRecompensaDinheiro() { return recompensaDinheiro; }
    public Gangue getGangueDominante() { return gangueDominante; }
    public void setGangueDominante(Gangue gangueDominante) { this.gangueDominante = gangueDominante; }
}