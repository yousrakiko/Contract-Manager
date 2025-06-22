package contract.management.bea.CONTRACT_MANAGER.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.model.AcquisitionContract;

@Service
public class WordTemplateService {
    public File generateContractFromTemplate(AcquisitionContract form, String templateName) throws IOException {
        InputStream templateStream = getClass().getResourceAsStream("/templates/docx/acquisitionTemplate.docx");
        XWPFDocument doc = new XWPFDocument(templateStream);

        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, form);
        }

        File tempFile = File.createTempFile("contract_", ".docx");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            doc.write(out);
        }

        return tempFile;
    }

    private void replaceInParagraph(XWPFParagraph paragraph, AcquisitionContract form) {
    String paragraphText = paragraph.getText();
 if (paragraphText.contains("${contractName}")){
        paragraphText = paragraphText.replace("${contractName}", form.getContractName());
    }
    if (paragraphText.contains("${contractObject}")){
        paragraphText = paragraphText.replace("${contractObject}", form.getContractObject());
    }
    if (paragraphText.contains("${montantchiffres}")){
        paragraphText = paragraphText.replace("${montantchiffres}", form.getMontantchiffres());
    }
    if (paragraphText.contains("${montantlettres}")){
        paragraphText = paragraphText.replace("${montantlettres}", form.getMontantlettres());
    }
    if (paragraphText.contains("${HTchiffres}")){
        paragraphText = paragraphText.replace("${HTchiffres}", form.getHTchiffres());
    }
    if (paragraphText.contains("${HTlettres}")){
        paragraphText = paragraphText.replace("${HTlettres}", form.getHTlettres());
    }
    if (paragraphText.contains("${TVAchiffres}")){
        paragraphText = paragraphText.replace("${TVAchiffres}", form.getTVAchiffres());
    }
    if (paragraphText.contains("${TVAlettres}")){
        paragraphText = paragraphText.replace("${TVAlettres}", form.getTVAlettres());
    }
    if (paragraphText.contains("${delaiExec}")){
        paragraphText = paragraphText.replace("${delaiExec}", form.getDelaiExec());
    }
    if (paragraphText.contains("${delaiGar}")){
        paragraphText = paragraphText.replace("${delaiGar}", form.getDelaiGar());
    }
    if (paragraphText.contains("${companyName}")){
        paragraphText = paragraphText.replace("${companyName}", form.getCompanyName());
    }
    if (paragraphText.contains("${capital}")){
        paragraphText = paragraphText.replace("${capital}", form.getCapital());
    }
    if (paragraphText.contains("${SiegeSocial}")){
        paragraphText = paragraphText.replace("${SiegeSocial}", form.getSiegeSocial());
    }
    if (paragraphText.contains("${representatif}")){
        paragraphText = paragraphText.replace("${representatif}", form.getRepresentatif());
    }
    if (paragraphText.contains("${repPosition}")){
        paragraphText = paragraphText.replace("${repPostion}", form.getRepPosition());
    }
    if (paragraphText.contains("${regCom}")){
        paragraphText = paragraphText.replace("${regCom}", form.getRegCom());
    }
    if (paragraphText.contains("${fax}")){
        paragraphText = paragraphText.replace("${fax}", form.getFax());
    }
    if (paragraphText.contains("${tel}")){
        paragraphText = paragraphText.replace("${tel}", form.getTel());
    }
    if (paragraphText.contains("${email}")){
        paragraphText = paragraphText.replace("${email}", form.getEmail());
    }
    if (paragraphText.contains("${bank}")){
        paragraphText = paragraphText.replace("${bank}", form.getBank());
    }
    if (paragraphText.contains("${bankAdress}")){
        paragraphText = paragraphText.replace("${bankAdress}", form.getBankAdress());
    }
    if (paragraphText.contains("${compte}")){
        paragraphText = paragraphText.replace("${compte}", form.getCompte());
    }
    if (paragraphText.contains("${key}")){
        paragraphText = paragraphText.replace("${key}", form.getKey());
    }
 
    int runCount = paragraph.getRuns().size();
    for (int i = runCount - 1; i >= 0; i--) {
        paragraph.removeRun(i);
    }

    paragraph.createRun().setText(paragraphText, 0);
}

}
