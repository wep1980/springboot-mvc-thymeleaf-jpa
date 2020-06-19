package projeto.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import projeto.springboot.model.Pessoa;
import projeto.springboot.model.Telefone;
import projeto.springboot.repository.PessoaRepository;
import projeto.springboot.repository.ProffisaoRepository;
import projeto.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProffisaoRepository profissaoRepository;
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		//Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());
		
		return modelAndView;
	}
	
	
	
	@GetMapping("/pessoapage") // Metodo de paginação
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model, @RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);

		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value ="**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {
		
		// Aqui pegamos os telefones da pessoa, como o mapeamento e feito em cascata com as operações de crud os objetos devem estar associados
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) { // Se ocorrer erros
			
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // Continua na tela de cadastro
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome")))); // Mostra a lista de pessoas de 5 em 5 por pagina
			modelAndView.addObject("pessoaobj", pessoa); // Retorna com os objetos preenchidos na tela
			
			List<String> msg = new ArrayList<String>();
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); // Vem das anotações @NotBlank
			}
			modelAndView.addObject("msg", msg);
			modelAndView.addObject("profissoes", profissaoRepository.findAll());

			return modelAndView;
		}
		
		if(file.getSize() > 0) { // Cadastrando curriculo novo
			pessoa.setCurriculo(file.getBytes()); 
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
			
		}else { //  Se não cadastrou nenhum curriculo novo
			
			if(pessoa.getId() != null && pessoa.getId() > 0) { // Editando
				
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get(); // Carrega a pessoa do banco de dados e coloca em uma variavel temporaria
				
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
			}
		}
		pessoaRepository.save(pessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))) );
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		return modelAndView;
	}
	
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))) );
		modelAndView.addObject("pessoaobj", new Pessoa());

		return modelAndView;
		
	}
	
	
	
	@GetMapping("/editarpessoa/{idpessoa}") // Mapeamento da URL
	public ModelAndView editar (@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa); // Consulta do objeto
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoaobj", pessoa.get()); // Colocando o objeto em edição passando pra view
		modelAndView.addObject("profissoes", profissaoRepository.findAll()); // Carrega as profissoes

		return modelAndView;
	}
	
	
	
	@GetMapping("/removerpessoa/{idpessoa}") // Mapeamento da URL
	public ModelAndView excluir (@PathVariable("idpessoa") Long idpessoa) {
		
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // Retorna pra tela de cadastro
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome")))); // Listando a lista de pessoas que vai ser carregada
		modelAndView.addObject("pessoaobj", new Pessoa()); // Instancia um objeto vazio

		return modelAndView;
	}
	
	
	//Metodo para download nao tem retorno
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response) throws IOException {
		
		// Consultar objeto pessoa no banco de dados
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get(); // O .get() retorna um tipo pessoa
		if(pessoa.getCurriculo() != null) { // Se tiver curriculo
			
			// Setar tamanho da resposta
			response.setContentLength(pessoa.getCurriculo().length);
			
			// Tipo do arquivo para download ou pode ser generica application/octet-stream
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			//Define o cabeçalho da resposta. Isso é padrão
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			// Finaliza a resposta passando o arquivo
			response.getOutputStream().write(pessoa.getCurriculo());
		}
	}

	
	@PostMapping("**/pesquisapessoa")
	public ModelAndView pesquisarPorNome(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("pesquisasexo") String pesquisasexo, @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		
		Page<Pessoa> pessoas = null;
		if(pesquisasexo != null && !pesquisasexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, pesquisasexo, pageable);
		}else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // Retorna para mesma tela de cadastro
		modelAndView.addObject("pessoas", pessoas);
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("nomepesquisa", nomepesquisa);

		return modelAndView;
		
	}
	
	
    // Metodos void não tem retorno
	@GetMapping("**/pesquisapessoa")
	public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("pesquisasexo") String pesquisasexo, HttpServletRequest request, HttpServletResponse response) 
					throws Exception {
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if(pesquisasexo != null && !pesquisasexo.isEmpty() && nomepesquisa != null &&  !nomepesquisa.isEmpty()) {// Busca por nome e sexo
			
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesquisasexo); 
			
		}else if (nomepesquisa != null &&  !nomepesquisa.isEmpty()) { // Busca somente por nome
			
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
			
		} else if(pesquisasexo != null && !pesquisasexo.isEmpty()) { // Busca somente por sexo
				
			pessoas = pessoaRepository.findPessoaBySexo(pesquisasexo);
			
		} else { // Busca todos
			
			Iterable<Pessoa> iterator = pessoaRepository.findAll();
			for (Pessoa pessoa : iterator) {
				pessoas.add(pessoa);
			}
		}
		
		// Serviço que faz a geração do relatorio do PDF
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		// Tamanho da resposta
		response.setContentLength(pdf.length);
		
		// Definir na resposta o tipo de arquivo -- application/octet-stream serve para qq tipo de arquivo, midia, pdf e etc
		response.setContentType("application/octet-stream");
		
		// Definir o cabeçalho da resposta
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		// Finaliza a resposta
		response.getOutputStream().write(pdf);
	}
	
	
	@GetMapping("/telefones/{idpessoa}") // Mapeamento da URL
	public ModelAndView telefones (@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa); // Consulta do objeto
		
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones"); // Retornando para a tela de telefones
		modelAndView.addObject("pessoaobj", pessoa.get()); // Colocando o objeto em edição passando pra view
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idpessoa)); // Carrega os telefones

		return modelAndView;
	}
	
	
	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addfonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		
		if(telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {
			
			ModelAndView modelAndView = new ModelAndView("cadastro/telefones"); // Vai continuar na mesma tela
			modelAndView.addObject("pessoaobj", pessoa); // Retorna o objeto pessoa
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid)); // Retorna a lista de telefones dessa pessoa
			
            List<String> msg = new ArrayList<String>(); // Faz a validação da mensagem
            
            if(telefone.getNumero().isEmpty()) {
            	msg.add("O numero deve ser informado");
            }
            
            if(telefone.getTipo().isEmpty()) {
            	msg.add("O Tipo deve ser informado");
            }
            modelAndView.addObject("msg", msg);
    		return modelAndView;

		}
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones");
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		return modelAndView;
	}
	
	
	@GetMapping("/removertelefone/{idtelefone}") // Mapeamento da URL
	public ModelAndView removertelefone (@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
        telefoneRepository.deleteById(idtelefone);	
        
		ModelAndView modelAndView = new ModelAndView("cadastro/telefones"); // Retorna pra tela de cadastro
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));

		return modelAndView;
	}
	
}
