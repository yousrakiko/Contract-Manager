package contract.management.bea.CONTRACT_MANAGER.service;

import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByContract(Contracts contract) {
        return commentRepository.findByContract(contract);
    }

    public void deleteComment(Integer id) {
        commentRepository.deleteById(id);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
}

