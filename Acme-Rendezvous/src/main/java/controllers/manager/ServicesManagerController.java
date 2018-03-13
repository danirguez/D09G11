package controllers.manager;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import services.ActorService;
import services.CategoryService;
import services.ManagerService;
import services.RendezvousService;
import services.ServicesService;
import controllers.AbstractController;
import domain.Category;
import domain.Manager;
import domain.Rendezvous;
import domain.Services;

@Controller
@RequestMapping("/services/manager")
public class ServicesManagerController extends AbstractController {

	@Autowired
	private ServicesService servicesService;

	@Autowired
	private ActorService actorService;

	@Autowired
	private RendezvousService rendezvousService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ManagerService managerService;
	
	// Creation ---------------------------------------------------------------

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView create() {
		ModelAndView res;
		
		Services services = this.servicesService.create();
			
		res = this.createEditModelAndView(services);

		return res;
	}

	@RequestMapping(value = "/listMyServices", method = RequestMethod.GET)
	public ModelAndView list() {
		ModelAndView result;

		Manager manager = (Manager) this.actorService.findByPrincipal();
		Collection<Services> services = servicesService.servicesByManager(manager);

		result = new ModelAndView("services/listMyServices");
		result.addObject("services", services);
		result.addObject("requestURI", "services/manager/listMyServices.do");

		return result;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public ModelAndView edit(@RequestParam final int servicesId) {
		ModelAndView result;
		Services services;

		services = this.servicesService.findOne(servicesId);
		result = this.createEditModelAndView(services);
		Collection<Rendezvous> rendezvous = this.rendezvousService.findRendezvousNotCancelled();
		result.addObject("rendezvous", rendezvous);
		Collection<Category> categories = this.categoryService.findAll();
		result.addObject("categories", categories);

		return result;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST, params = "save")
	public ModelAndView save(@Valid Services services, final BindingResult binding) {
		ModelAndView res;
		this.managerService.checkAuthority();
		services = this.servicesService.reconstruct(services, binding);
		
		Manager manager = managerService.findByPrincipal();
		services.setManager(manager);
		
		if (binding.hasErrors())
			res = this.createEditModelAndView(services, "services.params.error");
		else
			try {
				this.servicesService.save(services);
				
				res = new ModelAndView("redirect:../../");
			} catch (final Throwable oops) {
				System.out.println(oops.getMessage());
				System.out.println(oops.getCause());
				res = this.createEditModelAndView(services, "services.commit.error");
			}
		return res;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST, params = "delete")
	public ModelAndView delete(@Valid final Services services, final BindingResult binding) {
		ModelAndView res;
		try {
			this.servicesService.delete(services);
			res = new ModelAndView("redirect:../../");
		} catch (final Throwable oops) {
			res = this.createEditModelAndView(services, "services.commit.error");
		}
		return res;
	}

	protected ModelAndView createEditModelAndView(final Services services) {
		ModelAndView result;

		result = this.createEditModelAndView(services, null);

		return result;
	}

	protected ModelAndView createEditModelAndView(final Services services, final String message) {
		ModelAndView result;

		Collection<Rendezvous> rendezvous = new ArrayList<Rendezvous>();
		Collection<Category> category = new ArrayList<Category>();
		
		rendezvous = this.rendezvousService.findRendezvousNotCancelled();
		for(Rendezvous r: rendezvous){
			if(r.getServices() != null){
				rendezvous.remove(r);
			}
		}
		category = categoryService.findAll();
		
		result = new ModelAndView("services/manager/edit");
		result.addObject("services", services);
		result.addObject("rendezvous", rendezvous);
		result.addObject("categories", category);
		result.addObject("message", message);
		result.addObject("requestUri", "services/manager/edit.do");

		return result;
	}

}
