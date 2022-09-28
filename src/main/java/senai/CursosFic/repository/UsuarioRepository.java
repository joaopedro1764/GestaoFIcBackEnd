package senai.CursosFic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import Enum.TipoUsuario;
import senai.CursosFic.model.Usuario;

public interface UsuarioRepository extends PagingAndSortingRepository<Usuario, Long> {

	public List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);


	

	public List<Usuario> findAll();

	
	@Query("SELECT t FROM Usuario t WHERE t.nome LIKE %:p% ORDER BY t.nome ASC")
	public List<Usuario>buscarUsuario(@Param("p") String parametro);

}
