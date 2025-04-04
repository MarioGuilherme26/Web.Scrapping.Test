import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WebScrapingTest {
    public static void main(String[] args) {
        try {
            String url = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
            String pdfDirectory = "C:\\Users\\mario\\OneDrive\\Área de Trabalho\\PDFS";
            File dir = new File(pdfDirectory);
            if (!dir.exists()) dir.mkdir();

            int zipCounter = 1;
            String zipFilePath;
            do {
                zipFilePath = pdfDirectory + "\\Download" + zipCounter + ".zip";
                zipCounter++;
            } while (new File(zipFilePath).exists());

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                int fileCount = 0;

                for (Element link : links) {
                    String href = link.attr("abs:href");
                    if (link.text().contains("Anexo I") || link.text().contains("Anexo II") || link.text().contains("Anexo III")) {
                        System.out.println("Baixando: " + href);
                        String fileName = link.text().replace(" ", "_") + ".pdf";
                        String filePath = pdfDirectory + "\\" + fileName;

                        downloadFile(href, filePath);
                        addToZip(filePath, zipOutputStream);

                        fileCount++;

                        if (fileCount >= 3) {
                            System.out.println("Download concluído: 3 arquivos baixados e compactados em " + zipFilePath);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String fileURL, String fileName) throws IOException {
        URL url = new URL(fileURL);
        try (InputStream in = url.openStream();
             FileOutputStream fos = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void addToZip(String filePath, ZipOutputStream zipOutputStream) throws IOException {
        File file = new File(filePath);
        try (InputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
            zipOutputStream.closeEntry();
        }
        System.out.println("Adicionado ao ZIP: " + file.getName());
    }
}