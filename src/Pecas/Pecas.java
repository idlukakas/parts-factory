package Pecas;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class Calcula extends RecursiveTask<Integer> {
	private final Peca peca;
	int qtde;
	
	public Calcula(Peca peca, int qtde) {
		this.peca = peca;
		this.qtde = qtde;
	}
	
	public Calcula(Peca peca) {
		this.peca = peca;
		this.qtde = 1;
	}
	
	
	public Integer compute() {
		if (peca instanceof PecaSimples) 
			return peca.valor * qtde;
		PecaCompost pecacomp = (PecaCompost) peca;
		List<Calcula> tasks = new ArrayList<>();
		for(Pair p: pecacomp.composicao) {
			Calcula c = new Calcula(p.peca, p.qtde);
			tasks.add(c);
			c.fork();
		}
		int valor = 0;
		for(Calcula c: tasks) {
			int v = c.join();
			valor += v*qtde;
		}
		return valor;
	}
}

class Peca {
	String nome;
	int valor;
	
	public Peca (String n, int v) {
		this.nome = n;
		this.valor = v;
	}
	
	public boolean colocar (Object o, Peca comp, int qtde) {
		if(!(o instanceof PecaCompost)) return false;
		PecaCompost p = (PecaCompost) o;
		p.colocar(comp, qtde);
		return true;
	}
	/*
	public static boolean calcula (Peca o) {
		if(!(o instanceof PecaCompost)) return false;
		PecaCompost p = (PecaCompost) o;
		p.setValor(p.calcula());
		return true;
	}*/
	
	public static HashMap<String, Peca> buildPecas(String fileName) {
		HashMap<String, Peca> lista = new HashMap<>();
		try(Scanner s = new Scanner(new FileReader(fileName))){
			int m = s.nextInt();
			for(; m > 0; --m) {
				String a = s.next();
				lista.put(a, new PecaSimples(a, s.nextInt()));
			}
			while(s.hasNext()) {
				String a = s.next();
				if(lista.containsKey(a)){
					lista.get(a).colocar(lista.get(a), lista.get(s.next()), s.nextInt());
					continue;
				}
				lista.put(a, new PecaCompost(a, 0, lista.get(s.next()), s.nextInt()));
			}
		}catch(IOException e) {
		}
		return lista;
	}
	
	/*public static void calculaCompostos(HashMap<String, Peca> lista) {
		lista.forEach((String, PecaCompost) -> calcula(lista.get(String)));
	}*/
	
	public static void listaSimples(HashMap<String, Peca> lista) {
		lista.forEach((String, PecaCompost) -> System.out.println("\n" + listarSim(lista.get(String)) + "\n"));
	}
	
	protected static String listarSim(Peca o) {
		if(!(o instanceof PecaCompost)) return "";
		PecaCompost p = (PecaCompost) o;
		return p.listaSim();
	}

	public int getValor() {
		return this.valor;
	}
}

class PecaSimples extends Peca {
	public PecaSimples (String n, int v) {
		super(n, v);
	}
	
	public String toString()  {
		return "{" + this.nome + "," + this.valor + "}";
	}
	
}

class PecaCompost extends Peca {
	ArrayList<Pair> composicao = new ArrayList<>();
	
	public PecaCompost(String n, int v, Peca comp, int qtde)  {
		super (n, v);
		composicao.add(new Pair(comp, qtde));
	}
	
	public String listaSim() {
		String s = "";
		s+= ("\nPecas Simples necessarias para " + this.nome);
		s+= ("\n-------------------------------");
		for(Pair p: composicao) {
			if((p.peca instanceof PecaSimples))
				s+= ("\n" + p.toString());
			if((p.peca instanceof PecaCompost))
				s+= (listarSim(p.peca));
		}
		s+=  ("\n-------------------------------");
		return s;
	}

	public void colocar (Peca comp, int qtde) {
		composicao.add(new Pair(comp, qtde));
	}

	public String toString()  {
		return "(" + this.nome + "," + this.valor + "," + composicao + ")";
	}
}

public class Pecas {
	
	public static void CalculaComp (HashMap<String, Peca> hash) {
		int cores = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(cores);
		for (String key : hash.keySet()) {
		    Calcula root = new Calcula(hash.get(key));
			pool.submit(root);
			hash.get(key).valor = root.join();
		}
		pool.shutdown();
	}
	
	public static void main(String[] args) {
		// Lê o arquivo
		HashMap<String, Peca> lista = Peca.buildPecas(("/home/lukakas/dev/fatec/Prova_P2/pecas_fabrica.txt"));
		System.out.println(lista.toString());
		//System.out.println(lista.get("A"));
		//Peca.calculaCompostos(lista);
		CalculaComp(lista);
		System.out.println(lista.toString());
		Peca.listaSimples(lista);
	}
}
