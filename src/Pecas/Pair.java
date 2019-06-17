package Pecas;

public class Pair {
	public final Peca peca;
	public final int qtde;
	
	public Pair(Peca p, int q) {
		this.peca = p;
		this.qtde = q;
	}
	public String toString()  {
		return "(" + this.peca + "," + this.qtde + ")";
	}
}