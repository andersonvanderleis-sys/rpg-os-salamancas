public class Inimigo extends Personagem {
    private int dano;
    private int idTerritorio;

    public Inimigo(int id, String nome, int vida, int dano, int idTerritorio) {
        super(id, nome, vida, vida);
        this.dano = dano;
        this.idTerritorio = idTerritorio;
    }

    @Override
    public int atacar() {
        return this.dano;
    }

    public int getDano() { return dano; }
    public int getIdTerritorio() { return idTerritorio; }
}