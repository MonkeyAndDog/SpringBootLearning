package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
@RequestMapping("/demo")
public class MainController {

    private UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    @RequestMapping("/add/user")
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        userRepository.save(user);
        return "储存成功";
    }

    @RequestMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping("/add/person")
    @ResponseBody
    public String addPerson(@Valid PersonForm personForm, BindingResult result) {
        if (result.hasErrors()) {
            logger.info(result.toString());
            return "参数非法";
        }
        logger.info(personForm.toString());
        return personForm.toString();
    }
}
