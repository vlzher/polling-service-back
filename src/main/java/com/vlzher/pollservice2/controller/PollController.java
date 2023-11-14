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

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public
    ResponseEntity<Map<String, Object>> createPoll(@RequestBody PollRequest pollRequest, Principal principal) {
        User user = userRepository.findById(principal.getName()).orElseThrow();

        Poll newPoll = new Poll();
        newPoll.setPollName(pollRequest.getPollName());
        newPoll.setUser(user);

        Poll savedPoll = pollRepository.save(newPoll);
        Map<String, Object> response = new HashMap<>();
        response.put("pollID", savedPoll.getPollID());
        response.put("pollName", savedPoll.getPollName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Secured endpoint to add a question to a poll
    @PostMapping("/{pollID}/addQuestion")
    public ResponseEntity<QuestionResponseDTO> addQuestionToPoll(@PathVariable Long pollID, @RequestBody QuestionRequest questionRequest) {
        // Retrieve the poll based on the provided pollID
        Poll poll = pollRepository.findById(pollID).orElseThrow(() -> new EntityNotFoundException("Poll not found"));

        // Create a new question and associate it with the poll
        Question newQuestion = new Question();
        newQuestion.setQuestionName(questionRequest.getQuestionName());
        newQuestion.setPoll(poll);

        // Save the new question
        questionRepository.save(newQuestion);

        // List to store question options DTOs
        List<QuestionOptionDTO> questionOptionDTOs = new ArrayList<>();

        // Iterate through the question options and associate them with the new question
        for (String optionName : questionRequest.getQuestionOptions()) {
            QuestionOption newOption = new QuestionOption();
            newOption.setQuestionOptionName(optionName);
            newOption.setQuestion(newQuestion);

            // Save the new question option
            questionOptionRepository.save(newOption);

            // Count the answer and create a QuestionOptionDTO
            int answerCount = 0; // You might need to implement logic to count answers
            QuestionOptionDTO optionDTO = new QuestionOptionDTO(newOption, answerCount);
            questionOptionDTOs.add(optionDTO);
        }

        // Create a response DTO and set its properties
        QuestionResponseDTO responseDTO = new  QuestionResponseDTO();
        responseDTO.setQuestionID(newQuestion.getQuestionID());
        responseDTO.setQuestionName(newQuestion.getQuestionName());
        responseDTO.setQuestionOptions(questionOptionDTOs);

        // Respond with the created response DTO and HTTP 201 Created status
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Secured endpoint to remove a question from a poll
    @DeleteMapping("/{pollID}/removeQuestion/{questionID}")
    public ResponseEntity<Void> removeQuestionFromPoll(@PathVariable Long pollID, @PathVariable Long questionID, Principal principal) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();
        User user = userRepository.findById(principal.getName()).orElseThrow();
        if (!poll.getUser().equals(user)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // User is not the author of the poll
        }

        questionRepository.deleteById(questionID);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Secured endpoint to get poll details including questions, options, and answer counts
    @GetMapping("/{pollID}")
    public ResponseEntity<PollDetailsDTO> getPollDetails(@PathVariable Long pollID,Principal principal) {
        Poll poll = pollRepository.findById(pollID).orElseThrow();
        String userLogin = principal.getName();
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
        pollDetailsDTO.setMyPoll(poll.getUser().getLogin().equals(userLogin));
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
        log.info(poll.getUser().getLogin());
        log.info(user.getLogin());
        if (!poll.getUser().getLogin().equals(user.getLogin())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // User is not the author of the poll
        }

        pollRepository.deleteById(pollID);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
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


