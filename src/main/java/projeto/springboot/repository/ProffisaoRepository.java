package projeto.springboot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import projeto.springboot.model.Profissao;

@Transactional
@Repository
public interface ProffisaoRepository extends CrudRepository<Profissao, Long>{

}
