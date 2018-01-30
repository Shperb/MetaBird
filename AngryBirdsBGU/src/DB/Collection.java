package DB;

import java.util.ArrayList;

public class Collection<E> extends ArrayList<E>{

	private static final long serialVersionUID = 1L;

	public E getLast() {
		if (isEmpty()) {
			return null;
		}
		else {
			return get(size() - 1);
		}
	}
}
