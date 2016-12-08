package me.krstic.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import me.krstic.model.Digital;

@Repository
public interface DigitalRepository extends CrudRepository<Digital, Long> {

}
