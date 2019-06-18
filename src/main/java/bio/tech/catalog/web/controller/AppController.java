package bio.tech.catalog.web.controller;

import bio.tech.catalog.forms.ContactFrom;
import bio.tech.catalog.forms.SearchForm;
import bio.tech.catalog.forms.SearchFormSession;
import bio.tech.catalog.persistence.dao.*;
import bio.tech.catalog.persistence.model.*;
import bio.tech.catalog.service.UserService;
import bio.tech.catalog.utils.StudyQuery;
import bio.tech.catalog.web.dto.UserDto;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class AppController {

    private SearchFormSession searchFormSession;
    @Autowired
    public AppController(SearchFormSession searchFormSession) {
        this.searchFormSession = searchFormSession;
    }

    @Autowired
    UserService userService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    DesignRepository designRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    SpecTypeRepository specTypeRepository;

    @Autowired
    CharacterRepository characterRepository;

    @Autowired
    EthnicityRepository ethnicityRepository;

    @Autowired
    SpecimenRepository specimenRepository;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    DatasetRepository datasetRepository;

    @Autowired
    private StudyQuery studyQuery;

    /*
     *   SEARCH MAPPING
     */

    @RequestMapping("/")
    public String showSearchPage() {

        return "redirect:/search";
    }

    @RequestMapping("/search")
    public String searchPage(@RequestParam(name="advance", defaultValue="") String advance, Model model) {
        Collection<NeoStudy> studies = new ArrayList<>();
        SearchForm form = searchFormSession.toForm();
        if (advance.isEmpty()) {
            studies = showAllStudies(advance, form);
        }else {
            //form = searchFormSession.toForm();
            final Map<String, String> parameters = new LinkedHashMap<>();
            parameters.put("description", form.getDescriptionKeys());
            parameters.put("studyName", form.getStudyName());
            parameters.put("disease", form.getDisease());
            parameters.put("design", form.getDesign());
            parameters.put("country", form.getCountry());
            parameters.put("gender", form.getGender());
            parameters.put("specType", form.getSpecType());
            parameters.put("character", form.getCharacter());
            parameters.put("ethnicity", form.getEthnicity());
            parameters.put("bmiOp", form.getBmiOp());
            parameters.put("bmiVal", form.getBmiVal());
            parameters.put("ageOp", form.getAgeOp());
            parameters.put("ageVal", form.getAgeVal());

            final Map<String, String> definedParams = studyQuery.removeNullParameters(parameters);
            String query = studyQuery.constructQuery(definedParams);
            if (query.isEmpty())
                studies = showAllStudies(advance, form);
            else
                studies = studyQuery.runNeoQuery(query, definedParams);

            if (form.getHasSpecimens())
                studies.removeIf((NeoStudy study) -> study.getTotalSpecimens() < 1);

            if (form.getHasDataSets())
                studies.removeIf((NeoStudy study) -> study.getDataSets().size() < 1);
        }

        int countStudies = studyRepository.countStudies();
        model.addAttribute("countStudies", countStudies);
        int countSamples = (int)specimenRepository.count();
        model.addAttribute("countSamples", countSamples);
        int countDatasets = (int)datasetRepository.count();
        model.addAttribute("countDatasets", countDatasets);
        model.addAttribute("studies", studies);

        createFieldSelections(form);
        // this line is needed because createFieldSelections update the form object
        model.addAttribute("searchForm", form);

        return "home";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String advancedSearch(@Valid @ModelAttribute("searchForm") SearchForm searchForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return "home";
        }

        searchFormSession.saveForm(searchForm);

        return "redirect:/search?advance=advance";
    }

    /*
     * CONTACT MAPPING
     */

    @RequestMapping("/contact")
    public String showContactPage(@ModelAttribute ContactFrom contactFrom) {

        return "contact";
    }

    /*
     * List users
     */

    @RequestMapping("/users")
    public String catalogUsers(@ModelAttribute("registerForm") UserDto registerForm, Model model) {

        Collection<User> users = userService.findAllUsers();
        model.addAttribute("users", users);

        return "users";
    }

    @RequestMapping("/enable")
    public String validateUser(@RequestParam("email") String email, Model model) throws IOException {
        User user = userService.findUserByEmail(email);
        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_USER")) {
                String path = "./users/" + user.getUsername();
                File folder = new File(path);
                folder.mkdir();
            }
        }
        user.setEnabled(true);
        userService.saveRegisteredUser(user);

        return "redirect:/users";
    }

    /*
     * All pages with url starting with "advance" when refreshed point to "filter.html" page
     */
    @RequestMapping(value={"/advance/**"})
    public String filterPage() {
        return "redirect:/filter.html";
    }

    /*
     * NON API
     */

    private User authenticatedUser() {
        final org.springframework.security.core.userdetails.User authenticated = (org.springframework.security.core.userdetails.User)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userService.findUserByEmail(authenticated.getUsername());
    }

    private Collection<NeoStudy> showAllStudies (String advance, SearchForm form) {

        if (advance.isEmpty() || (!form.getHasSpecimens() && !form.getHasDataSets()))
            searchFormSession.clearForm();

        Collection<NeoStudy> studies = studyRepository.findAll();

        for (NeoStudy study: studies) {
            study.setSearchNoSpecimens(study.getNoSpecimens());
            study.setSearchNoParticipants(study.getParticipants().size());
        }

        return studies;
    }

    private void createFieldSelections(SearchForm form) {
        Collection<NeoDesign> designs = designRepository.findAll();
        Collection<NeoCountry> countries = countryRepository.findAll();
        Collection<NeoGender> genders = genderRepository.findAll();
        Collection<NeoSpecType> specTypes = specTypeRepository.findAll();
        Collection<NeoCharacter> characters = characterRepository.findAll();
        Collection<NeoEthnicity> ethnicities = ethnicityRepository.findAll();

        Collection<NeoStudy> studies = studyRepository.findAll();
        Collection<String> diseases = new HashSet<>();
        for (NeoStudy study: studies) {
            if (!study.getDisease().isEmpty()) {
                diseases.add(study.getDisease());
            }
        }

        form.setDiseases(diseases);
        form.setDesigns(designs);
        form.setCountries(countries);
        form.setGenders(genders);
        form.setSpecTypes(specTypes);
        form.setCharacters(characters);
        form.setEthnicities(ethnicities);
    }
}
