package mephi.repository.user;

import mephi.entities.user.User;
import mephi.repository.base.Creatable;
import mephi.repository.base.Deletable;
import mephi.repository.base.WithGetAll;
import mephi.repository.base.WithGetByCondition;

public interface UserRepository extends
    Creatable<User>,
    Deletable,
    WithGetByCondition<User>,
    WithGetAll<User>
{}
