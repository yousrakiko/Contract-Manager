package contract.management.bea.CONTRACT_MANAGER.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import contract.management.bea.CONTRACT_MANAGER.dto.CommentDTO;
import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.service.CommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final ContractsRepository contractsRepository;

    public CommentController(CommentService commentService, ContractsRepository contractsRepository) {
        this.commentService = commentService;
        this.contractsRepository = contractsRepository;
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.saveComment(comment));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contract/{contractId}")
public ResponseEntity<List<CommentDTO>> getCommentsByContract(@PathVariable Integer contractId) {
    Contracts contract = contractsRepository.findById(contractId).orElse(null);
    if (contract == null) {
        return ResponseEntity.notFound().build();
    }

    List<CommentDTO> dtoList = commentService.getCommentsByContract(contract)
        .stream()
        .map(comment -> new CommentDTO(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getContract().getContractId(),
            comment.getAuthor().getMatricule()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(dtoList);
}

@GetMapping
public ResponseEntity<List<CommentDTO>> getAllComments() {
    List<CommentDTO> dtoList = commentService.getAllComments()
        .stream()
        .map(comment -> new CommentDTO(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getContract().getContractId(),
            comment.getAuthor().getMatricule()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(dtoList);
}

}

