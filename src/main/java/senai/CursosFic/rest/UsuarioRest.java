package senai.CursosFic.rest;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import Enum.LogsEnum;
import Enum.TipoUsuario;

import senai.CursosFic.model.Log;
import senai.CursosFic.model.TokenJWT;
import senai.CursosFic.model.Usuario;
import senai.CursosFic.repository.FazerLogRepository;
import senai.CursosFic.repository.UsuarioRepository;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/usuario")
public class UsuarioRest implements HandlerInterceptor {

	@Autowired
	private UsuarioRepository repository;
	
	@Autowired
	private FazerLogRepository fazerLogRepository;

	public static final String EMISSOR = "3M1SSORS3CR3t0";

	public static final String SECRET = "S3Cr3t0CUrS0F1C";

	// API DE CRIAR OS USUARIOS
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> criarUsuario(@RequestBody Usuario usuario, Log log, HttpServletRequest servlet) {

		List<Usuario> list = repository.findAll();

		// percorre todos os usuarios e verifica se o email ja existe no bd
		for (Usuario u : list) {
			if (u.getEmail().equals(usuario.getEmail())) {

				return ResponseEntity.status(HttpStatus.CONFLICT).body(usuario.getEmail());

			}

			else if (u.getNif().equals(usuario.getNif())) {

				return ResponseEntity.status(HttpStatus.NOT_EXTENDED).body(usuario.getNif());
			}
		}

		// faz a verificação de campos vazio
		if (usuario.getNome().equals("") || usuario.getEmail().equals("") || usuario.getNif().equals("")
				|| usuario.getTipoUsuario() == null) {
			// envia um status de erro ao front

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(usuario);

		} else {
			
			usuario.setSenha(usuario.getNif());
			
			repository.save(usuario);
			
			Date date = new Date();
			
			String horaAtual = new SimpleDateFormat("HH:mm:ss").format(date);
			
			String dataAtual = new SimpleDateFormat("dd/MM/yyyy").format(date);
		
			log.setHora(horaAtual);
			
			log.setData(dataAtual);
			
			String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJub21lX3VzdWFyaW8iOiJEaW9vZyIsImlkX3VzdWFyaW8iOjEsImlzcyI6IjNNMVNTT1JTM0NSM3QwIiwidXN1YXJpbyI6IlVzdWFyaW8oaWQ9MSwgbm9tZT1EaW9vZywgZW1haWw9ZW1haWxAZ21haWwuY29tLCBuaWY9MTIzNDU2Nywgc2VuaGE9OTM4NmQ5MDlkNmYwODgzOTM0ZjgxYWRmODZlOTQ0NzJmNGJjNGU1OTg4YmIyNDdjYzM4ZDAxMWFkN2U3YTY5ZCwgcmVkZWZpbmlyU2VuaGE9dHJ1ZSwgdGlwb1VzdWFyaW89TWFzdGVyKSIsImV4cCI6MTY2ODE5MTU4NiwidGlwb191c3VhcmlvIjoiTWFzdGVyIn0.AG-BZcGHdI5R_oxwlzrcP9klfmX6jwCae4MS6JmKzKg";

			try {
				
				System.out.println("TOKENN " + token);
				
				//algoritmo para descriptografar
				Algorithm algoritmo = Algorithm.HMAC256(UsuarioRest.SECRET);
				
				JWTVerifier verifier =  JWT.require(algoritmo).withIssuer(UsuarioRest.EMISSOR).build();
				//linha que vai validar o token
				DecodedJWT jwt = verifier.verify(token); 
				//extrair os dados do payload
				Map<String, Claim> payload = jwt.getClaims();
				
				System.out.println(payload.get("nome_usuario"));
				
				String nomeUsuario = payload.get("nome_usuario").toString();
				
				nomeUsuario = nomeUsuario.substring(1, nomeUsuario.length() - 1);
				
				System.out.println("NOMEE: " + nomeUsuario);
				
				log.setNomeUsuario(nomeUsuario);
				
				log.setLogsEnum(LogsEnum.CADASTROU);
				
				
				fazerLogRepository.save(log);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
			return ResponseEntity.created(URI.create("/" + usuario.getId())).body(usuario);

		}
	}

	// API DE LISTAR OS USUARIOS
	@RequestMapping(value = "", method = RequestMethod.GET)
	public Iterable<Usuario> listarUsuario() {

		return repository.findAll();
	}

	// API DE DELETAR USUARIO
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> excluirUsuario(@PathVariable("id") Long idUsuario) {

		repository.deleteById(idUsuario);

		// RETORNO SEM CORPO
		return ResponseEntity.noContent().build();

	}

	// API DE ALTERAR USUARIO
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> alterarUsuario(@RequestBody Usuario usuario, @PathVariable("id") Long idUsuario) {

		if (idUsuario != usuario.getId()) {
			throw new RuntimeException("id não existente!");

		}

		repository.save(usuario);

		HttpHeaders headers = new HttpHeaders();

		headers.setLocation(URI.create("/api/usuario"));

		return new ResponseEntity<Void>(headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/tipo/{tipo}", method = RequestMethod.GET)
	public List<Usuario> getUsuariosByTipo(@PathVariable("idTipo") TipoUsuario tipo) {
		return repository.findByTipoUsuario(tipo);
	}

	// API BUSCAR USUARIO
	@RequestMapping(value = "/buscar/{nome}", method = RequestMethod.GET)
	public List<Usuario> buscarUsuario(@PathVariable("nome") String nome) {
		return repository.buscarUsuario(nome);
	}

	// api para logar o usuário
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> logar(@RequestBody Usuario usuario) {

		// trás uma lista de usuários
		List<Usuario> list = repository.findAll();

		// for pra percorrer a lista de usuários
		for (Usuario u : list) {

			// verifica se o nif digitado é igual ao do banco de dados
			if (u.getNif().equals(usuario.getNif()) && u.getSenha().equals(usuario.getSenha())) {

				// Adicionar valores para o token
				Map<String, Object> payload = new HashMap<String, Object>();

				payload.put("id_usuario", u.getId());

				payload.put("nome_usuario", u.getNome());

				String tipo = u.getTipoUsuario().toString();
				
				payload.put("usuario", u.toString());
				
				payload.put("tipo_usuario", tipo);

				Calendar expiracao = Calendar.getInstance();

				// expirar sessão do usuario que estiver logado depois de uma hora
				expiracao.add(Calendar.HOUR, 1);

				// algoritmo para assinar o token
				Algorithm algorithm = Algorithm.HMAC256(SECRET);

				// gerar o token
				TokenJWT tokenJwt = new TokenJWT();

				tokenJwt.setToken(JWT.create().withPayload(payload).withIssuer(EMISSOR)
						.withExpiresAt(expiracao.getTime()).sign(algorithm));

				if (u.isRedefinirSenha() == false) {
					Long id = u.getId();

					return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).body(u);

				} else {

					return ResponseEntity.ok(tokenJwt);
				}

			}

		}

		return new ResponseEntity<TokenJWT>(HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/verificarParametro", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> verificarparametro(@RequestBody Usuario usuario) {

		List<Usuario> lista = repository.findAll();

		for (Usuario u : lista) {

			if (u.getEmail().equals(usuario.getEmail())) {
				
				

				return ResponseEntity.status(HttpStatus.OK).build();

			}

		}

		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}

	@RequestMapping(value = "/redefinirSenha/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> redefinirSenha(@RequestBody Usuario usuario, @PathVariable("id") Long idUsuario) {
		
		usuario.setRedefinirSenha(true);

		repository.save(usuario);
		

		HttpHeaders headers = new HttpHeaders();

		headers.setLocation(URI.create("/api/usuario"));

		return new ResponseEntity<Void>(headers, HttpStatus.OK);
	     

		

	}

}