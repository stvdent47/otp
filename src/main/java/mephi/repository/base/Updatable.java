package mephi.repository.base;

public interface Updatable<T> {
    boolean update(T item);
}
