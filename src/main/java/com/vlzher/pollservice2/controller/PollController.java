package com.vlzher.pollservice2.controller;

import com.vlzher.pollservice2.dto.*;
import com.vlzher.pollservice2.entity.*;
import com.vlzher.pollservice2.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public
    ResponseEntity createPoll(@RequestBody PollRequest pollRequest, Principal principal) {
        try {
        User user = userRepository.findById(principal.getName()).orElseThrow();

        Poll newPoll = new Poll();
        newPoll.setPollName(pollRequest.getPollName());
        newPoll.setUser(user);

        Poll savedPoll = pollRepository.save(newPoll);
        Map<String, Object> response = new HashMap<>();
        response.put("pollID", savedPoll.getPollID());
        response.put("pollName", savedPoll.getPollName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (EntityNotFoundException e) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{pollID}/addQuestion")
    public ResponseEntity<QuestionResponseDTO> addQuestionToPoll(@PathVariable Long pollID, @RequestBody QuestionRequest questionRequest) {
        try {
            Poll poll = pollRepository.findById(pollID).orElseThrow(() -> new EntityNotFoundException("Poll not found"));

            Question newQuestion = new Question();
            newQuestion.setQuestionName(questionRequest.getQuestionName());
            newQuestion.setPoll(poll);

            questionRepository.save(newQuestion);

            List<QuestionOptionDTO> questionOptionDTOs = new ArrayList<>();

            for (String optionName : questionRequest.getQuestionOptions()) {
                QuestionOption newOption = new QuestionOption();
                newOption.setQuestionOptionName(optionName);
                newOption.setQuestion(newQuestion);

                questionOptionRepository.save(newOption);

                int answerCount = 0;
                QuestionOptionDTO optionDTO = new QuestionOptionDTO(newOption, answerCount);
                questionOptionDTOs.add(optionDTO);
            }

            QuestionResponseDTO responseDTO = new  QuestionResponseDTO();
            responseDTO.setQuestionID(newQuestion.getQuestionID());
            responseDTO.setQuestionName(newQuestion.getQuestionName());
            responseDTO.setQuestionOptions(questionOptionDTOs);

            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{pollID}/removeQuestion/{questionID}")
    public ResponseEntity<Void> removeQuestionFromPoll(@PathVariable Long pollID, @PathVariable Long questionID, Principal principal) {
        try {
            Poll poll = pollRepository.findById(pollID).orElseThrow(() -> new EntityNotFoundException("Poll not found"));
            User user = userRepository.findById(principal.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (!poll.getUser().equals(user)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            questionRepository.deleteById(questionID);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{pollID}")
    public ResponseEntity<PollDetailsDTO> getPollDetails(@PathVariable Long pollID, Principal principal) {
        try {
            Poll poll = pollRepository.findById(pollID).orElseThrow(() -> new EntityNotFoundException("Poll not found"));

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
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{pollID}")
    public ResponseEntity<Void> deletePoll(@PathVariable Long pollID, Principal principal) {
        try {
            Poll poll = pollRepository.findById(pollID).orElseThrow(() -> new EntityNotFoundException("Poll not found"));
            User user = userRepository.findById(principal.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));

            log.info(poll.getUser().getLogin());
            log.info(user.getLogin());

            if (!poll.getUser().getLogin().equals(user.getLogin())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            pollRepository.deleteById(pollID);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/answers")
    public ResponseEntity<List<AnswerDTO>> getUserAnswers(Principal principal) {
        try {
            List<Answer> userAnswers = answerRepository.findByUserLogin(principal.getName());
            List<AnswerDTO> answerDTOs = userAnswers.stream().map(AnswerDTO::new).collect(Collectors.toList());
            return new ResponseEntity<>(answerDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/answer")
    public ResponseEntity<Void> answerQuestion(@RequestBody AnswerRequest answerRequest, Principal principal) {
        try {
            User user = userRepository.findById(principal.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));
            QuestionOption option = questionOptionRepository.findById(answerRequest.getQuestionOptionID()).orElseThrow(() -> new EntityNotFoundException("Question option not found"));
            Answer newAnswer = new Answer();
            newAnswer.setUser(user);
            newAnswer.setQuestionOption(option);
            answerRepository.save(newAnswer);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}


