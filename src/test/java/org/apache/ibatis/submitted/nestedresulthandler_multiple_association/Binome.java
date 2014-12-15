package org.apache.ibatis.submitted.nestedresulthandler_multiple_association;

public class Binome<T, U> {
	private T one;
	private U two;

	public Binome() {
	}

	public Binome(final T one, final U two) {
		this.one = one;
		this.two = two;
	}

	public T getOne() {
		return one;
	}

	public void setOne(T one) {
		this.one = one;
	}

	public U getTwo() {
		return two;
	}

	public void setTwo(U two) {
		this.two = two;
	}

	@Override
	public int hashCode() {
		return (one != null ? one.hashCode() : 0) + (two != null ? two.hashCode() : 0);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Binome<?, ?>) {
			Binome<?, ?> bin = (Binome<?, ?>) obj;
			return one != null && one.equals(bin.getOne()) && two != null && two.equals(bin.getTwo());
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "Binome [one=" + one + ", two=" + two + "]";
	}
}
