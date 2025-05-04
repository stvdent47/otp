package mephi.repository.base;

public interface Creatable<T> {
    boolean create(T item);
}
