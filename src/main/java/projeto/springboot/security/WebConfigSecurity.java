package projeto.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{

	@Autowired
	private ImplementacaoUserDatailsService implementacaoUserDatailsService;
	
	@Override // Configura as solicitações de acesso por http
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
		.disable()  // Desativa as configurações padrao de memoria
		.authorizeRequests() // Permitir restringir acessos
		.antMatchers(HttpMethod.GET, "/").permitAll() // Qualquer usuario acessa a pagina inicial
		.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN") // Essa parte da url so pode ser acessada por administrador
		.anyRequest().authenticated()
		.and().formLogin().permitAll() // Permite qualquer usuario
		.loginPage("/login") // Configuração da pagina inicial
		.defaultSuccessUrl("/cadastropessoa") // Se o login foi feito com sucesso redireciona para pagina de cadastro
		.failureUrl("/login?error=true") // se o login der erro a pagina e redirecionada para a pagina de login
		.and().logout().logoutSuccessUrl("/login") // Ao realizar o logout retorna para a tela de login
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	
	@Override  // Cria autenticação do usuário com banco de dados ou em memória
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementacaoUserDatailsService).passwordEncoder(new BCryptPasswordEncoder());
		
		
		/*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("wep")
		.password("$2a$10$fV1UCAj03RLLsJMP8MJNJOdXH67Oovv713y1dvn406yVdN0AP1I22")
		.roles("ADMIN");*/
		
	}
	
	
	@Override // Ignora URL especificas
	public void configure(WebSecurity web) throws Exception {
		
		web.ignoring().antMatchers("/materialize/**");
	}
}
