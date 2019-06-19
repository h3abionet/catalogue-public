package bio.tech.catalog.web.controller;

import bio.tech.catalog.persistence.dao.PrivilegeRepository;
import bio.tech.catalog.persistence.dao.RoleRepository;
import bio.tech.catalog.persistence.model.Privilege;
import bio.tech.catalog.persistence.model.Role;
import bio.tech.catalog.persistence.model.User;
import bio.tech.catalog.registration.OnRegistrationCompleteEvent;
import bio.tech.catalog.security.ISecurityUserService;
import bio.tech.catalog.service.UserService;
import bio.tech.catalog.web.dto.PasswordDto;
import bio.tech.catalog.web.dto.UserDto;
import bio.tech.catalog.web.error.InvalidOldPasswordException;
import bio.tech.catalog.web.error.UserNotFoundException;
import bio.tech.catalog.web.util.GenericResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
public class RegistrationController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private ISecurityUserService securityUserService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @PostMapping(value = "/user/registration")
    @ResponseBody
    public GenericResponse registerUserAccount(@Valid final UserDto accountDto, final HttpServletRequest request) {
        LOGGER.debug("Registering user account with information: {}", accountDto);

        final User registered = userService.registerNewUserAccount(accountDto);
        for (Role role : registered.getRoles()) {
            if (role.getName().equals("ROLE_USER")) {
                eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
                break;
            }
        }
        return new GenericResponse("success");
    }


    // TODO: the AppUrl is hardcoded (need to be set automatically)
    private String getAppUrl(HttpServletRequest request) {
        return "https://catalog.h3africa.org" + request.getContextPath();
    }

    @GetMapping(value = "/registrationConfirm")
    public String confirmRegistration(final HttpServletRequest request, final Model model,
                                      @RequestParam("token") final String token) throws UnsupportedEncodingException {

        Locale locale = request.getLocale();
        final String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            final User user = userService.getUser(token);
            // TODO: DO WE NEED TO AUTHENTICATE WITHOUT PASSWORD HERE (KEEP IT FOR NOW)
            authWithoutPassword(user);
            return "redirect:/logout?message=" + messages.getMessage("message.accountVerified", null, locale);
        }

        model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
        model.addAttribute("expired", "expired".equals(result));
        model.addAttribute("token", token);
        return "redirect:/badUser.html?lang=" + locale.getLanguage();
    }

    @GetMapping(value = "/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping(value = "/user/resetPassword")
    @ResponseBody
    public GenericResponse resetPassword(final HttpServletRequest request, @RequestParam("email") final String userEmail) {
        final User user = userService.findUserByEmail(userEmail);
        if (user == null) {
            throw new UserNotFoundException(messages.getMessage("message.badEmail", null, request.getLocale()));
        }
        Collection<User> collection = new ArrayList<>(Arrays.asList(user));
        final String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(collection, token);
        mailSender.send(constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user));
        return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
    }

    @GetMapping(value = "/user/changePassword")
    public String showChangePasswordPage(final Locale locale, final Model model, @RequestParam("id") final Long id,
                                         @RequestParam("token") final String token) {
        final String result = securityUserService.validatePasswordResetToken(id, token);
        if (result != null) {
            model.addAttribute("message", messages.getMessage("auth.message", null, locale));
            return "redirect:/login?lang=" + locale.getLanguage();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "redirect:/updatePassword.html?lang=" + locale.getLanguage();
    }

    @PostMapping(value = "/user/savePassword")
    @ResponseBody
    public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changeUserPassword(user, passwordDto.getNewPassword());
        return new GenericResponse(messages.getMessage("message.resetPasswordSuc", null, locale));
    }

    @PostMapping(value = "/user/updatePassword")
    @ResponseBody
    public GenericResponse changeUserPassword(final Locale locale, @Valid PasswordDto passwordDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        final User user = userService.findUserByEmail(email);
        if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            throw new InvalidOldPasswordException();
        }
        userService.changeUserPassword(user, passwordDto.getNewPassword());
        return new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, locale));
    }

    @GetMapping("/user/delete")
    public String removeUser(@RequestParam("username") String username, Model model) throws IOException {
        User user = userService.findUserByUsername(username);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ARCHIVE") || role.getName().equals("ROLE_BIOBANK")) {
                String path = "./users/" + user.getUsername();
                File folder = new File(path);
                FileUtils.deleteDirectory(folder);
            } else if (role.getName().equals("ROLE_USER")){
                String path = "./users/" + user.getUsername();
                File folder = new File(path);
                FileUtils.deleteDirectory(folder);
            }
        }

        userService.deleteUser(Arrays.asList(user));

        return "redirect:/users";
    }

    private SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale, final String token, final User user) {
        final String url = contextPath + "/user/changePassword?id=" + user.getId() + "&token=" + token;
        final String message = messages.getMessage("message.resetPassword", null, locale);
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.mail"));
        return email;
    }

    private void authWithoutPassword(User user) {
        final List<String> privileges = new ArrayList<>();
        final List<Privilege> collection = new ArrayList<>();
        if (user.getRoles() == null) {
            user.setRoles(roleRepository.findByUserId(user.getId()));
        }
        for (final Role role : user.getRoles()) {
            if (role.getPrivileges() == null) {
                collection.addAll(privilegeRepository.findByRoleName(role.getName()));
            } else {
                collection.addAll(role.getPrivileges());
            }
        }
        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
