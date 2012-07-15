package it.unisannio.legiolinteata.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;


public abstract class Advisor<T> {
	public static final class Advice<T> implements Comparable<Advice<T>>{
		private final T object;
		private final double value;
		
		private Advice(Advisor<T> advisor, T object) {
			this.object = object;
			this.value = advisor.evaluate(object);
		}
		
		public final T getObject() {
			return object;
		}
		
		public final double getValue() {
			return value;
		}
		
		@Override
		public int compareTo(Advice<T> arg0) {
			return value == arg0.value ? 0 :
				(value < arg0.value ? 1 : -1);
		}
		
		@Override
		public String toString() {
			return "\"" + object + "\" (" + getValue() + ")";
		}
	}
	
	private FIS logic;
	private FunctionBlock function;
	
	public Advisor(String fcl, String block) {
		logic = FIS.load(fcl);
		if(logic == null) {
			throw new RuntimeException("Cannot load fuzzy inference system '" + fcl + "'");
		}
		
		function = logic.getFunctionBlock(block);
	}
	
	protected abstract List<T> generate();
	protected abstract double evaluate(T object);
	
	public FIS getFuzzyInferenceSystem() {
		return logic;
	}
	
	public FunctionBlock getFunctionBlock() {
		return function;
	}
	
	protected SortedSet<Advice<T>> buildAdvices() {
		List<T> candidates = generate();
		TreeSet<Advice<T>> advices = new TreeSet<Advice<T>>();
		
		for(T candidate : candidates) {
			advices.add(new Advice<T>(this, candidate));
		}
		
		return advices;
	}
	
	public List<Advice<T>> getAdvices() {
		return new ArrayList<Advice<T>>(buildAdvices());
	}
	
	public List<T> getBestAdvices(int limit, double cutoff) {
		List<Advice<T>> advices = getAdvices();

		limit = Math.min(limit, advices.size());
		List<T> best = new ArrayList<T>(limit);
		
		
		for(int i = 0; i < limit; ++i) {
			Advice<T> a = advices.get(i);
			if(a.getValue() < cutoff) 
				break;
			
			best.add(advices.get(i).getObject());
		}
		
		return best;
	}
	
	public T getBestAdvice(double cutoff) {
		SortedSet<Advice<T>> advices = buildAdvices();
		System.out.println(advices);
		Advice<T> best = advices.first();
		System.out.println("Picked " + best);
		
		return (best.getValue() < cutoff) ? null : best.getObject();
	}
}
