package com.vlzher.pollservice2.controller;

import com.vlzher.pollservice2.dto.*;
import com.vlzher.pollservice2.entity.*;
import com.vlzher.pollservice2.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/polls")
@Slf4j
public class PollController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PollController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @GetMapping
    public ResponseEntity<List<PollDTO>> getAllPolls() {
        List<Poll> polls = pollRepository.findAll();
        List<PollDTO> pollDTOs = polls.stream().map(PollDTO::new).collect(Collectors.toList());
        return new ResponseEntity<>(pollDTOs, HttpStatus.OK);
    }

    // Secured endpoint to create a new poll
    @PostMapping
    public ResponseEntity<Void> createPoll(@RequestBody PollRequest pollRequest, Principal principal) {
        User user = userRepository.findById(principal.getName()).orElseThrow();

        Poll newPoll = new Poll();
        newPoll.setPollName(pollRequest.getPollName());
        newPoll.setUser(user);

        pollRepository.save(newPoll);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Secured endpoint to add a question to a poll
    @PostMapping("/{pollID}/addQuestion")
    public ResponseEntity<Void> addQuestionToPoll(@PathVariable Long pollID, @RequestBody QuestionRequest questionRequest) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();

        Question newQuestion = new Question();
        newQuestion.setQuestionName(questionRequest.getQuestionName());
        newQuestion.setPoll(poll);

        questionRepository.save(newQuestion);

        // Assume questionRequest.getQuestionOptions() is a list of strings
        for (String optionName : questionRequest.getQuestionOptions()) {
            QuestionOption newOption = new QuestionOption();
            newOption.setQuestionOptionName(optionName);
            newOption.setQuestion(newQuestion);

            questionOptionRepository.save(newOption);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Secured endpoint to remove a question from a poll
    @DeleteMapping("/{pollID}/removeQuestion/{questionID}")
    public ResponseEntity<Void> removeQuestionFromPoll(@PathVariable Long pollID, @PathVariable Long questionID, Principal principal) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();
        User user = userRepository.findById(principal.getName()).orElseThrow();
//        log.info("Poll user: {}", poll.getUser()); // Log the value of poll.getUser()
//        log.info("Logged in user: {}", user);
        if (!poll.getUser().equals(user)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // User is not the author of the poll
        }

        questionRepository.deleteById(questionID);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Secured endpoint to get poll details including questions, options, and answer counts
    @GetMapping("/{pollID}")
    public ResponseEntity<PollDetailsDTO> getPollDetails(@PathVariable Long pollID) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();

        List<Question> questions = questionRepository.findByPollPollID(pollID);
        List<QuestionDetailsDTO> questionDTOs = questions.stream()
                .map(question -> {
                    List<QuestionOptionDTO> questionOptions =
                            questionOptionRepository.findByQuestionQuestionID(question.getQuestionID())
                                    .stream()
                                    .map(option -> {
                                        int answerCount = answerRepository.countByQuestionOption(option);
                                        return new QuestionOptionDTO(option, answerCount);
                                    })
                                    .collect(Collectors.toList());
                    return new QuestionDetailsDTO(question, questionOptions);
                })
                .collect(Collectors.toList());

        PollDetailsDTO pollDetailsDTO = new PollDetailsDTO();
        pollDetailsDTO.setPollID(poll.getPollID());
        pollDetailsDTO.setPollName(poll.getPollName());
        pollDetailsDTO.setQuestions(questionDTOs);

        return new ResponseEntity<>(pollDetailsDTO, HttpStatus.OK);
    }

    // Secured endpoint to delete a poll (only accessible by the poll author)
    @DeleteMapping("/{pollID}")
    public ResponseEntity<Void> deletePoll(@PathVariable Long pollID, Principal principal) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();
        User user = userRepository.findById(principal.getName()).orElseThrow();

        if (!poll.getUser().equals(user)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // User is not the author of the poll
        }

        pollRepository.deleteById(pollID);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        log.info("Received registration request: {}", registrationRequest);

        User newUser = new User();
        newUser.setLogin(registrationRequest.getLogin());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword())); // Hash the password
        log.info("Creating new user: {}", newUser);

        try {
            userRepository.save(newUser);
            log.info("User saved successfully");
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (DuplicateKeyException e) {
            // Handle duplicate login (username) error
            log.error("Duplicate login error", e);
            return new ResponseEntity("Username already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Handle other exceptions
            log.error("Error registering user", e);
            return new ResponseEntity("Registration failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Login endpoint (handled by Spring Security)

    // Secured endpoint to get all answers by the authenticated user
    @GetMapping("/answers")
    public ResponseEntity<List<AnswerDTO>> getUserAnswers(Principal principal) {
        List<Answer> userAnswers = answerRepository.findByUserLogin(principal.getName());
        List<AnswerDTO> answerDTOs = userAnswers.stream().map(AnswerDTO::new).collect(Collectors.toList());

        return new ResponseEntity<>(answerDTOs, HttpStatus.OK);
    }

    // Secured endpoint to answer a question
    @PostMapping("/answer")
    public ResponseEntity<Void> answerQuestion(@RequestBody AnswerRequest answerRequest, Principal principal) {
        User user = userRepository.findById(principal.getName()).orElseThrow();
        QuestionOption option = questionOptionRepository.findById(answerRequest.getQuestionOptionID()).orElseThrow();

        Answer newAnswer = new Answer();
        newAnswer.setUser(user);
        newAnswer.setQuestionOption(option);

        answerRepository.save(newAnswer);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Other helper methods and classes (DTOs, Request classes, etc.)
}


