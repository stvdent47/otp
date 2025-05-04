package mephi.repository.base;

import java.util.List;

public interface WithGetByCondition<T> {
    List<T> getByCondition(String condition);
}
