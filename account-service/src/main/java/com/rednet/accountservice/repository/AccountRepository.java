package com.rednet.accountservice.repository;

import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.model.AccountUniqueFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameOrEmail (String username, String email);
    Optional<Account> findByUsername        (String username);
    Optional<Account> findByEmail           (String email);

    boolean existsByUsername    (String username);
    boolean existsByEmail       (String email);

    List<Account> findAllByUsernameOrEmail(String username, String email);
    List<AccountUniqueFields> findAllUniqueFieldsByUsernameOrEmail(String username, String email);
}
