package contract.management.bea.CONTRACT_MANAGER.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
      @Autowired
    private PasswordEncoder passwordEncoder;

    public User getByLogin(String login) {
        System.out.println("Checking request: " + login + " je suis la");
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByMatricule(login.trim()));
        return userOpt.orElse(null);
    }

    public String structureByLogin(String userLogin) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByEmail(userLogin.trim()));
        return userOpt.map(user -> String.valueOf(user.getStructId())).orElse("Unknown");
    }

    public String updatePassword(String login, String pass) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByMatricule(login.trim()));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(pass.trim());
            userRepository.save(user);
            return "Password updated successfully";
        }
        return "User not found";
    }

    public List<User> getListUsers() {
        return userRepository.findAll();
    }

    public User insertUser(User user) {
        return userRepository.save(user); 
    }

    
    public int countMatricule(String matricule) {
        return userRepository.findByMatricule(matricule) == null ? 0 : 1;
    }

  public User addUser(User user) {
    user.setStatus("ACTIVE");
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    User savedUser = userRepository.save(user);

     generateUserPdf(savedUser); 
    return savedUser;
    }
    public void generateUserPdf(User user) {
    try {
        String filePath = "pdfs/" + user.getMatricule() + ".pdf";
        File pdfDir = new File("pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        document.add(new Paragraph("User Information", font));
        document.add(new Paragraph("Matricule: " + user.getMatricule()));
        document.add(new Paragraph("Name: " + user.getFirstName() + " " + user.getLastName()));
        document.add(new Paragraph("Email: " + user.getEmail()));
       document.add(new Paragraph("Role: " + user.getRole().getRoleName()));
        document.add(new Paragraph("Structure ID: " + user.getStructId()));
        document.add(new Paragraph("Status: " + user.getStatus()));

        document.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public File getUserPdfFile(String matricule) {
    return new File("pdfs/" + matricule + ".pdf");
}

    public User getUserByMatricule(String matricule) {
    return userRepository.findById(matricule)
        .orElseThrow(() -> new RuntimeException("User not found"));
}

     public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("INACTIVE");
        userRepository.save(user);
    }
    public void activateUser(String matricule) {
        User user = userRepository.findById(matricule)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("ACTIVE");
        userRepository.save(user);
    }
  
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isMatriculeExists(String matricule) {
        return userRepository.existsByMatricule(matricule);
    }

    public void updateUserEmail(String matricule, String newEmail) {
       User user = userRepository.findByMatricule(matricule);
         if (user == null) {
    throw new RuntimeException("User not found");
      }
        user.setEmail(newEmail);
        userRepository.save(user);
    }
}

