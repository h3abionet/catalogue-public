package bio.tech.catalog.web.api;

import bio.tech.catalog.persistence.dao.CartRepository;
import bio.tech.catalog.persistence.dao.ProjectRepository;
import bio.tech.catalog.persistence.model.NeoCart;
import bio.tech.catalog.persistence.model.NeoProject;
import bio.tech.catalog.persistence.model.User;
import bio.tech.catalog.service.UserService;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ProjectApiConroller {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartRepository cartRepository;

    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> projectCards() {

        User user = authenticatedUser();
        List<NeoProject> projects = projectRepository.userProjects(user.getUsername());
        List<JSONObject> projectCards = new ArrayList<>();
        for (NeoProject project : projects) {
            JSONObject obj = new JSONObject();
            obj.put("project", project);
            List<NeoCart> approvedCards = new ArrayList<>();
            List<NeoCart> requestedCards = new ArrayList<>();
            List<NeoCart> pendingCards = new ArrayList<>();
            List<NeoCart> carts = cartRepository.projectCarts(project.getId());
            for (NeoCart cart : carts) {
                switch (cart.getStatus()) {
                    case "Approved":
                        approvedCards.add(cart);
                        break;
                    case "Requested":
                        requestedCards.add(cart);
                        break;
                    case "Pending":
                        pendingCards.add(cart);
                        break;
                }
            }
            obj.put("approved-cards", approvedCards);
            obj.put("requested-cards", requestedCards);
            obj.put("pending-cards", pendingCards);
            projectCards.add(obj);
        }

        JSONObject response = new JSONObject();
        response.put("project-cards", projectCards);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private URI createOrEditProject(String obj, MultipartFile[] reports, boolean create) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        NeoProject project = mapper.readValue(obj, NeoProject.class);
        User user = authenticatedUser();
        String path = "./users/" + user.getUsername() + "/" + project.getProjectId();
        File folder = new File(path);
        if (create)
            folder.mkdir();
        Path pathLocation = Paths.get(folder.getAbsolutePath());
        for(MultipartFile r : reports) {
            if (! r.isEmpty())
                Files.copy(r.getInputStream(), pathLocation.resolve(r.getOriginalFilename()));
        }
        project.setUser(new ArrayList<>(Arrays.asList(user)));
        NeoProject savedProject = projectRepository.save(project);

        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedProject.getId()).toUri();
    }

    @RequestMapping(value = "/create/project", method = RequestMethod.POST)
    public ResponseEntity<Object> createProject(@RequestParam("project") String obj,
                                                @RequestParam("reports") MultipartFile[] reports) throws IOException {

        URI location = createOrEditProject(obj, reports, true);

        return ResponseEntity.created(location).build();
    }

    @RequestMapping(value = "/edit/project", method = RequestMethod.POST)
    public ResponseEntity<Object> editProject(@RequestParam("project") String obj,
                                              @RequestParam("reports") MultipartFile[] reports) throws IOException {
        URI location = createOrEditProject(obj, reports, false);

        return ResponseEntity.created(location).build();
    }

    @RequestMapping(value = "/project/edit/{id}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> editProject(@PathVariable("id") String projectId) throws FileNotFoundException {
        JSONObject obj = new JSONObject();
        User user = authenticatedUser();
        NeoProject project = projectRepository.findProjectByProjectIdAndUsername(user.getUsername(), projectId);

        obj.put("project", project);

        String path = "./users/" + user.getUsername() + "/" + project.getProjectId();
        File dir = new File(path);

        Map<String, String> files = new LinkedHashMap<>();
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".pdf")) {
                files.put(file.getName(), "./users/" + user.getUsername() + "/" + project.getProjectId());
            }
        }

        obj.put("files", files);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @RequestMapping(value = "/project/delete/file", method = RequestMethod.POST)
    public ResponseEntity<JSONObject> deleteProjectFile(@RequestBody Map<String, String> body) throws IOException {
        JSONObject obj = new JSONObject();

        String path = body.get("path");
        File file = new File(path);
        String filename = file.getName();
        file.delete();

        obj.put("delete", filename + " deleted successfully.");

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> deleteProject(@PathVariable("id") String projectId) throws IOException {

        User user = authenticatedUser();
        NeoProject project = projectRepository.projectByUsernameAndProjectId(user.getUsername(), projectId);
        List<NeoCart> carts = cartRepository.projectCarts(project.getId());

        JSONObject obj = new JSONObject();
        if (!carts.isEmpty()) {
            obj.put("error", "Project with id " + projectId + " is linked to Cart objects. Delete first the carts!");
        } else {
            projectRepository.deleteByProjectId(projectId);
            obj.put("message", "Project with id " + projectId + " has been deleted");

            String path = "./users/" + user.getUsername() + "/" + projectId;
            File folder = new File(path);
            FileUtils.deleteDirectory(folder);
        }

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    /*
     * NON API
     */

    private User authenticatedUser() {
        final org.springframework.security.core.userdetails.User authenticated = (org.springframework.security.core.userdetails.User)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userService.findUserByEmail(authenticated.getUsername());
    }
}
