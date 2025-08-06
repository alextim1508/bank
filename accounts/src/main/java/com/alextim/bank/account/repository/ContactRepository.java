package com.alextim.bank.account.repository;

import com.alextim.bank.account.constant.ContactType;
import com.alextim.bank.account.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByTypeAndValue(ContactType type, String value);

    Optional<Contact> findByTypeAndValue(ContactType type, String value);

    List<Contact> findByAccount_Login(String login);
}
